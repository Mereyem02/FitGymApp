package com.example.fitgym.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitgym.data.model.Coach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAOCoach {

    private static final String TABLE_COACH = "Coach";
    private SQLiteDatabase db;

    public DAOCoach(SQLiteDatabase db) {
        this.db = db;
    }

    // --- Aide conversion list <-> string ---
    private String convertirListeEnString(List<String> liste) {
        if (liste == null || liste.isEmpty()) return "";
        return String.join(",", liste);
    }

    private List<String> convertirStringEnListe(String texte) {
        if (texte == null || texte.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(texte.split(",")));
    }

    // --- Ajout ---
    public long ajouterCoach(Coach coach) {
        if (coach.getId() == null || coach.getId().trim().isEmpty()) return 0;

        if (!db.isOpen()) {
            throw new IllegalStateException("DB fermée ! Assure-toi qu'elle est ouverte avant d'écrire.");
        }

        ContentValues values = new ContentValues();
        values.put("id", coach.getId());
        values.put("nom", coach.getNomComplet());
        values.put("specialites", coach.getSpecialites() != null ? String.join(",", coach.getSpecialites()) : "");
        values.put("photo_url", coach.getPhotoUrl());
        values.put("contact", coach.getContact());
        values.put("description", coach.getDescription());
        values.put("rating", coach.getRating());
        values.put("review_count", coach.getReviewCount());
        values.put("session_count", coach.getSessionCount());

        // Un seul insert suffit
        return db.insertWithOnConflict(TABLE_COACH, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }



    // ✅ Suppression d’un coach sans ID (secours)
    public void supprimerCoachSansId(String nomComplet) {
        db.delete("Coach", "nom = ?", new String[]{nomComplet});
    }


    // --- Modification ---
    public int modifierCoach(Coach coach) {
        ContentValues values = new ContentValues();
        values.put("nom", coach.getNom());
        values.put("photo_url", coach.getPhotoUrl());
        values.put("contact", coach.getContact());
        values.put("description", coach.getDescription());
        values.put("rating", coach.getRating());
        values.put("review_count", coach.getReviewCount());
        values.put("session_count", coach.getSessionCount());
        values.put("specialites", convertirListeEnString(coach.getSpecialites()));

        String id = coach.getId();
        if (id == null) return 0;
        return db.update(TABLE_COACH, values, "id = ?", new String[]{id});
    }

    // --- Suppression ---
    public int supprimerCoach(String id) {
        if (id == null) return 0;
        return db.delete(TABLE_COACH, "id = ?", new String[]{id});
    }

    // --- Lister ---
    public List<Coach> listerCoachs() {
        List<Coach> liste = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COACH, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Coach c = construireCoachDepuisCursor(cursor);
                    liste.add(c);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return liste;
    }

    // --- Obtenir par ID ---
    public Coach obtenirCoachParId(String id) {
        if (id == null) return null;
        Coach coach = null;
        Cursor cursor = db.query(TABLE_COACH, null, "id = ?", new String[]{id}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                coach = construireCoachDepuisCursor(cursor);
            }
            cursor.close();
        }
        return coach;
    }

    private Coach construireCoachDepuisCursor(Cursor cursor) {
        Coach c = new Coach();
        int idx;

        idx = cursor.getColumnIndex("id");
        if (idx != -1) c.setId(cursor.getString(idx));

        idx = cursor.getColumnIndex("nom");
        if (idx != -1) c.setNom(cursor.getString(idx));

        idx = cursor.getColumnIndex("photo_url");
        if (idx != -1) c.setPhotoUrl(cursor.getString(idx));

        idx = cursor.getColumnIndex("contact");
        if (idx != -1) c.setContact(cursor.getString(idx));

        idx = cursor.getColumnIndex("description");
        if (idx != -1) c.setDescription(cursor.getString(idx));

        idx = cursor.getColumnIndex("rating");
        if (idx != -1) c.setRating(cursor.getDouble(idx));

        idx = cursor.getColumnIndex("review_count");
        if (idx != -1) c.setReviewCount(cursor.getInt(idx));

        idx = cursor.getColumnIndex("session_count");
        if (idx != -1) c.setSessionCount(cursor.getInt(idx));

        idx = cursor.getColumnIndex("specialites");
        if (idx != -1) c.setSpecialites(convertirStringEnListe(cursor.getString(idx)));

        return c;
    }

    // --- Vider table ---
    public void viderCoachs() {
        db.delete(TABLE_COACH, null, null);
    }
}
