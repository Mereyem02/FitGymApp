package com.example.fitgym.ui.client.auth;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitgym.R;
import com.example.fitgym.data.dao.DAOClient;
import com.example.fitgym.data.db.DatabaseHelper;
import com.example.fitgym.data.db.FirebaseHelper;
import com.example.fitgym.data.model.Admin;
import com.example.fitgym.data.model.Client;
import com.example.fitgym.ui.admin.LoginAdminActivity;
import com.example.fitgym.ui.admin.MainActivityAdmin;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnSeConnecter;
    private TextView tvGoToRegister, tvForgotPassword;
    private SignInButton btnGoogleSignIn;

    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;
    private GoogleSignInClient googleSignInClient;
    private DatabaseHelper dbHelper;

    private static final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_client);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnSeConnecter = findViewById(R.id.btnSeConnecter1);
        tvGoToRegister = findViewById(R.id.tvGoToRegister1);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        LinearLayout rootLayout = findViewById(R.id.rootLayout);
        Animation formAnim = AnimationUtils.loadAnimation(this, R.anim.form_anim);
        rootLayout.startAnimation(formAnim);

        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();
        dbHelper = new DatabaseHelper(this);

        // Toggle password visibility
        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if ((etPassword.getInputType() & android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ==
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else {
                        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }
                    etPassword.setSelection(etPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        btnSeConnecter.setOnClickListener(v -> loginClient());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, RecoverPasswordActivity.class))
        );

        // Google Sign-In config
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
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



        if (!isNetworkAvailable()) {
            DAOClient daoClient = new DAOClient(this);
            Client localClient = daoClient.obtenirClientParEmail(email);
            // OFFLINE login
            if (localClient != null && password.equals(localClient.getMotDePasse())) {
                Toast.makeText(this, "Connexion hors ligne réussie ✅", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Identifiants incorrects (offline) ❌", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            // MODE ONLINE → Firebase
            FirebaseHelper firebaseHelper = new FirebaseHelper();
            firebaseHelper.getClient(new FirebaseHelper.ClientCallback() {
                @Override
                public void onCallback(Client client) {
                    if (client != null && client.getEmail().equals(email) && client.getMotDePasse().equals(password)) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Bienvenue " + client.getEmail() + " ! ✅", Toast.LENGTH_SHORT).show();
                            dbHelper.syncClient(client);
                            startActivity(new Intent(LoginActivity.this, MainActivityAdmin.class));
                            finish();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, "Login ou mot de passe incorrect ❌", Toast.LENGTH_SHORT).show()
                        );
                    }
                }

            });
        }
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isConnected();
    }

    private void googleSignIn() {
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
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
                    Toast.makeText(this, "Erreur Google Sign-In ❌", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Connexion Google échouée ❌", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Échec authentification Firebase ❌", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    Client client = new Client();
                    client.setId(user.getUid());
                    client.setNom(user.getDisplayName());
                    client.setEmail(user.getEmail());
                    client.setMotDePasse("");

                    DAOClient daoClient = new DAOClient(this);
                    firebaseHelper.ajouterClient(client, success -> {
                        if (success) {
                            daoClient.modifierClient(client); // sauvegarde locale
                            Toast.makeText(this, "Connecté avec Google ✅", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur ajout client Firebase ❌", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }
}
