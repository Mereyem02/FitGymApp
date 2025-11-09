package com.example.fitgym.data.db;

import androidx.annotation.NonNull;

import com.example.fitgym.data.model.Admin;
import com.example.fitgym.data.model.Coach;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private final DatabaseReference dbRef;
    private final DatabaseReference coachesRef;

    public FirebaseHelper() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("admins/admin");
        coachesRef = db.getReference("coaches");
    }

    /** Interface pour les callbacks de mise à jour (ajout, modification, suppression) */
    public interface UpdateCallback {
        void onComplete(boolean success);
    }

    /** Interface pour récupérer l’admin */
    public interface AdminCallback {
        void onCallback(Admin admin);
    }

    /** Interface pour récupérer la liste des coachs */
    public interface CoachesCallback {
        void onCallback(List<Coach> coachList);
    }

    /** Interface pour récupérer la photo de l’admin */
    public interface PhotoCallback {
        void onCallback(String photoBase64);
    }

    // --- AJOUTER COACH ---
    public void ajouterCoach(Coach coach, UpdateCallback callback) {
        String nouveauCoachId = coachesRef.push().getKey();
        if (nouveauCoachId == null) {
            callback.onComplete(false);
            return;
        }

        coach.setId(nouveauCoachId);
        coachesRef.child(nouveauCoachId).setValue(coach)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // --- MODIFIER COACH ---
    public void modifierCoach(Coach coach, UpdateCallback callback) {
        if (coach.getId() == null || coach.getId().isEmpty()) {
            callback.onComplete(false);
            return;
        }
        coachesRef.child(coach.getId()).setValue(coach)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // --- SUPPRIMER COACH ---
    public void supprimerCoach(String coachId, UpdateCallback callback) {
        if (coachId == null || coachId.isEmpty()) {
            callback.onComplete(false);
            return;
        }
        coachesRef.child(coachId).removeValue()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // --- RECUPERER TOUS LES COACHS ---
    public void getAllCoaches(CoachesCallback callback) {
        coachesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Coach> coachList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot coachSnapshot : snapshot.getChildren()) {
                        Coach coach = coachSnapshot.getValue(Coach.class);
                        if (coach != null) {
                            coach.setId(coachSnapshot.getKey()); // Assigner l’ID Firebase
                            coachList.add(coach);
                        }
                    }
                }
                callback.onCallback(coachList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(new ArrayList<>());
            }
        });
    }

    // --- ADMIN ---
    public void getAdmin(AdminCallback callback) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String login = snapshot.child("login").getValue(String.class);
                    String motDePasse = snapshot.child("motDePasse").getValue(String.class);
                    if (login != null && motDePasse != null) {
                        callback.onCallback(new Admin(login, motDePasse));
                    } else {
                        callback.onCallback(null);
                    }
                } else callback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        });
    }

    public void updateAdminEmail(String newEmail, UpdateCallback callback) {
        dbRef.child("login").setValue(newEmail)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void updateAdminPassword(String newPassword, UpdateCallback callback) {
        dbRef.child("motDePasse").setValue(newPassword)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void updateAdminPhoto(String photoBase64, UpdateCallback callback) {
        dbRef.child("photo").setValue(photoBase64)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void getAdminPhoto(PhotoCallback callback) {
        dbRef.child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String photoBase64 = snapshot.getValue(String.class);
                    callback.onCallback(photoBase64);
                } else callback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        });
    }
}
