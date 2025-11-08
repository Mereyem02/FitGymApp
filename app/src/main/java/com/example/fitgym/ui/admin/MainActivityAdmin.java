package com.example.fitgym.ui.admin;

import com.example.fitgym.R;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivityAdmin extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // lie ton XML ici
        setContentView(R.layout.activity_main_admin);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Par défaut → Dashboard
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
}
