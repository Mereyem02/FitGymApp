package com.example.fitgym.ui.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitgym.R;
import com.example.fitgym.data.model.Coach;
import com.example.fitgym.ui.viewmodel.CoachViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListeCoachsActivity extends AppCompatActivity {

    private CoachViewModel viewModelCoach;
    private RecyclerView listeCoachsRecyclerView;
    private AdaptateurCoach adaptateur;
    private MaterialButton btnAjouterCoach;
    private TextInputEditText champRechercheCoach;
    private BottomNavigationView navigationBas;

    // Garde une copie de la liste complète pour le filtre
    private List<Coach> listeCompleteCoachs = new ArrayList<>();

    // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par Android) ---
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lie cette classe à son fichier XML (le design)
        setContentView(R.layout.activity_liste_coachs);

        // Initialisation des vues (liaison avec le XML)
        listeCoachsRecyclerView = findViewById(R.id.recyclerViewCoachs);
        btnAjouterCoach = findViewById(R.id.btnAjouterCoach);
        champRechercheCoach = findViewById(R.id.edtSearchCoach);
        navigationBas = findViewById(R.id.bottomNavigation);

        // Configuration du RecyclerView (la liste)
        adaptateur = new AdaptateurCoach();
        listeCoachsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listeCoachsRecyclerView.setAdapter(adaptateur);

        // ViewModel
        viewModelCoach = new ViewModelProvider(this).get(CoachViewModel.class);

        // Observer (surveiller) la liste des coachs
        viewModelCoach.getListeCoachs().observe(this, new Observer<List<Coach>>() {
            // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par l'Observer) ---
            @Override
            public void onChanged(List<Coach> coaches) {
                // Quand les données changent, on met à jour notre liste complète
                listeCompleteCoachs.clear();
                listeCompleteCoachs.addAll(coaches);
                // On dit à l'adaptateur d'afficher cette nouvelle liste
                adaptateur.definirCoachs(coaches);
            }
        });

        // Clic sur le bouton "Ajouter"
        btnAjouterCoach.setOnClickListener(v -> {
            // Ouvre une nouvelle activité pour ajouter un coach
            // Intent intent = new Intent(ListeCoachsActivity.this, AjouterCoachActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Ouvrir l'activité AjouterCoach...", Toast.LENGTH_SHORT).show();
        });

        // Filtre de recherche
        champRechercheCoach.addTextChangedListener(new TextWatcher() {
            // --- CES FONCTIONS NE PEUVENT PAS ÊTRE TRADUITES (Requises par TextWatcher) ---
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // À chaque lettre tapée, on lance le filtre de l'adaptateur
                adaptateur.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Gestion de la navigation en bas
        navigationBas.setSelectedItemId(R.id.nav_coachs); // Marquer "Coachs" comme actif
        navigationBas.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                Toast.makeText(this, "Dashboard cliqué", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_seances) {
                Toast.makeText(this, "Séances cliqué", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_coachs) {
                return true; // On est déjà ici
            } else if (itemId == R.id.nav_reservations) {
                Toast.makeText(this, "Réservations cliqué", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profil) {
                Toast.makeText(this, "Profil cliqué", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    // --- Classe interne pour l'Adaptateur RecyclerView ---
    // Implémente "Filterable" pour permettre la recherche
    private class AdaptateurCoach extends RecyclerView.Adapter<AdaptateurCoach.ViewHolderCoach> implements Filterable {

        private List<Coach> listeCoachsAffichee; // Liste actuellement montrée
        private List<Coach> listeCoachsComplete; // Liste totale pour le filtre

        public AdaptateurCoach() {
            this.listeCoachsAffichee = new ArrayList<>();
            this.listeCoachsComplete = new ArrayList<>();
        }

        // Ma fonction personnalisée pour mettre à jour la liste
        public void definirCoachs(List<Coach> liste) {
            this.listeCoachsAffichee.clear();
            this.listeCoachsAffichee.addAll(liste);
            this.listeCoachsComplete.clear();
            this.listeCoachsComplete.addAll(liste);
            notifyDataSetChanged(); // Rafraîchit l'affichage
        }

        // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par Android) ---
        @NonNull
        @Override
        public ViewHolderCoach onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Crée une nouvelle "carte" en utilisant le layout item_coach.xml
            View vue = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_coach, parent, false);
            return new ViewHolderCoach(vue);
        }

        // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par Android) ---
        @Override
        public void onBindViewHolder(@NonNull ViewHolderCoach holder, int position) {
            // Récupère le coach à cette position
            Coach coach = listeCoachsAffichee.get(position);
            // Appelle la fonction "lierDonnees" pour remplir la carte
            holder.lierDonnees(coach);
        }

        // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par Android) ---
        @Override
        public int getItemCount() {
            // Retourne le nombre total d'items dans la liste affichée
            return listeCoachsAffichee != null ? listeCoachsAffichee.size() : 0;
        }

        // --- GESTION DU FILTRE DE RECHERCHE ---

        // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par Android) ---
        @Override
        public Filter getFilter() {
            return filtreCoach;
        }

        private Filter filtreCoach = new Filter() {
            // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par Android) ---
            @Override
            protected FilterResults performFiltering(CharSequence contrainte) {
                List<Coach> listeFiltree = new ArrayList<>();
                if (contrainte == null || contrainte.length() == 0) {
                    // Si la recherche est vide, on affiche la liste complète
                    listeFiltree.addAll(listeCoachsComplete);
                } else {
                    // Sinon, on filtre
                    String motifFiltre = contrainte.toString().toLowerCase().trim();
                    for (Coach coach : listeCoachsComplete) {
                        if (coach.getNomComplet().toLowerCase().contains(motifFiltre)) {
                            listeFiltree.add(coach);
                        }
                    }
                }
                FilterResults resultats = new FilterResults();
                resultats.values = listeFiltree;
                return resultats;
            }

            // --- CETTE FONCTION NE PEUT PAS ÊTRE TRADUITE (Requise par Android) ---
            @Override
            protected void publishResults(CharSequence contrainte, FilterResults resultats) {
                listeCoachsAffichee.clear();
                listeCoachsAffichee.addAll((List) resultats.values);
                notifyDataSetChanged(); // Rafraîchit la liste avec les résultats filtrés
            }
        };

        // --- Classe interne pour le ViewHolder ---
        // Représente UNE SEULE carte dans la liste
        class ViewHolderCoach extends RecyclerView.ViewHolder {

            // Références aux éléments graphiques du fichier item_coach.xml
            CircleImageView imgProfil;
            TextView txtNom, txtNote, txtDescription, txtNbSeances;
            ChipGroup groupeDeChips;
            MaterialButton btnModifier, btnSupprimer;

            public ViewHolderCoach(@NonNull View vueItem) {
                super(vueItem);
                // Fait la liaison avec le XML une seule fois
                imgProfil = vueItem.findViewById(R.id.imgCoachProfile);
                txtNom = vueItem.findViewById(R.id.txtCoachName);
                txtNote = vueItem.findViewById(R.id.txtCoachRating);
                txtDescription = vueItem.findViewById(R.id.txtCoachDescription);
                txtNbSeances = vueItem.findViewById(R.id.txtSessionCount);
                groupeDeChips = vueItem.findViewById(R.id.chipGroupSpecialties);
                btnModifier = vueItem.findViewById(R.id.btnModifier);
                btnSupprimer = vueItem.findViewById(R.id.btnSupprimer);
            }

            // Ma fonction personnalisée pour remplir la carte avec les données
            public void lierDonnees(Coach coach) {
                // Remplit les champs
                txtNom.setText(coach.getNomComplet());
                txtNote.setText(String.format(Locale.FRANCE, "%.1f (%d avis)", coach.getRating(), coach.getReviewCount()));
                txtDescription.setText(coach.getDescription());
                txtNbSeances.setText(String.format(Locale.FRANCE, "%d séances", coach.getSessionCount()));

                // Charger l'image de profil
                Glide.with(itemView.getContext())
                        .load(coach.getImageUrl())
                        .placeholder(R.drawable.ic_profile_placeholder) // Image par défaut
                        .error(R.drawable.ic_profile_placeholder) // Image en cas d'erreur
                        .into(imgProfil);

                // Gérer les "Chips" (tags de spécialité)
                groupeDeChips.removeAllViews(); // Vider les anciens
                if (coach.getSpecialites() != null) {
                    for (String specialite : coach.getSpecialites()) {
                        Chip chip = new Chip(itemView.getContext());
                        chip.setText(specialite);
                        chip.setChipBackgroundColorResource(R.color.chip_background_color);
                        chip.setTextColor(getResources().getColor(R.color.chip_text_color));
                        groupeDeChips.addView(chip); // Ajouter le nouveau
                    }
                }

                // Clic sur le bouton "Modifier"
                btnModifier.setOnClickListener(v -> {
                    // Logique pour ouvrir l'écran de modification
                    Toast.makeText(ListeCoachsActivity.this, "Modifier " + coach.getNomComplet(), Toast.LENGTH_SHORT).show();
                });

                // Clic sur le bouton "Supprimer"
                btnSupprimer.setOnClickListener(v -> {
                    new AlertDialog.Builder(ListeCoachsActivity.this)
                            .setTitle("Supprimer Coach")
                            .setMessage("Voulez-vous vraiment supprimer " + coach.getNomComplet() + " ?")
                            .setPositiveButton("Oui", (dialog, which) -> {
                                viewModelCoach.supprimerCoach(coach.getId());
                                Toast.makeText(ListeCoachsActivity.this, "Coach supprimé !", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Non", null)
                            .show();
                });
            }
        }
    }
}