package com.example.fitgym.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitgym.data.db.DatabaseHelper;
import com.example.fitgym.data.model.Client;

import java.util.ArrayList;
import java.util.List;

public class DAOClient {

    private static SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public DAOClient(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private void open() {
        db = dbHelper.getWritableDatabase();
    }

    private void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // ------------------------------
    //    AJOUTER CLIENT
    // ------------------------------
    public long ajouterClient(Client client) {
        open();
        ContentValues values = new ContentValues();
        values.put("nom", client.getNom());
        values.put("email", client.getEmail());
        values.put("motDePasse", client.getMotDePasse());
        values.put("telephone", client.getTelephone());

        long id = db.insert("Client", null, values);
        close();
        return id;
    }

    // ------------------------------
    //    OBTENIR CLIENT PAR EMAIL
    // ------------------------------
    public Client obtenirClientParEmail(String email) {
        open();
        Client client = null;

        Cursor cursor = db.rawQuery("SELECT * FROM Client WHERE email = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            client = new Client();
            client.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))));
            client.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
            client.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            client.setMotDePasse(cursor.getString(cursor.getColumnIndexOrThrow("motDePasse")));
            client.setTelephone(cursor.getString(cursor.getColumnIndexOrThrow("telephone")));
        }

        cursor.close();
        close();
        return client;
    }

    // ------------------------------
    //    OBTENIR CLIENT PAR ID
    // ------------------------------
    public Client obtenirClientParId(int id) {
        open();
        Client client = null;

        Cursor cursor = db.rawQuery("SELECT * FROM Client WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            client = new Client();
            client.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))));
            client.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
            client.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            client.setMotDePasse(cursor.getString(cursor.getColumnIndexOrThrow("motDePasse")));
            client.setTelephone(cursor.getString(cursor.getColumnIndexOrThrow("telephone")));
        }

        cursor.close();
        close();
        return client;
    }

    // ------------------------------
    //    LISTER TOUS LES CLIENTS
    // ------------------------------
    public  List<Client> listerClients() {
        open();
        List<Client> liste = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM Client", null);

        if (cursor.moveToFirst()) {
            do {
                Client client = new Client();
                client.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))));
                client.setNom(cursor.getString(cursor.getColumnIndexOrThrow("nom")));
                client.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                client.setMotDePasse(cursor.getString(cursor.getColumnIndexOrThrow("motDePasse")));
                client.setTelephone(cursor.getString(cursor.getColumnIndexOrThrow("telephone")));

                liste.add(client);

            } while (cursor.moveToNext());
        }

        cursor.close();
        close();
        return liste;
    }

    // ------------------------------
    //    MODIFIER CLIENT
    // ------------------------------
    public int modifierClient(Client client) {
        open();
        ContentValues values = new ContentValues();
        values.put("nom", client.getNom());
        values.put("email", client.getEmail());
        values.put("motDePasse", client.getMotDePasse());
        values.put("telephone", client.getTelephone());


        int result = db.update("Client", values, "id=?", new String[]{String.valueOf(client.getId())});
        close();
        return result;
    }

    // ------------------------------
    //    SUPPRIMER CLIENT
    // ------------------------------
    public int supprimerClient(int id) {
        open();
        int result = db.delete("Client", "id=?", new String[]{String.valueOf(id)});
        close();
        return result;
    }



}