package com.example.fitgym.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import com.example.fitgym.data.model.Admin;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sport_app.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ADMIN = "Admin";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAdminTable = "CREATE TABLE IF NOT EXISTS " + TABLE_ADMIN + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login TEXT UNIQUE NOT NULL, " +
                "mot_de_passe TEXT NOT NULL)";
        db.execSQL(createAdminTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
        onCreate(db);
    }

    // Récupère un admin spécifique
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

    // Récupère l’unique admin local
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

    // Insère ou met à jour un admin localement
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

    // ✅ Corrigé : Met à jour l’email de l’admin localement
    public void updateAdminEmail(String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("login", newEmail);
        db.update(TABLE_ADMIN, cv, null, null);
        db.close();
    }

    // ✅ Corrigé : Met à jour le mot de passe (colonne correcte)
    public void updateAdminPassword(String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("mot_de_passe", newPassword); // 🔥 correction ici
        db.update(TABLE_ADMIN, cv, null, null);
        db.close();
    }
    public static final String CREATE_TABLE_COACH = "CREATE TABLE IF NOT EXISTS Coach (" +
            "id TEXT PRIMARY KEY," +
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
