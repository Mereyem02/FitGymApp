package com.example.fitgym.data.db;

// 1. Create a class to hold your constants and helper methods.
public class DBHelper {

    private DBHelper() {
    }

    // --- REQUÊTE DE CRÉATION MISE À JOUR ---
    // Ajout de description, rating, review_count, et session_count
    public static final String CREATE_TABLE_COACH = "CREATE TABLE IF NOT EXISTS Coach (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nom TEXT NOT NULL," +
            "prenom TEXT NOT NULL," +
            "specialites TEXT," +      // Stockera la liste comme "Yoga,Muscu"
            "photo_url TEXT," +
            "contact TEXT," +
            "description TEXT," +     // NOUVEAU
            "rating REAL," +          // NOUVEAU (REAL pour les nombres à virgule)
            "review_count INTEGER," + // NOUVEAU
            "session_count INTEGER" + // NOUVEAU
            ");";

}