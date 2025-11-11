package com.example.fitgym.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.fitgym.data.model.Coach;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// ❌ DO NOT EXTEND SQLiteOpenHelper
public class DAOCoach {

    private static final String TABLE_COACH = "Coach";

    // ✅ Store the database, don't create it
    private SQLiteDatabase db;

    // ✅ Constructor now takes the database from DatabaseHelper
    public DAOCoach(SQLiteDatabase db) {
        this.db = db;
    }

    // --- FONCTIONS D'AIDE pour convertir Liste <-> String ---
    private String convertirListeEnString(List<String> liste) {
        if (liste == null || liste.isEmpty()) {
            return "";
        }
        return String.join(",", liste);
    }

    private List<String> convertirStringEnListe(String texte) {
        if (texte == null || texte.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(texte.split(",")));
    }


    public long ajouterCoach(Coach coach) {
        // ❌ Don't call getWritableDatabase()
        ContentValues values = new ContentValues();

        values.put("nom", coach.getNom());
        values.put("photo_url", coach.getPhotoUrl());
        values.put("contact", coach.getContact());
        values.put("description", coach.getDescription());
        values.put("rating", coach.getRating());
        values.put("review_count", coach.getReviewCount());
        values.put("session_count", coach.getSessionCount());
        values.put("specialites", convertirListeEnString(coach.getSpecialites()));

        // ✅ Use the 'db' variable directly
        long id = db.insert(TABLE_COACH, null, values);
        // ❌ Don't call db.close() here! Let the helper manage it.
        return id;
    }

    public int modifierCoach(Coach coach) {
        ContentValues values = new ContentValues();
        values.put("nom", coach.getNom());
        values.put("photo_url", coach.getPhotoUrl());
        values.put("description", coach.getDescription());
        values.put("rating", coach.getRating());
        values.put("review_count", coach.getReviewCount());
        values.put("session_count", coach.getSessionCount());
        values.put("specialites", convertirListeEnString(coach.getSpecialites()));

        int rows = db.update(TABLE_COACH, values, "id = ?", new String[]{String.valueOf(coach.getId())});
        return rows;
    }

    public int supprimerCoach(String id) {
        int rows = db.delete(TABLE_COACH, "id = ?", new String[]{id});
        return rows;
    }

    public List<Coach> listerCoachs() {
        List<Coach> liste = new ArrayList<>();
        // ✅ Use the 'db' variable directly
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COACH, null);

        if (cursor.moveToFirst()) {
            do {
                Coach c = construireCoachDepuisCursor(cursor);
                liste.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // ❌ Don't call db.close()
        return liste;
    }

    public Coach obtenirCoachParId(String id) {
        Coach coach = null;
        Cursor cursor = db.query(TABLE_COACH,
                null, "id = ?", new String[]{id},
                null, null, null);

        if (cursor.moveToFirst()) {
            coach = construireCoachDepuisCursor(cursor);
        }
        cursor.close();
        return coach;
    }

    private Coach construireCoachDepuisCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndexOrThrow("id");
        int nomIndex = cursor.getColumnIndexOrThrow("nom");
        int photoUrlIndex = cursor.getColumnIndexOrThrow("photo_url");
        int contactIndex = cursor.getColumnIndexOrThrow("contact");
        int descriptionIndex = cursor.getColumnIndexOrThrow("description");
        int ratingIndex = cursor.getColumnIndexOrThrow("rating");
        int reviewCountIndex = cursor.getColumnIndexOrThrow("review_count");
        int sessionCountIndex = cursor.getColumnIndexOrThrow("session_count");
        int specialitesIndex = cursor.getColumnIndexOrThrow("specialites");

        Coach c = new Coach();
        c.setId(cursor.getString(idIndex));
        c.setNom(cursor.getString(nomIndex));
        c.setPhotoUrl(cursor.getString(photoUrlIndex));
        c.setContact(cursor.getString(contactIndex));
        c.setDescription(cursor.getString(descriptionIndex));
        c.setRating(cursor.getDouble(ratingIndex));
        c.setReviewCount(cursor.getInt(reviewCountIndex));
        c.setSessionCount(cursor.getInt(sessionCountIndex));
        String specialitesEnString = cursor.getString(specialitesIndex);
        c.setSpecialites(convertirStringEnListe(specialitesEnString));

        return c;
    }
    // Dans DAOCoach.java
    public void viderCoachs() {
        db.delete(TABLE_COACH, null, null);
    }
}
