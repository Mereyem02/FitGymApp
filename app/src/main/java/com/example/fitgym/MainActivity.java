package com.example.fitgym;

import com.example.fitgym.R;
import com.example.fitgym.ui.admin.LoginAdminActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnAdmin, btnClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdmin = findViewById(R.id.btnAdmin);
        btnClient = findViewById(R.id.btnClient);

        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.fitgym.ui.admin.LoginAdminActivity.class);
            startActivity(intent);
        });
        btnClient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.fitgym.ui.client.auth.LoginActivity.class);
            startActivity(intent);
        });


    }
}
