package com.example.fitgym.ui.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitgym.R;
import com.example.fitgym.data.model.Coach;
import com.example.fitgym.ui.viewmodel.CoachViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListeCoachsFragment extends Fragment {

    private CoachViewModel viewModelCoach;
    private RecyclerView recyclerViewCoachs;
    private AdaptateurCoach adaptateur;
    private List<Coach> listeCompleteCoachs = new ArrayList<>();
    private TextView txtTotalCoachs, txtNombreResultats;
    private FloatingActionButton fabAjouterCoach;
    private EditText champRecherche;

    private static final int PICK_IMAGE = 101;
    private Uri imageUri;
    private ImageView imagePreviewActive;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View vue = inflater.inflate(R.layout.activity_liste_coachs, container, false);

        viewModelCoach = new ViewModelProvider(this).get(CoachViewModel.class);

        recyclerViewCoachs = vue.findViewById(R.id.coachesRecyclerView);
        txtNombreResultats = vue.findViewById(R.id.resultCount);
        champRecherche = vue.findViewById(R.id.searchInput);
        fabAjouterCoach = vue.findViewById(R.id.fabAddCoach);

        adaptateur = new AdaptateurCoach();
        recyclerViewCoachs.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCoachs.setAdapter(adaptateur);

        configurerEcouteurs();
        observerViewModel();

        return vue;
    }

    private void configurerEcouteurs() {
        fabAjouterCoach.setOnClickListener(v -> {
            // tu peux lancer un Fragment ou Activity pour ajouter un coach
        });

        champRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adaptateur.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void observerViewModel() {
        viewModelCoach.getListeCoachs().observe(getViewLifecycleOwner(), new Observer<List<Coach>>() {
            @Override
            public void onChanged(List<Coach> coaches) {
                listeCompleteCoachs.clear();
                listeCompleteCoachs.addAll(coaches);
                adaptateur.definirCoachs(coaches);
                txtTotalCoachs.setText(String.valueOf(coaches.size()));
            }
        });
    }

    private void afficherDialogModifierCoach(Coach coach) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View vueDialog = getLayoutInflater().inflate(R.layout.dialog_modifier_coach, null);
        builder.setView(vueDialog);
        AlertDialog dialog = builder.create();

        TextInputEditText inputNom = vueDialog.findViewById(R.id.inputNomComplet);
        TextInputEditText inputSpecialites = vueDialog.findViewById(R.id.inputSpecialites);
        TextInputEditText inputBio = vueDialog.findViewById(R.id.inputBiographie);
        MaterialButton btnChoisirImage = vueDialog.findViewById(R.id.inputUrlImage);
        MaterialButton btnModifier = vueDialog.findViewById(R.id.btnModifier);
        MaterialButton btnAnnuler = vueDialog.findViewById(R.id.btnAnnuler);

        inputNom.setText(coach.getNomComplet());
        inputSpecialites.setText(String.join(", ", coach.getSpecialites()));
        inputBio.setText(coach.getDescription());
        Glide.with(this).load(coach.getPhotoUrl()).into(imagePreviewActive);

        btnChoisirImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnModifier.setOnClickListener(v -> {
            String nom = inputNom.getText().toString().trim();
            String specs = inputSpecialites.getText().toString().trim();
            String bio = inputBio.getText().toString().trim();

            if (nom.isEmpty() || bio.isEmpty()) {
                Toast.makeText(getContext(), "Nom et biographie obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            coach.setNom(nom);
            coach.setDescription(bio);
            coach.setSpecialites(List.of(specs.split(",")));
            if (imageUri != null) coach.setPhotoUrl(imageUri.toString());

            viewModelCoach.modifierCoach(coach);
            Toast.makeText(getContext(), "Coach modifié !", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null && imagePreviewActive != null) {
            imageUri = data.getData();
            imagePreviewActive.setImageURI(imageUri);
        }
    }

    private class AdaptateurCoach extends RecyclerView.Adapter<AdaptateurCoach.ViewHolderCoach> implements Filterable {

        private List<Coach> listeAffichee = new ArrayList<>();
        private List<Coach> listeComplete = new ArrayList<>();

        public void definirCoachs(List<Coach> liste) {
            listeAffichee.clear();
            listeAffichee.addAll(liste);
            listeComplete.clear();
            listeComplete.addAll(liste);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolderCoach onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vue = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach, parent, false);
            return new ViewHolderCoach(vue);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderCoach holder, int position) {
            holder.lierDonnees(listeAffichee.get(position));
        }

        @Override
        public int getItemCount() { return listeAffichee.size(); }

        @Override
        public Filter getFilter() { return filtreCoach; }

        private final Filter filtreCoach = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Coach> listeFiltree = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) listeFiltree.addAll(listeComplete);
                else {
                    String motif = constraint.toString().toLowerCase(Locale.FRANCE).trim();
                    for (Coach c : listeComplete) {
                        if (c.getNomComplet().toLowerCase(Locale.FRANCE).contains(motif)
                                || c.getDescription().toLowerCase(Locale.FRANCE).contains(motif)
                                || (c.getSpecialites() != null && c.getSpecialites().toString().toLowerCase(Locale.FRANCE).contains(motif))) {
                            listeFiltree.add(c);
                        }
                    }
                }
                FilterResults result = new FilterResults();
                result.values = listeFiltree;
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listeAffichee.clear();
                listeAffichee.addAll((List<Coach>) results.values);
                txtNombreResultats.setText(listeAffichee.size() + " coachs trouvés");
                notifyDataSetChanged();
            }
        };

        class ViewHolderCoach extends RecyclerView.ViewHolder {
            CircleImageView imgProfil;
            TextView txtNom, txtDescription, txtNote, txtNbSeances;
            ChipGroup groupeDeChips;
            MaterialButton btnModifier;
            Button btnSupprimer;

            public ViewHolderCoach(@NonNull View itemView) {
                super(itemView);
                imgProfil = itemView.findViewById(R.id.coachAvatar);
                txtNom = itemView.findViewById(R.id.coachName);
                txtDescription = itemView.findViewById(R.id.coachDescription);
                txtNote = itemView.findViewById(R.id.coachRating);
                txtNbSeances = itemView.findViewById(R.id.coachSessionsCount);
                groupeDeChips = itemView.findViewById(R.id.chipGroupSpecialties);
                btnModifier = itemView.findViewById(R.id.btnEditCoach);
                btnSupprimer = itemView.findViewById(R.id.btnSupprimer);
            }

            public void lierDonnees(Coach coach) {
                txtNom.setText(coach.getNomComplet());
                txtDescription.setText(coach.getDescription());
                txtNote.setText(String.format(Locale.FRANCE, "%.1f (%d avis)", coach.getRating(), coach.getReviewCount()));
                txtNbSeances.setText(coach.getSessionCount() + " séances");

                Glide.with(itemView.getContext())
                        .load(coach.getPhotoUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imgProfil);

                groupeDeChips.removeAllViews();
                if (coach.getSpecialites() != null) {
                    for (String s : coach.getSpecialites()) {
                        Chip chip = new Chip(itemView.getContext());
                        chip.setText(s);
                        chip.setChipBackgroundColorResource(R.color.chip_background_color);
                        chip.setTextColor(getResources().getColor(R.color.chip_text_color));
                        groupeDeChips.addView(chip);
                    }
                }

                btnModifier.setOnClickListener(v -> afficherDialogModifierCoach(coach));
                btnSupprimer.setOnClickListener(v -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Supprimer Coach")
                            .setMessage("Voulez-vous vraiment supprimer " + coach.getNomComplet() + " ?")
                            .setPositiveButton("Oui", (dialog, which) -> viewModelCoach.supprimerCoach(coach.getId()))
                            .setNegativeButton("Non", null)
                            .show();
                });
            }
        }
    }
}
