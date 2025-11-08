package com.example.fitgym.data.repository;

import android.content.Context;

import com.example.fitgym.data.dao.DAOCoach;
import com.example.fitgym.data.model.Coach;

import java.util.List;

public class CoachRepository {
    private DAOCoach daoCoach;

    public CoachRepository(Context context) {
        daoCoach = new DAOCoach(context);
    }

    public Coach obtenirCoachParId(int id) {
        List<Coach> listeCoachs = daoCoach.listerCoachs();
        if (listeCoachs != null) {
            for (Coach coach : listeCoachs) {
                if (coach.getId() == id) {
                    return coach;
                }
            }
        }
        return null;
    }

    public long ajouterCoach(Coach coach) {
        return daoCoach.ajouterCoach(coach);
    }

    public int modifierCoach(Coach coach) {
        return daoCoach.modifierCoach(coach);
    }

    public int supprimerCoach(int id) {
        return daoCoach.supprimerCoach(id);
    }

    public List<Coach> listerCoachs() {
        return daoCoach.listerCoachs();
    }
}