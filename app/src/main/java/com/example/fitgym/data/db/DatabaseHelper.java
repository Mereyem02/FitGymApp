package com.example.fitgym.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fitgym.data.model.Admin;
import com.example.fitgym.data.model.Categorie;
import com.example.fitgym.data.model.Coach;
import com.example.fitgym.data.model.Seance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "sport_app.db";
    public static final int DATABASE_VERSION = 5;

    private static final String TABLE_ADMIN = "Admin";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Admin table
        String createAdminTable = "CREATE TABLE IF NOT EXISTS " + TABLE_ADMIN + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login TEXT UNIQUE NOT NULL, " +
                "mot_de_passe TEXT NOT NULL)";
        db.execSQL(createAdminTable);

        // Coach table (avec prenom)
        String createCoachTable = "CREATE TABLE IF NOT EXISTS Coach (" +
                "id TEXT PRIMARY KEY," +
                "nom TEXT NOT NULL," +
                "specialites TEXT," +
                "photo_url TEXT," +
                "contact TEXT," +
                "description TEXT," +
                "rating REAL," +
                "review_count INTEGER," +
                "session_count INTEGER" +
                ");";
        db.execSQL(createCoachTable);
        String createClientTable = "CREATE TABLE IF NOT EXISTS Client (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "motDePasse TEXT NOT NULL," +  // <--- ici
                "telephone TEXT" +
                ");";
        db.execSQL(createClientTable);
        String CREATE_TABLE_CATEGORIES = "CREATE TABLE categories (" +
                "categorieId TEXT PRIMARY KEY, " +
                "nom TEXT, " +
                "description TEXT)";
        db.execSQL(CREATE_TABLE_CATEGORIES);

        // Table des séances
        String CREATE_TABLE_SEANCES = "CREATE TABLE IF NOT EXISTS seances (" +
                "id TEXT PRIMARY KEY, " +
                "titre TEXT, " +
                "niveau TEXT, " +
                "date TEXT, " +
                "heure TEXT, " +
                "duree INTEGER, " +
                "prix REAL, " +
                "placesTotales INTEGER, " +
                "placesDisponibles INTEGER, " +
                "description TEXT, " +
                "coachId TEXT, " +
                "categorieId TEXT)";
        db.execSQL(CREATE_TABLE_SEANCES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
        db.execSQL("DROP TABLE IF EXISTS Coach");
        db.execSQL("DROP TABLE IF EXISTS Client");
        db.execSQL("DROP TABLE IF EXISTS seances");
        db.execSQL("DROP TABLE IF EXISTS categories");
        onCreate(db);
    }

    // Admin helpers
    public Admin getAdmin(String login, String motDePasse) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT login, mot_de_passe FROM " + TABLE_ADMIN + " WHERE login=? AND mot_de_passe=?",
                new String[]{login, motDePasse}
        );

        Admin admin = null;
        if (cursor.moveToFirst()) {
            admin = new Admin(
                    cursor.getString(cursor.getColumnIndexOrThrow("login")),
                    cursor.getString(cursor.getColumnIndexOrThrow("mot_de_passe"))
            );
        }
        cursor.close();
        db.close();
        return admin;
    }

    public Admin getAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT login, mot_de_passe FROM " + TABLE_ADMIN + " LIMIT 1", null);

        Admin admin = null;
        if (cursor.moveToFirst()) {
            admin = new Admin(
                    cursor.getString(cursor.getColumnIndexOrThrow("login")),
                    cursor.getString(cursor.getColumnIndexOrThrow("mot_de_passe"))
            );
        }
        cursor.close();
        db.close();
        return admin;
    }

    public void syncAdmin(Admin admin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("login", admin.getLogin());
        values.put("mot_de_passe", admin.getMotDePasse());

        int rows = db.update(TABLE_ADMIN, values, "login=?", new String[]{admin.getLogin()});
        if (rows == 0) {
            db.insert(TABLE_ADMIN, null, values);
        }
        db.close();
    }

    public void updateAdminEmail(String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("login", newEmail);
        db.update(TABLE_ADMIN, cv, null, null);
        db.close();
    }

    public void updateAdminPassword(String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("mot_de_passe", newPassword);
        db.update(TABLE_ADMIN, cv, null, null);
        db.close();
    }
    public boolean insertOrUpdateSeance(Seance s) {
        SQLiteDatabase db = this.getWritableDatabase(); // utilise SQLiteOpenHelper (classe DatabaseHelper)
        ContentValues cv = new ContentValues();
        // si id null on crée un UUID ici (stratégie: id côté client)
        if (s.getId() == null || s.getId().isEmpty()) {
            s.setId(UUID.randomUUID().toString());
        }
        cv.put("id", s.getId());
        cv.put("titre", s.getTitre());
        cv.put("niveau", s.getNiveau());
        cv.put("date", s.getDate());
        cv.put("heure", s.getHeure());
        cv.put("duree", s.getDuree());
        cv.put("prix", s.getPrix());
        cv.put("placesTotales", s.getPlacesTotales());
        cv.put("placesDisponibles", s.getPlacesDisponibles());
        cv.put("description", s.getDescription());
        cv.put("coachId", s.getCoachId());
        cv.put("categorieId", s.getCategorieId());

        long res = db.insertWithOnConflict("seances", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return res != -1;
    }

    // Récupérer toutes les séances (ordre par date+heure)
    public List<Seance> getAllSeances() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM seances ORDER BY date, heure", null);
        List<Seance> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            Seance s = new Seance();
            s.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            s.setTitre(cursor.getString(cursor.getColumnIndexOrThrow("titre")));
            s.setNiveau(cursor.getString(cursor.getColumnIndexOrThrow("niveau")));
            s.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            s.setHeure(cursor.getString(cursor.getColumnIndexOrThrow("heure")));
            s.setDuree(cursor.getInt(cursor.getColumnIndexOrThrow("duree")));
            s.setPrix(cursor.getDouble(cursor.getColumnIndexOrThrow("prix")));
            s.setPlacesTotales(cursor.getInt(cursor.getColumnIndexOrThrow("placesTotales")));
            s.setPlacesDisponibles(cursor.getInt(cursor.getColumnIndexOrThrow("placesDisponibles")));
            s.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            s.setCoachId(cursor.getString(cursor.getColumnIndexOrThrow("coachId")));
            s.setCategorieId(cursor.getString(cursor.getColumnIndexOrThrow("categorieId"))); // utilise la méthode correcte
            list.add(s);
        }
        cursor.close();
        db.close();
        return list;
    }

    // Récupérer une séance par id
    public Seance getSeanceById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM seances WHERE id = ?", new String[]{id});
        Seance s = null;
        if (cursor.moveToFirst()) {
            s = new Seance();
            s.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            s.setTitre(cursor.getString(cursor.getColumnIndexOrThrow("titre")));
            s.setNiveau(cursor.getString(cursor.getColumnIndexOrThrow("niveau")));
            s.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            s.setHeure(cursor.getString(cursor.getColumnIndexOrThrow("heure")));
            s.setDuree(cursor.getInt(cursor.getColumnIndexOrThrow("duree")));
            s.setPrix(cursor.getDouble(cursor.getColumnIndexOrThrow("prix")));
            s.setPlacesTotales(cursor.getInt(cursor.getColumnIndexOrThrow("placesTotales")));
            s.setPlacesDisponibles(cursor.getInt(cursor.getColumnIndexOrThrow("placesDisponibles")));
            s.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            s.setCoachId(cursor.getString(cursor.getColumnIndexOrThrow("coachId")));
            s.setCategorieId(cursor.getString(cursor.getColumnIndexOrThrow("categorieId")));
        }
        cursor.close();
        db.close();
        return s;
    }

    // Supprimer une séance locale par id
    public boolean deleteSeance(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("seances", "id=?", new String[]{id});
        db.close();
        return rows > 0;
    }
    public void deleteAllSeances() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("seances", null, null);
        db.close();
    }


    // Mettre à jour (si tu veux séparer update/insert)
    public boolean updateSeance(Seance s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("titre", s.getTitre());
        cv.put("niveau", s.getNiveau());
        cv.put("date", s.getDate());
        cv.put("heure", s.getHeure());
        cv.put("duree", s.getDuree());
        cv.put("prix", s.getPrix());
        cv.put("placesTotales", s.getPlacesTotales());
        cv.put("placesDisponibles", s.getPlacesDisponibles());
        cv.put("description", s.getDescription());
        cv.put("coachId", s.getCoachId());
        cv.put("categorieId", s.getCategorieId());

        int rows = db.update("seances", cv, "id=?", new String[]{s.getId()});
        db.close();
        return rows > 0;
    }
    // INSÉRER / LIRE CATEGORIES

    public boolean insertOrUpdateCategorie(Categorie c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("categorieId", c.getCategorieId());
        cv.put("nom", c.getNom());
        cv.put("description", c.getDescription());
        long res = db.insertWithOnConflict("categories", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return res != -1;
    }

    public List<Categorie> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM categories", null);
        List<Categorie> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            Categorie c = new Categorie();
            c.setCategorieId(cursor.getString(cursor.getColumnIndexOrThrow("categorieId")));
            c.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
            c.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            list.add(c);
        }
        cursor.close();
        db.close();
        return list;
    }

    public Categorie getCategorieById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM categories WHERE categorieId = ?", new String[]{id});
        Categorie c = null;
        if (cursor.moveToFirst()) {
            c = new Categorie();
            c.setCategorieId(cursor.getString(cursor.getColumnIndexOrThrow("categorieId")));
            c.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
            c.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        }
        cursor.close();
        db.close();
        return c;
    }

    public Coach getCoachById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Coach WHERE id = ?", new String[]{id});
        Coach c = null;
        if (cursor.moveToFirst()) {
            c = new Coach();
            c.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            c.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
            c.setPhotoUrl(cursor.getString(cursor.getColumnIndexOrThrow("photo_url")));
            c.setSpecialites(Collections.singletonList(cursor.getString(cursor.getColumnIndexOrThrow("specialites"))));
            c.setContact(cursor.getString(cursor.getColumnIndexOrThrow("contact")));
            c.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            c.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow("rating")));
            c.setReviewCount(cursor.getInt(cursor.getColumnIndexOrThrow("review_count")));
            c.setSessionCount(cursor.getInt(cursor.getColumnIndexOrThrow("session_count")));
        }
        cursor.close();
        db.close();
        return c;
    }
}
