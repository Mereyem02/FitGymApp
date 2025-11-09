package com.example.fitgym.ui.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitgym.R;
import com.example.fitgym.data.db.FirebaseHelper;
import com.example.fitgym.data.model.Coach;
import com.example.fitgym.ui.viewmodel.CoachViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListeCoachsFragment extends Fragment {

    private CoachViewModel viewModelCoach;
    private RecyclerView recyclerViewCoachs;
    private AdaptateurCoach adaptateur;
    private List<Coach> listeCompleteCoachs = new ArrayList<>(); // source "master"
    private List<Coach> listeAffichee = new ArrayList<>();       // source affichée (filtrée/triée)
    private FloatingActionButton fabAjouterCoach;
    private EditText champRecherche;
    private static final int PICK_IMAGE = 101;
    private Uri imageUri;
    private ImageView imagePreviewActive;
    private ImageButton btnFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View vue = inflater.inflate(R.layout.activity_liste_coachs, container, false);

        // --- ViewModel / Views ---
        viewModelCoach = new ViewModelProvider(this).get(CoachViewModel.class);
        recyclerViewCoachs = vue.findViewById(R.id.coachesRecyclerView);
        champRecherche = vue.findViewById(R.id.searchInput);
        fabAjouterCoach = vue.findViewById(R.id.fabAddCoach);

        // important : initialiser btnFilter AVANT de l'utiliser
        btnFilter = vue.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> afficherDialogFiltre());

        ImageButton btnBack = vue.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // on utilise la vue passée pour obtenir le NavController — plus fiable
            try {
                Navigation.findNavController(v).popBackStack();
            } catch (Exception e) {
                // fallback si nav non disponible
                requireActivity().finish();
            }
        });

        // RecyclerView + adapter
        adaptateur = new AdaptateurCoach();
        recyclerViewCoachs.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCoachs.setAdapter(adaptateur);

        configurerEcouteurs();
        observerViewModel();

        return vue;
    }

    private void afficherDialogFiltre() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filtre_coach, null);

        Spinner spinnerFiltre = dialogView.findViewById(R.id.spinnerFiltre);
        Spinner spinnerSort = dialogView.findViewById(R.id.spinnerSort);

        // Valeurs des filtres et tris
        String[] filtres = {"Tous", "Fitness", "Yoga", "Crossfit", "Cardio"};
        String[] tris = {"Nom (A-Z)", "Nom (Z-A)", "Plus populaire ↑", "Séances ↑"};

        ArrayAdapter<String> adapterFiltre = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, filtres);
        spinnerFiltre.setAdapter(adapterFiltre);

        ArrayAdapter<String> adapterSort = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, tris);
        spinnerSort.setAdapter(adapterSort);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filtrer et trier les coachs");
        builder.setView(dialogView);

        builder.setPositiveButton("Appliquer", (dialog, which) -> {
            String filtreChoisi = spinnerFiltre.getSelectedItem().toString();
            String triChoisi = spinnerSort.getSelectedItem().toString();
            appliquerFiltreEtTri(filtreChoisi, triChoisi);
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * Applique filtre + tri sur la listeCompleteCoachs et met à jour le RecyclerView.
     */
    private void appliquerFiltreEtTri(String filtre, String tri) {
        if (listeCompleteCoachs == null || listeCompleteCoachs.isEmpty()) {
            Toast.makeText(getContext(), "Aucun coach disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Filtrage
        List<Coach> resultat = new ArrayList<>();
        for (Coach c : listeCompleteCoachs) {
            if (filtre.equals("Tous")) {
                resultat.add(c);
            } else {
                // specialites est une List<String> — on vérifie la présence d'une spécialité qui contient le mot
                List<String> specs = c.getSpecialites();
                if (specs != null) {
                    for (String s : specs) {
                        if (s != null && s.trim().equalsIgnoreCase(filtre)) {
                            resultat.add(c);
                            break;
                        }
                    }
                }
            }
        }

        // Tri
        switch (tri) {
            case "Nom (A-Z)":
                Collections.sort(resultat, (c1, c2) -> c1.getNomComplet().compareToIgnoreCase(c2.getNomComplet()));
                break;
            case "Nom (Z-A)":
                Collections.sort(resultat, (c1, c2) -> c2.getNomComplet().compareToIgnoreCase(c1.getNomComplet()));
                break;
            case "Plus populaire ↑":
                Collections.sort(resultat, (c1, c2) -> Double.compare(c2.getRating(), c1.getRating())); // décroissant
                break;
            case "Séances ↑":
                Collections.sort(resultat, (c1, c2) -> Integer.compare(c2.getSessionCount(), c1.getSessionCount())); // décroissant
                break;
            default:
                // pas de tri particulier
                break;
        }

        // Appliquer sur la liste affichée et notifier l'adapter
        listeAffichee.clear();
        listeAffichee.addAll(resultat);
        adaptateur.definirCoachs(listeAffichee);
        Toast.makeText(getContext(), "Filtre appliqué : " + filtre + " | Tri : " + tri, Toast.LENGTH_SHORT).show();
    }

    private void configurerEcouteurs() {
        fabAjouterCoach.setOnClickListener(v -> afficherDialogAjouterCoach());

        champRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adaptateur != null) adaptateur.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void observerViewModel() {
        viewModelCoach.getListeCoachs().observe(getViewLifecycleOwner(), coaches -> {
            if (coaches == null) return;
            // mettre à jour liste master et liste affichée
            listeCompleteCoachs.clear();
            listeCompleteCoachs.addAll(coaches);

            listeAffichee.clear();
            listeAffichee.addAll(coaches);

            adaptateur.definirCoachs(listeAffichee);
        });
    }

    private void afficherDialogAjouterCoach() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View vueDialog = getLayoutInflater().inflate(R.layout.dialog_ajout_coach, null);
        builder.setView(vueDialog);
        AlertDialog dialog = builder.create();

        TextInputEditText edtNom = vueDialog.findViewById(R.id.inputNomComplet);
        TextInputEditText edtDescription = vueDialog.findViewById(R.id.inputBiographie);
        TextInputEditText edtSpecialites = vueDialog.findViewById(R.id.inputSpecialites);
        MaterialButton btnChoisirImage = vueDialog.findViewById(R.id.inputUrlImage);
        Button btnAnnuler = vueDialog.findViewById(R.id.btnAnnuler);
        Button btnAjouter = vueDialog.findViewById(R.id.btnModifier);
        imagePreviewActive = vueDialog.findViewById(R.id.imagePreview);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        // Choix d'image
        btnChoisirImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnAjouter.setOnClickListener(v -> {
            String nom = edtNom.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();
            String specs = edtSpecialites.getText().toString().trim();

            if (nom.isEmpty()) {
                Toast.makeText(getContext(), "Nom obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            Coach nouveauCoach = new Coach();
            nouveauCoach.setNom(nom);
            nouveauCoach.setDescription(desc);
            nouveauCoach.setSpecialites(specs.isEmpty() ? new ArrayList<>() : Arrays.asList(specs.split(",")));

            // Upload image si elle a été choisie
            uploadImageToFirebase(imageUri, downloadUrl -> {
                if (downloadUrl != null) nouveauCoach.setPhotoUrl(downloadUrl);
                else nouveauCoach.setPhotoUrl("");

                FirebaseHelper firebaseHelper = new FirebaseHelper();
                firebaseHelper.ajouterCoach(nouveauCoach, success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Coach ajouté !", Toast.LENGTH_SHORT).show();

                        // Ajout direct dans la liste et RecyclerView
                        listeCompleteCoachs.add(0, nouveauCoach); // en tête
                        listeAffichee.add(0, nouveauCoach);
                        adaptateur.definirCoachs(listeAffichee);

                        dialog.dismiss();
                        imageUri = null; // reset URI
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        imagePreviewActive.setImageResource(R.drawable.ic_placeholder);
        dialog.show();
    }

    // --- Upload image Firebase Storage ---
    private void uploadImageToFirebase(Uri uri, OnImageUploadListener listener) {
        if (uri == null) {
            listener.onUploadComplete(null);
            return;
        }

        String nomFichier = "coachs/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(nomFichier);

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> listener.onUploadComplete(downloadUri.toString()))
                        .addOnFailureListener(e -> listener.onUploadComplete(null)))
                .addOnFailureListener(e -> listener.onUploadComplete(null));
    }

    public interface OnImageUploadListener {
        void onUploadComplete(String downloadUrl);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imagePreviewActive != null) imagePreviewActive.setImageURI(imageUri);
            if (adaptateur != null) adaptateur.setImageTempChoisie(imageUri); // aperçu direct dans la liste
        }
    }

    // ---------------- Adaptateur ----------------
    private class AdaptateurCoach extends RecyclerView.Adapter<AdaptateurCoach.ViewHolderCoach> implements android.widget.Filterable {

        private List<Coach> listeAff = new ArrayList<>();
        private List<Coach> listeCompl = new ArrayList<>();
        private Uri imageTempChoisie; // image temporaire sélectionnée

        public void definirCoachs(List<Coach> liste) {
            listeAff.clear();
            listeAff.addAll(liste);
            listeCompl.clear();
            listeCompl.addAll(liste);
            notifyDataSetChanged();
        }

        public void setImageTempChoisie(Uri uri) {
            imageTempChoisie = uri;
            notifyDataSetChanged();
        }

        public void clearImageTemp() { imageTempChoisie = null; }

        @NonNull
        @Override
        public ViewHolderCoach onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vue = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach, parent, false);
            return new ViewHolderCoach(vue);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderCoach holder, int position) {
            holder.lierDonnees(listeAff.get(position));
        }

        @Override
        public int getItemCount() { return listeAff.size(); }

        @Override
        public android.widget.Filter getFilter() { return filtreCoach; }

        private final android.widget.Filter filtreCoach = new android.widget.Filter() {
            @Override
            protected android.widget.Filter.FilterResults performFiltering(CharSequence constraint) {
                List<Coach> listeFiltree = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    listeFiltree.addAll(listeCompl);
                } else {
                    String motif = constraint.toString().toLowerCase(Locale.FRANCE).trim();
                    for (Coach c : listeCompl) {
                        if ((c.getNomComplet() != null && c.getNomComplet().toLowerCase(Locale.FRANCE).contains(motif))
                                || (c.getDescription() != null && c.getDescription().toLowerCase(Locale.FRANCE).contains(motif))
                                || (c.getSpecialites() != null && c.getSpecialites().toString().toLowerCase(Locale.FRANCE).contains(motif))) {
                            listeFiltree.add(c);
                        }
                    }
                }
                android.widget.Filter.FilterResults result = new android.widget.Filter.FilterResults();
                result.values = listeFiltree;
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint, android.widget.Filter.FilterResults results) {
                listeAff.clear();
                if (results.values != null) {
                    //noinspection unchecked
                    listeAff.addAll((List<Coach>) results.values);
                }
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
                btnModifier = itemView.findViewById(R.id.btnModifier);
                btnSupprimer = itemView.findViewById(R.id.btnSupprimer);
            }

            public void lierDonnees(Coach coach) {
                txtNom.setText(coach.getNomComplet());
                txtDescription.setText(coach.getDescription());
                txtNote.setText(String.format(Locale.FRANCE, "%.1f (%d avis)", coach.getRating(), coach.getReviewCount()));
                txtNbSeances.setText(coach.getSessionCount() + " séances");

                if (imageTempChoisie != null) {
                    imgProfil.setImageURI(imageTempChoisie);
                } else {
                    Glide.with(itemView.getContext())
                            .load(coach.getPhotoUrl())
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .into(imgProfil);
                }

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
        imagePreviewActive = vueDialog.findViewById(R.id.imagePreview1);

        inputNom.setText(coach.getNomComplet());
        inputBio.setText(coach.getDescription());
        if (coach.getSpecialites() != null) {
            inputSpecialites.setText(String.join(",", coach.getSpecialites()));
        }
        Glide.with(getContext())
                .load(coach.getPhotoUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imagePreviewActive);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());

        btnChoisirImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnModifier.setOnClickListener(v -> {
            String nom = inputNom.getText().toString().trim();
            String desc = inputBio.getText().toString().trim();
            String specs = inputSpecialites.getText().toString().trim();

            if (nom.isEmpty()) {
                Toast.makeText(getContext(), "Nom obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            coach.setNom(nom);
            coach.setDescription(desc);
            coach.setSpecialites(specs.isEmpty() ? new ArrayList<>() : Arrays.asList(specs.split(",")));

            if (imageUri != null) {
                uploadImageToFirebase(imageUri, downloadUrl -> {
                    if (downloadUrl != null) coach.setPhotoUrl(downloadUrl);
                    int index = listeCompleteCoachs.indexOf(coach);
                    if (index != -1) adaptateur.notifyItemChanged(index);
                    dialog.dismiss();
                    imageUri = null;
                });
            } else {
                int index = listeCompleteCoachs.indexOf(coach);
                if (index != -1) adaptateur.notifyItemChanged(index);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
