package com.example.fitgym.ui.client.seance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.fitgym.R;
import com.example.fitgym.data.db.FirebaseHelper;
import com.example.fitgym.data.model.Seance;

public class DetailSeanceActivity extends AppCompatActivity {

    private TextView titre, description, dateHeure, prix, categorie, coach;
    private ImageView image;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_seance);

        String seanceId = getIntent().getStringExtra("seanceId");
        firebaseHelper = new FirebaseHelper();

        titre = findViewById(R.id.titreDetail);
        description = findViewById(R.id.descriptionDetail);
        dateHeure = findViewById(R.id.dateHeureDetail);
        prix = findViewById(R.id.prixDetail);
        categorie = findViewById(R.id.categorieDetail);
        coach = findViewById(R.id.coachDetail);
        image = findViewById(R.id.imageDetail);

        firebaseHelper.getSeanceById(seanceId, seance -> {
            if (seance != null) {
                afficherSeance(seance);
            }
        });
    }

    private void afficherSeance(Seance s) {
        titre.setText(s.getTitre());
        description.setText(s.getDescription());
        dateHeure.setText(s.getDate() + " · " + s.getHeure());
        prix.setText(s.getPrix() + " €");
        categorie.setText(s.getCategorieId());
        coach.setText(s.getCoachId());

        Glide.with(this)
                .load(R.drawable.default_image)
                .into(image);
    }
}
