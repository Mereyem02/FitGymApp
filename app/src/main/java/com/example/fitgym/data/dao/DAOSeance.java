package com.example.fitgym.data.dao;

import android.content.Context;

import com.example.fitgym.data.db.DatabaseHelper;
import com.example.fitgym.data.model.Seance;

import java.util.List;

public class DAOSeance {

    private DatabaseHelper dbHelper;

    public DAOSeance(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean ajouterSeance(Seance s) {
        return dbHelper.insertOrUpdateSeance(s);
    }

    public boolean modifierSeance(Seance s) {
        return dbHelper.updateSeance(s);
    }

    public boolean supprimerSeance(String id) {
        return dbHelper.deleteSeance(id);
    }

    public List<Seance> listerSeances() {
        return dbHelper.getAllSeances();
    }

    public void viderSeances() {
        dbHelper.deleteAllSeances();
    }

    public Seance getSeanceParId(String id) {
        return dbHelper.getSeanceById(id);
    }
}
