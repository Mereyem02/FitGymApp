package com.example.fitgym.ui.admin;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fitgym.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivityAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Fragment par défaut → Dashboard
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragmentAdmin())
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragmentAdmin();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragmentAdmin();
            } else if (id == R.id.nav_sessions) {
                // selectedFragment = new SessionsFragment();
            } else if (id == R.id.nav_coaches) {
                selectedFragment = new ListeCoachsFragment();
            } else if (id == R.id.nav_reservations) {
                // selectedFragment = new ReservationsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    // Méthode appelée depuis le XML (android:onClick)
    public void onManageCoachsClick(View view) {
        // Remplacer le fragment actuel par ListeCoachsFragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ListeCoachsFragment())
                .addToBackStack(null) // permet de revenir en arrière
                .commit();
    }
}
