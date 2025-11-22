package com.example.fitgym.data.db;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.example.fitgym.data.model.Admin;
import com.example.fitgym.data.model.Categorie;
import com.example.fitgym.data.model.Client;
import com.example.fitgym.data.model.Coach;
import com.example.fitgym.data.model.Seance;
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
    private final DatabaseReference categoriesRef;
    private final DatabaseReference seancesRef;


    public FirebaseHelper() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        adminRef = db.getReference("admins/admin");
        coachsRef = db.getReference("coachs");
        clientsRef = db.getReference("clients");
        categoriesRef = db.getReference("categories");
        seancesRef = db.getReference("seances");
    }
    public void getClientByEmail(String email, ClientCallback callback) {
        clientsRef.orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Client c = ds.getValue(Client.class);
                                if (c != null) c.setId(ds.getKey());
                                callback.onCallback(c);
                                return;
                            }
                        }
                        callback.onCallback(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback(null);
                    }
                });
    }




    public interface ClientCallback { void onCallback(Client client); }

    public void getClientById(String uid, ClientCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onCallback(null);
            return;
        }

        clientsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Client client = snapshot.getValue(Client.class);
                    if (client != null) client.setId(snapshot.getKey());
                    callback.onCallback(client);
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



    // === Callbacks ===
    public interface UpdateCallback { void onComplete(boolean success); }
    public interface AdminCallback { void onCallback(Admin admin); }
    public interface CoachesCallback { void onCallback(List<Coach> coachList); }
    public interface ClientsCallback { void onCallback(List<Client> clientList); }
    public interface PhotoCallback { void onCallback(String photoBase64); }
    public interface CategoriesCallback {
        void onCallback(List<Categorie> categorieList);
    }

    public interface SeancesCallback {
        void onCallback(List<Seance> seanceList);
    }

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
    public void getSeanceById(String seanceId, OnSeanceLoadedListener listener) {
        if (seanceId == null || seanceId.trim().isEmpty()) {
            listener.onSeanceLoaded(null);
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Seances")
                .child(seanceId);

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Seance seance = task.getResult().getValue(Seance.class);
                listener.onSeanceLoaded(seance);
            } else {
                listener.onSeanceLoaded(null);
            }
        });
    }
    public interface OnSeanceLoadedListener {
        void onSeanceLoaded(Seance seance);
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
    public void ajouterSeance(Seance seance, Consumer<Boolean> callback) {
        String key = seance.getId();
        if (key == null || key.isEmpty()) {
            key = seancesRef.push().getKey();
            if (key == null) { callback.accept(false); return; }
            seance.setId(key);
        }
        seancesRef.child(key).setValue(seance)
                .addOnSuccessListener(aVoid -> callback.accept(true))
                .addOnFailureListener(e -> callback.accept(false));
    }


    // --- MODIFIER SEANCE ---
    public void modifierSeance(Seance seance, UpdateCallback callback) {
        if (seance.getId() == null || seance.getId().isEmpty()) { callback.onComplete(false); return; }
        seancesRef.child(seance.getId()).setValue(seance)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // --- SUPPRIMER SEANCE ---
    public void supprimerSeance(String seanceId, UpdateCallback callback) {
        if (seanceId == null || seanceId.isEmpty()) { callback.onComplete(false); return; }
        seancesRef.child(seanceId).removeValue()
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    // --- RECUPERER TOUTES LES SEANCES ---
    public void getAllSeances(SeancesCallback callback) {
        seancesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Seance> list = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Seance se = s.getValue(Seance.class);
                    if (se != null) {
                        se.setId(s.getKey()); // 🔹 garde la key firebase comme id
                        list.add(se);
                    }
                }
                callback.onCallback(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(new ArrayList<>());
            }
        });
    }

    // --- OBTENIR UN COACH PAR ID (utile pour adapter) ---
    public void getCoachById(String coachId, CoachesCallbackSingle callback) {
        // CoachesCallbackSingle : nouvelle interface de callback pour un seul coach (à ajouter)
        coachsRef.child(coachId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Coach c = snapshot.getValue(Coach.class);
                if (c != null) {
                    c.setId(snapshot.getKey());
                    callback.onCallback(c);
                } else callback.onCallback(null);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onCallback(null); }
        });
    }
    public interface CoachesCallbackSingle { void onCallback(Coach coach); }
    // --- OBTENIR UNE CATEGORIE PAR ID ---
    public void getCategorieById(String categorieId, CategoriesCallbackSingle callback) {
        categoriesRef.child(categorieId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Categorie c = snapshot.getValue(Categorie.class);
                if (c != null) {
                    c.setCategorieId(snapshot.getKey());
                    callback.onCallback(c);
                } else callback.onCallback(null);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { callback.onCallback(null); }
        });
    }

    // interface pour catégorie single
    public interface CategoriesCallbackSingle { void onCallback(Categorie categorie); }

    // --- RECUPERER TOUTES LES CATEGORIES ---
    public void getAllCategories(CategoriesCallback callback) {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Categorie> list = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Categorie c = s.getValue(Categorie.class);
                    if (c != null) {
                        c.setCategorieId(s.getKey());
                        list.add(c);
                    }
                }
                callback.onCallback(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback(new ArrayList<>());
            }
        });
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