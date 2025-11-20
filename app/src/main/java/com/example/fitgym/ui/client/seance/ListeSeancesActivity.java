package com.example.fitgym.ui.client.seance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.fitgym.R;
import com.example.fitgym.data.db.DatabaseHelper;
import com.example.fitgym.data.db.FirebaseHelper;
import com.example.fitgym.data.model.Seance;
import com.example.fitgym.ui.adapter.SeanceAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListeSeancesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SeanceAdapter adapter;
    private List<Seance> listSeances = new ArrayList<>();
    private FirebaseHelper firebaseHelper;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_seance);

        recyclerView = findViewById(R.id.recyclerSeances);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);
        firebaseHelper = new FirebaseHelper();

        adapter = new SeanceAdapter(listSeances, new SeanceAdapter.OnItemClickListener() {
            @Override
            public void onModifierClicked(Seance seance) {
                // côté client, normalement pas de modification
                // sinon tu peux naviguer
                Intent intent = new Intent(ListeSeancesActivity.this, DetailSeanceActivity.class);
                intent.putExtra("seanceId", seance.getId());
                startActivity(intent);
            }

            @Override
            public void onSupprimerClicked(Seance seance) {
                // en client : normalement rien
            }
        }, db);

        recyclerView.setAdapter(adapter);

        chargerSeances();
    }

    private void chargerSeances() {
        firebaseHelper.getAllSeances(seances -> {
            if (seances != null) {
                listSeances = seances;
                adapter.updateData(seances);
            }
        });
    }
}
