package com.example.fitgym.ui.client.auth;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitgym.R;
import com.example.fitgym.data.db.FirebaseHelper;
import com.example.fitgym.data.model.Client;
import com.example.fitgym.ui.viewmodel.ClientViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputNom, inputEmail, inputTelephone, inputPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private SignInButton btnGoogleSignIn;

    private ClientViewModel clientViewModel;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1000;

    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registre_client);

        inputNom = findViewById(R.id.inputNom);
        inputEmail = findViewById(R.id.inputEmail);
        inputTelephone = findViewById(R.id.inputTelephone);
        inputPassword = findViewById(R.id.inputPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();

        tvGoToLogin.setOnClickListener(v -> finish());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnRegister.setOnClickListener(v -> inscrireClient());
        btnGoogleSignIn.setOnClickListener(v -> googleSignIn());
    }

    // Vérifier Internet
    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isConnected();
    }

    private void inscrireClient() {
        String nom = inputNom.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String telephone = inputTelephone.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // =========================
        // MODE OFFLINE
        // =========================
        if (!hasInternet()) {
            Client client = new Client();
            client.setId(String.valueOf(System.currentTimeMillis())); // id temporaire
            client.setNom(nom);
            client.setEmail(email);
            client.setTelephone(telephone);
            client.setMotDePasse(password);
            client.setSynced(false);

            clientViewModel.inscrire(client);
            Toast.makeText(this, "Compte enregistré hors-ligne", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // =========================
        // MODE ONLINE (FIREBASE)
        // =========================
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Erreur vérification email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isNewUser =
                            task.getResult().getSignInMethods().isEmpty();

                    if (!isNewUser) {
                        Toast.makeText(this, "Email déjà utilisé", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Créer l’utilisateur
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(createTask -> {
                                if (!createTask.isSuccessful()) {
                                    Toast.makeText(
                                            this,
                                            "Erreur création compte: "
                                                    + createTask.getException().getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser == null) return;

                                String uid = firebaseUser.getUid();

                                Client client = new Client();
                                client.setId(uid);
                                client.setNom(nom);
                                client.setEmail(email);
                                client.setTelephone(telephone);
                                client.setMotDePasse("");
                                client.setSynced(true);

                                firebaseHelper.ajouterClient(client, success -> {
                                    if (success) {
                                        clientViewModel.inscrire(client);
                                        Toast.makeText(this,
                                                "Compte créé avec succès !",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(this,
                                                "Erreur d'ajout Firebase",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                });
    }

    // GOOGLE SIGN-IN
    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this,
                        "Connexion Google échouée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Échec authentification Firebase",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser == null) return;

                    String uid = firebaseUser.getUid();
                    String nom = firebaseUser.getDisplayName();
                    String email = firebaseUser.getEmail();

                    Client client = new Client();
                    client.setId(uid);
                    client.setNom(nom);
                    client.setEmail(email);
                    client.setMotDePasse("");
                    client.setSynced(true);

                    // Vérifier si déjà dans Firebase
                    firebaseHelper.getClient(uid, existingClient -> {

                        if (existingClient == null) {
                            firebaseHelper.ajouterClient(client, success -> {
                                if (success) clientViewModel.inscrire(client);
                            });
                        } else {
                            clientViewModel.inscrire(existingClient);
                        }

                        Toast.makeText(this,
                                "Connecté avec Google : " + email,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
    }
}
