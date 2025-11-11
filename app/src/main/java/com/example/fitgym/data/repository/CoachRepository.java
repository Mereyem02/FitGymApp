package com.example.fitgym.data.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitgym.data.dao.DAOCoach;
import com.example.fitgym.data.db.DatabaseHelper;
import com.example.fitgym.data.model.Coach;

import java.util.List;

public class CoachRepository {

    private DAOCoach daoCoach;

    // Constructeur : initialise DAO avec DatabaseHelper
    public CoachRepository(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        daoCoach = new DAOCoach(db);
    }

    public Coach obtenirCoachParId(String id) {
        return daoCoach.obtenirCoachParId(id);
    }

    public long ajouterCoach(Coach coach) {
        return daoCoach.ajouterCoach(coach);
    }

    public int modifierCoach(Coach coach) {
        return daoCoach.modifierCoach(coach);
    }

    public int supprimerCoach(String id) {
        return daoCoach.supprimerCoach(id);
    }

    public List<Coach> listerCoachs() {
        return daoCoach.listerCoachs();
    }
}
