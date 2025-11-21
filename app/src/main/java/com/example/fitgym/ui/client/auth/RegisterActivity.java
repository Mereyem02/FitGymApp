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
import com.example.fitgym.data.dao.DAOClient;
import com.example.fitgym.data.db.DatabaseHelper;
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

import java.util.List;

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
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registre_client);

        // -------------------- Views --------------------
        inputNom = findViewById(R.id.inputNom);
        inputEmail = findViewById(R.id.inputEmail);
        inputTelephone = findViewById(R.id.inputTelephone);
        inputPassword = findViewById(R.id.inputPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // -------------------- Instances --------------------
        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();
        dbHelper = new DatabaseHelper(this);

        // -------------------- Listeners --------------------
        tvGoToLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> inscrireClient());
        btnGoogleSignIn.setOnClickListener(v -> googleSignIn());

        // -------------------- Google Sign-In --------------------
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sync auto des clients hors-ligne si internet dispo
        if (hasInternet()) syncOfflineClients();
    }

    // -------------------- Vérifier Internet --------------------
    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isConnected();
    }

    // -------------------- Inscription client --------------------
    private void inscrireClient() {
        String nom = inputNom.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String telephone = inputTelephone.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasInternet()) {
            // -------------------- Mode hors-ligne --------------------
            Client client = new Client();
            client.setId(String.valueOf(System.currentTimeMillis())); // ID local temporaire
            client.setNom(nom);
            client.setEmail(email);
            client.setTelephone(telephone);
            client.setMotDePasse(password);
            client.setSynced(false);

            DAOClient dao = new DAOClient(this);
            dao.ajouterClient(client);
            clientViewModel.inscrire(client);

            Toast.makeText(this, "Compte enregistré hors-ligne ✅", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // -------------------- Mode en ligne --------------------
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Erreur vérification email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                    if (!isNewUser) {
                        Toast.makeText(this, "Email déjà utilisé", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Créer compte Firebase Auth
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(createTask -> {
                                if (!createTask.isSuccessful()) {
                                    Toast.makeText(this,
                                            "Erreur création compte: " + createTask.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser == null) return;

                                Client client = new Client();
                                client.setId(firebaseUser.getUid());
                                client.setNom(nom);
                                client.setEmail(email);
                                client.setTelephone(telephone);
                                client.setMotDePasse(""); // ne pas stocker mot de passe Firebase
                                client.setSynced(true);

                                // Ajouter dans Firestore + local
                                firebaseHelper.ajouterClient(client, success -> {
                                    if (success) {
                                        dbHelper.syncClient(client, password); // sync local
                                        clientViewModel.inscrire(client);
                                        Toast.makeText(this, "Compte créé avec succès ✅", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Erreur ajout Firebase ❌", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                });
    }

    // -------------------- Google Sign-In --------------------
    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Échec authentification Firebase ❌", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser == null) return;

                    Client client = new Client();
                    client.setId(firebaseUser.getUid());
                    client.setNom(firebaseUser.getDisplayName());
                    client.setEmail(firebaseUser.getEmail());
                    client.setMotDePasse("");
                    client.setSynced(true);

                    firebaseHelper.getClient(firebaseUser.getUid(), existingClient -> {
                        if (existingClient == null) {
                            firebaseHelper.ajouterClient(client, success -> {
                                if (success) clientViewModel.inscrire(client);
                            });
                        } else {
                            clientViewModel.inscrire(existingClient);
                        }

                        Toast.makeText(this, "Connecté avec Google : " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Connexion Google échouée ❌", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // -------------------- Synchronisation auto --------------------
    private void syncOfflineClients() {
        List<Client> offlineClients = dbHelper.getOfflineClients();
        DAOClient daoClient = new DAOClient(this);

        for (Client client : offlineClients) {
            String email = client.getEmail();
            String password = client.getMotDePasse();

            // Vérifier si le compte existe déjà sur Firebase
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(fetchTask -> {
                        if (!fetchTask.isSuccessful()) return;

                        boolean isNew = fetchTask.getResult().getSignInMethods().isEmpty();

                        if (!isNew) {
                            // Compte existant → juste marquer comme synchronisé localement
                            client.setSynced(true);
                            daoClient.modifierClient(client);
                            return;
                        }

                        // Créer le compte sur Firebase
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(createTask -> {
                                    if (!createTask.isSuccessful()) return;

                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser == null) return;

                                    client.setId(firebaseUser.getUid());

                                    // Ajouter dans Firestore
                                    firebaseHelper.ajouterClient(client, success -> {
                                        if (success) {
                                            client.setSynced(true);
                                            daoClient.modifierClient(client); // mettre à jour ID + synced
                                        }
                                    });
                                });
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasInternet()) syncOfflineClients();
    }
}
