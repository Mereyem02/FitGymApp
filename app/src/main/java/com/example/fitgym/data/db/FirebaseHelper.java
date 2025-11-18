package com.example.fitgym.data.db;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.example.fitgym.data.model.Admin;
import com.example.fitgym.data.model.Client;
import com.example.fitgym.data.model.Coach;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private final DatabaseReference adminRef;
    private final DatabaseReference coachsRef;
    private final DatabaseReference clientsRef;

    public FirebaseHelper() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        adminRef = db.getReference("admins/admin");
        coachsRef = db.getReference("coachs");
        clientsRef = db.getReference("clients");
    }

    // === Callbacks ===
    public interface UpdateCallback { void onComplete(boolean success); }
    public interface AdminCallback { void onCallback(Admin admin); }
    public interface CoachesCallback { void onCallback(List<Coach> coachList); }
    public interface ClientsCallback { void onCallback(List<Client> clientList); }
    public interface PhotoCallback { void onCallback(String photoBase64); }

    // ========================
    // ====== CLIENTS =========
    // ========================
    // Fetch single client by UID
    public void getClient(String clientId, Consumer<Client> callback) {
        if (clientId == null || clientId.isEmpty()) {
            callback.accept(null);
            return;
        }

        clientsRef.child(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Client client = snapshot.getValue(Client.class);
                if (client != null) {
                    client.setId(snapshot.getKey());
                }
                callback.accept(client);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.accept(null);
            }
        });
    }

    public void ajouterClient(Client client, Consumer<Boolean> callback) {
        if (client.getId() == null || client.getId().isEmpty()) {
            callback.accept(false);
            return;
        }
        clientsRef.child(client.getId()).setValue(client)
                .addOnSuccessListener(aVoid -> callback.accept(true))
                .addOnFailureListener(e -> callback.accept(false));
    }

    public void modifierClient(Client client, UpdateCallback callback) {
        if (client.getId() == null || client.getId().isEmpty()) {
            callback.onComplete(false);
            return;
        }
        clientsRef.child(client.getId()).setValue(client)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void supprimerClient(String clientId, UpdateCallback callback) {
        if (clientId == null || clientId.isEmpty()) {
            callback.onComplete(false);
            return;
        }
        clientsRef.child(clientId).removeValue()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void getAllClients(ClientsCallback callback) {
        clientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Client> clientList = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Client client = snap.getValue(Client.class);
                    if (client != null) {
                        client.setId(snap.getKey());
                        clientList.add(client);
                    }
                }
                callback.onCallback(clientList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(new ArrayList<>());
            }
        });
    }

    // ========================
    // ====== COACHS ==========
    // ========================
    public void ajouterCoach(Coach coach, Consumer<Boolean> callback) {
        String key = coachsRef.push().getKey();
        if (key == null) {
            callback.accept(false);
            return;
        }
        coach.setId(key);
        coachsRef.child(key).setValue(coach)
                .addOnSuccessListener(aVoid -> callback.accept(true))
                .addOnFailureListener(e -> callback.accept(false));
    }

    public void modifierCoach(Coach coach, UpdateCallback callback) {
        if (coach.getId() == null || coach.getId().isEmpty()) {
            callback.onComplete(false);
            return;
        }
        coachsRef.child(coach.getId()).setValue(coach)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void supprimerCoach(String coachId, UpdateCallback callback) {
        if (coachId == null || coachId.isEmpty()) {
            callback.onComplete(false);
            return;
        }
        coachsRef.child(coachId).removeValue()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void getAllCoaches(CoachesCallback callback) {
        coachsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Coach> coachList = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Coach coach = snap.getValue(Coach.class);
                    if (coach != null) {
                        coach.setId(snap.getKey());
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

    // ========================
    // ====== ADMIN ===========
    // ========================
    public void getAdmin(AdminCallback callback) {
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String login = snapshot.child("login").getValue(String.class);
                    String motDePasse = snapshot.child("motDePasse").getValue(String.class);
                    if (login != null && motDePasse != null)
                        callback.onCallback(new Admin(login, motDePasse));
                    else callback.onCallback(null);
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
