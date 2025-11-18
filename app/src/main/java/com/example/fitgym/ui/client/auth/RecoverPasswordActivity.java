package com.example.fitgym.ui.client.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitgym.R;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverPasswordActivity extends AppCompatActivity {

    private EditText etEmailRecover;
    private Button btnRecoverPassword;
    private ProgressBar progressBarRecover;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        etEmailRecover = findViewById(R.id.etEmailRecover);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);
        progressBarRecover = findViewById(R.id.progressBarRecover);

        mAuth = FirebaseAuth.getInstance();

        btnRecoverPassword.setOnClickListener(v -> recoverPassword());
    }

    private void recoverPassword() {
        String email = etEmailRecover.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarRecover.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBarRecover.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Lien de réinitialisation envoyé à " + email, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
