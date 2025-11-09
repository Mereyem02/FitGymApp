package com.example.fitgym.data.dao;

import static com.example.fitgym.data.db.DatabaseHelper.CREATE_TABLE_COACH;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.fitgym.data.model.Coach;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAOCoach extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fitgym.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_COACH = "Coach";


    public DAOCoach(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // On utilise notre nouvelle requête de création
        db.execSQL(CREATE_TABLE_COACH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si vous changez la structure, cela supprime et recrée la table
        // ATTENTION : Vous perdrez toutes les données existantes
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COACH);
        onCreate(db);
    }

    // --- FONCTIONS D'AIDE pour convertir Liste <-> String ---
    private String convertirListeEnString(List<String> liste) {
        if (liste == null || liste.isEmpty()) {
            return "";
        }
        // Utilise une virgule comme séparateur
        return String.join(",", liste);
    }

    private List<String> convertirStringEnListe(String texte) {
        if (texte == null || texte.isEmpty()) {
            return new ArrayList<>();
        }
        // Sépare le texte par les virgules
        return new ArrayList<>(Arrays.asList(texte.split(",")));
    }


    public long ajouterCoach(Coach coach) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("nom", coach.getNom());
        values.put("prenom", coach.getPrenom());
        values.put("photo_url", coach.getPhotoUrl());
        values.put("contact", coach.getContact());

        // --- AJOUT DES NOUVEAUX CHAMPS ---
        values.put("description", coach.getDescription());
        values.put("rating", coach.getRating());
        values.put("review_count", coach.getReviewCount());
        values.put("session_count", coach.getSessionCount());

        // --- CONVERSION DE LA LISTE ---
        values.put("specialites", convertirListeEnString(coach.getSpecialites()));

        long id = db.insert(TABLE_COACH, null, values);
        db.close();
        return id;
    }

    public int modifierCoach(Coach coach) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("nom", coach.getNom());
        values.put("prenom", coach.getPrenom());
        values.put("photo_url", coach.getPhotoUrl());
        values.put("contact", coach.getContact());

        // --- AJOUT DES NOUVEAUX CHAMPS ---
        values.put("description", coach.getDescription());
        values.put("rating", coach.getRating());
        values.put("review_count", coach.getReviewCount());
        values.put("session_count", coach.getSessionCount());

        // --- CONVERSION DE LA LISTE ---
        values.put("specialites", convertirListeEnString(coach.getSpecialites()));

        int rows = db.update(TABLE_COACH, values, "id = ?", new String[]{String.valueOf(coach.getId())});
        db.close();
        return rows;
    }

    public int supprimerCoach(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // L'argument est maintenant un String
        int rows = db.delete(TABLE_COACH, "id = ?", new String[]{id});
        db.close();
        return rows;
    }

    public List<Coach> listerCoachs() {
        List<Coach> liste = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COACH, null);

        if (cursor.moveToFirst()) {
            do {
                // On utilise la fonction interne pour construire le coach
                Coach c = construireCoachDepuisCursor(cursor);
                liste.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return liste;
    }

    // --- NOUVELLE FONCTION REQUISE PAR LE VIEWMODEL ---
    // Dans DAOCoach.java...
    public Coach obtenirCoachParId(String id) { // Accepte un String
        SQLiteDatabase db = this.getReadableDatabase();
        Coach coach = null;

        Cursor cursor = db.query(TABLE_COACH,
                null,
                "id = ?", // La base de données fait le filtre
                new String[]{id}, // Avec l'ID
                null, null, null);

        if (cursor.moveToFirst()) {
            coach = construireCoachDepuisCursor(cursor);
        }

        cursor.close();
        db.close();
        return coach;
    }


    // --- NOUVELLE FONCTION D'AIDE pour éviter la duplication de code ---
    // (Utilisée par listerCoachs et obtenirCoachParId)
    private Coach construireCoachDepuisCursor(Cursor cursor) {
        // Récupérer les index des colonnes
        int idIndex = cursor.getColumnIndexOrThrow("id");
        int nomIndex = cursor.getColumnIndexOrThrow("nom");
        int prenomIndex = cursor.getColumnIndexOrThrow("prenom");
        int photoUrlIndex = cursor.getColumnIndexOrThrow("photo_url");
        int contactIndex = cursor.getColumnIndexOrThrow("contact");
        int descriptionIndex = cursor.getColumnIndexOrThrow("description");
        int ratingIndex = cursor.getColumnIndexOrThrow("rating");
        int reviewCountIndex = cursor.getColumnIndexOrThrow("review_count");
        int sessionCountIndex = cursor.getColumnIndexOrThrow("session_count");
        int specialitesIndex = cursor.getColumnIndexOrThrow("specialites");

        Coach c = new Coach();

        // Remplir l'objet Coach
        c.setId(cursor.getString(idIndex));
        c.setNom(cursor.getString(nomIndex));
        c.setPrenom(cursor.getString(prenomIndex));
        c.setPhotoUrl(cursor.getString(photoUrlIndex));
        c.setContact(cursor.getString(contactIndex));

        // --- LIRE LES NOUVEAUX CHAMPS ---
        c.setDescription(cursor.getString(descriptionIndex));
        c.setRating(cursor.getDouble(ratingIndex));
        c.setReviewCount(cursor.getInt(reviewCountIndex));
        c.setSessionCount(cursor.getInt(sessionCountIndex));

        // --- RECONVERSION EN LISTE ---
        String specialitesEnString = cursor.getString(specialitesIndex);
        c.setSpecialites(convertirStringEnListe(specialitesEnString));

        return c;
    }
}