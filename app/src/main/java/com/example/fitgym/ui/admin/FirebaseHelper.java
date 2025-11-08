package com.example.fitgym.ui.admin;

import androidx.annotation.NonNull;

import com.example.fitgym.data.model.Admin;
import com.example.fitgymapp.models.Admin;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {

    private final DatabaseReference dbRef;

    public FirebaseHelper() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("admins/admin");
    }

    // Interface pour le callback
    public interface AdminCallback {
        void onCallback(Admin admin);

    }

    // Méthode pour récupérer l'admin
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
                } else {
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        });
    }
    public void updateAdminEmail(String newEmail, UpdateCallback callback) {
        dbRef.child("login").setValue(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true);
                    } else {
                        callback.onComplete(false);
                    }
                });
    }

    // Interface pour le callback de mise à jour
    public interface UpdateCallback {
        void onComplete(boolean success);
    }
    public void updateAdminPassword(String newPassword, UpdateCallback callback) {
        dbRef.child("motDePasse").setValue(newPassword)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }
    // Mettre à jour la photo de l'admin
    public void updateAdminPhoto(String photoBase64, UpdateCallback callback) {
        dbRef.child("photo").setValue(photoBase64)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // Récupérer la photo de l'admin depuis Firebase
    public void getAdminPhoto(PhotoCallback callback) {
        dbRef.child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String photoBase64 = snapshot.getValue(String.class);
                    callback.onCallback(photoBase64);
                } else {
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        });
    }

    // Interface callback pour la photo
    public interface PhotoCallback {
        void onCallback(String photoBase64);
    }

    // Récupérer la photo de l'admin dans un thread séparé
    public void getAdminPhotoAsync(PhotoCallback callback) {
        new Thread(() -> dbRef.child("photo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String photoBase64 = snapshot.getValue(String.class);
                    callback.onCallback(photoBase64);
                } else {
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(null);
            }
        })).start();
    }


}
