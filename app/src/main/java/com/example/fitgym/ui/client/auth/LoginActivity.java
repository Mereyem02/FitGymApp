package com.example.fitgym.ui.client.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnSeConnecter;
    private TextView tvGoToRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private SignInButton btnGoogleSignIn;

    private ClientViewModel clientViewModel;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;

    private static final int RC_SIGN_IN = 1000;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_client);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnSeConnecter = findViewById(R.id.btnSeConnecter1);
        tvGoToRegister = findViewById(R.id.tvGoToRegister1);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn); // Assure-toi d'avoir le SignInButton dans ton layout

        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();

        // Bouton login classique
        btnSeConnecter.setOnClickListener(v -> loginClient());

        // Aller vers RegisterActivity
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecoverPasswordActivity.class);
            startActivity(intent);
        });

        // Config Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // mettre ton web client id
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setOnClickListener(v -> googleSignIn());
    }

    private void loginClient() {
        String email = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Client client = clientViewModel.login(email, password);

        if (client != null) {
            Toast.makeText(this, "Bienvenue " + client.getNom(), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Login ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    // ==============================
    // Google Sign-In
    // ==============================
    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                } else {
                    Toast.makeText(this, "Erreur Google Sign-In", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Connexion Google échouée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {

            if (!task.isSuccessful()) {
                Toast.makeText(this, "Échec authentification Firebase", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erreur FirebaseAuth Google", task.getException());
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
            client.setMotDePasse(""); // jamais stocker

            // Ajouter dans Firebase Database si nouveau
            firebaseHelper.ajouterClient(client, success -> {
                if (success) {
                    clientViewModel.inscrire(client); // sauvegarde locale SQLite
                    Toast.makeText(this, "Connecté avec Google : " + email, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Erreur ajout client Firebase", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erreur ajout client Google Firebase");
                }
            });
        });
    }
}
