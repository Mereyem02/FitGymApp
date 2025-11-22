package com.example.fitgym.ui.client.auth;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitgym.R;
import com.example.fitgym.data.dao.DAOClient;
import com.example.fitgym.data.db.DatabaseHelper;
import com.example.fitgym.data.db.FirebaseHelper;
import com.example.fitgym.data.model.Client;
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

        // Toggle mot de passe visible/invisible
        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etPassword.getRight()
                        - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                    if ((etPassword.getInputType() & android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                            == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
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

        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, RecoverPasswordActivity.class)));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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

        Client localClient = dbHelper.getClientByEmail(email);

        if (!isNetworkAvailable()) {
            if (localClient != null) {
                if (localClient.isGoogleSignIn()) {
                    // Google déjà utilisé → login hors ligne
                    proceedToMainActivity(localClient, "(offline Google)");
                    return;
                }
                // Email/Password hors ligne
                if (password.equals(localClient.getMotDePasse())) {
                    proceedToMainActivity(localClient, "(offline)");
                    return;
                }
            }

            // Connexion hors ligne
            if (localClient != null && localClient.getMotDePasse().trim().equals(password)) {
                proceedToMainActivity(localClient, "(offline)");
            } else {
                Toast.makeText(this, "Email ou mot de passe incorrect ❌ (offline)", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Si compte local non synchronisé → créer sur Firebase
        if (localClient != null && !localClient.isSynced()) {
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(fetchTask -> {
                boolean existsOnFirebase = fetchTask.getResult() != null &&
                        fetchTask.getResult().getSignInMethods() != null &&
                        !fetchTask.getResult().getSignInMethods().isEmpty();

                if (existsOnFirebase) {
                    // Compte existe sur Firebase → login classique
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(loginTask -> {
                                if (!loginTask.isSuccessful()) {
                                    Toast.makeText(this, "Mot de passe incorrect pour le compte en ligne ❌", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user == null) return;

                                localClient.setId(user.getUid());
                                localClient.setSynced(true);
                                new DAOClient(this).modifierClient(localClient);

                                firebaseHelper.ajouterClient(localClient, success -> {
                                    dbHelper.syncClient(localClient);
                                    proceedToMainActivity(localClient, "(synced)");
                                });
                            });
                } else {
                    // Compte n’existe pas sur Firebase → créer
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(createTask -> {
                                if (!createTask.isSuccessful()) {
                                    Toast.makeText(this, "Erreur synchronisation Firebase: " + createTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user == null) return;

                                localClient.setId(user.getUid());
                                localClient.setSynced(true);
                                new DAOClient(this).modifierClient(localClient);

                                firebaseHelper.ajouterClient(localClient, success -> {
                                    dbHelper.syncClient(localClient);
                                    proceedToMainActivity(localClient, "(synced)");
                                });
                            });
                }
            });
            return;
        }

        // Connexion en ligne classique
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Email ou mot de passe incorrect ❌", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    firebaseHelper.getClientById(user.getUid(), client -> {
                        if (client != null) {
                            dbHelper.syncClient(client);
                            proceedToMainActivity(client, "");
                        } else {
                            Client newClient = new Client();
                            newClient.setId(user.getUid());
                            newClient.setNom(user.getDisplayName() != null ? user.getDisplayName() : "Utilisateur");
                            newClient.setEmail(email);
                            newClient.setMotDePasse(password);
                            newClient.setSynced(true);

                            firebaseHelper.ajouterClient(newClient, success -> {
                                dbHelper.syncClient(newClient);
                                proceedToMainActivity(newClient, "");
                            });
                        }
                    });
                });
    }


    private void proceedToMainActivity(Client client, String extra) {
        Toast.makeText(this, "Bienvenue " + client.getNom() + " " + extra + " ✅", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivityAdmin.class));
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // =================== GOOGLE SIGN-IN ===================
    private void googleSignIn() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Google Sign-In nécessite Internet ❌", Toast.LENGTH_SHORT).show();
            return;
        }
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

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    firebaseHelper.getClientById(user.getUid(), existingClient -> {
                        runOnUiThread(() -> {
                            if (existingClient != null) {
                                dbHelper.syncClient(existingClient);
                                Toast.makeText(this, "Connecté avec Google ✅", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivityAdmin.class));
                                finish();
                            } else {
                                Client client = new Client();
                                client.setId(user.getUid());
                                client.setNom(user.getDisplayName());
                                client.setEmail(user.getEmail());
                                client.setMotDePasse("GOOGLE_SIGN_IN");
                                client.setSynced(true);

                                firebaseHelper.ajouterClient(client, success -> {
                                    dbHelper.syncClient(client);
                                    Toast.makeText(this, "Connecté avec Google ✅", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivityAdmin.class));
                                    finish();
                                });
                            }
                        });
                    });
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(Exception.class);
                if (account != null) firebaseAuthWithGoogle(account);
                else Toast.makeText(this, "Erreur Google Sign-In ❌", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Connexion Google échouée ❌", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
