package com.example.fitgym.data.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitgym.data.dao.DAOCoach;
import com.example.fitgym.data.model.Coach;

import java.util.List;

/**
 * Ce Repository gère la communication avec la base de données LOCALE (SQLite)
 * en utilisant le DAOCoach.
 */
public class CoachRepository {

    private DAOCoach daoCoach;



    /**
     * Récupère un coach par son ID (String) de manière efficace.
     * @param id L'ID du coach (doit être un String)
     */
    public Coach obtenirCoachParId(String id) {
        // CORRECTION: On n'utilise pas la boucle.
        // On appelle directement le DAO, qui fait une requête SQL optimisée.
        return daoCoach.obtenirCoachParId(id);
    }

    /**
     * Ajoute un coach à la base locale.
     * (Rendu public pour être accessible par le ViewModel)
     */
    public long ajouterCoach(Coach coach) {
        return daoCoach.ajouterCoach(coach);
    }

    /**
     * Modifie un coach dans la base locale.
     */
    public int modifierCoach(Coach coach) {
        return daoCoach.modifierCoach(coach);
    }

    /**
     * Supprime un coach de la base locale par son ID (String).
     * @param id L'ID du coach (doit être un String)
     */
    public int supprimerCoach(String id) {
        // CORRECTION: Le paramètre est maintenant un String
        return daoCoach.supprimerCoach((id));
    }

    /**
     * Liste tous les coachs de la base locale.
     */
    public List<Coach> listerCoachs() {
        return daoCoach.listerCoachs();
    }
}