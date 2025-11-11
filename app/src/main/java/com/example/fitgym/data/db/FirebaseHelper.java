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

/**
 * FirebaseHelper - gère les opérations Firebase pour Admin et Coach.
 */
public class FirebaseHelper {

    private final DatabaseReference adminRef;
    private final DatabaseReference coachsRef;

    public FirebaseHelper() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        adminRef = db.getReference("admins/admin");
        coachsRef = db.getReference("coachs"); // cohérent avec le reste du projet
    }

    // --- Interfaces de callback ---
    public interface UpdateCallback {
        void onComplete(boolean success);
    }

    public interface AdminCallback {
        void onCallback(Admin admin);
    }

    public interface CoachesCallback {
        void onCallback(List<Coach> coachList);
    }

    public interface PhotoCallback {
        void onCallback(String photoBase64);
    }

    // --- AJOUTER COACH ---
    public void ajouterCoach(Coach coach, UpdateCallback callback) {
        String id = coachsRef.push().getKey();
        if (id == null) {
            callback.onComplete(false);
            return;
        }
        coach.setId(id);
        coachsRef.child(id).setValue(coach)
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    // --- MODIFIER COACH ---
    public void modifierCoach(Coach coach, UpdateCallback callback) {
        if (coach.getId() == null || coach.getId().isEmpty()) {
            callback.onComplete(false);
            return;
        }
        coachsRef.child(coach.getId()).setValue(coach)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // --- SUPPRIMER COACH ---
    public void supprimerCoach(String coachId, UpdateCallback callback) {
        if (coachId == null || coachId.isEmpty()) {
            callback.onComplete(false);
            return;
        }
        coachsRef.child(coachId).removeValue()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // --- RECUPERER TOUS LES COACHS ---
    public void getAllCoaches(CoachesCallback callback) {
        coachsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Coach> coachList = new ArrayList<>();
                for (DataSnapshot coachSnapshot : snapshot.getChildren()) {
                    Coach coach = coachSnapshot.getValue(Coach.class);
                    if (coach != null) {
                        coach.setId(coachSnapshot.getKey());
                        coachList.add(coach);
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
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String login = snapshot.child("login").getValue(String.class);
                    String motDePasse = snapshot.child("motDePasse").getValue(String.class);
                    if (login != null && motDePasse != null) {
                        callback.onCallback(new Admin(login, motDePasse));
                    } else callback.onCallback(null);
                } else callback.onCallback(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        });
    }

    public void updateAdminEmail(String newEmail, UpdateCallback callback) {
        adminRef.child("login").setValue(newEmail)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void updateAdminPassword(String newPassword, UpdateCallback callback) {
        adminRef.child("motDePasse").setValue(newPassword)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void updateAdminPhoto(String photoBase64, UpdateCallback callback) {
        adminRef.child("photo").setValue(photoBase64)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void getAdminPhoto(PhotoCallback callback) {
        adminRef.child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback(snapshot.exists() ? snapshot.getValue(String.class) : null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        });
    }
}
