package com.example.fitgym.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitgym.data.model.Coach;
import com.example.fitgym.data.repository.CoachRepository;

import java.util.List;

public class CoachViewModel extends AndroidViewModel {

    private CoachRepository repository;
    private MutableLiveData<List<Coach>> listeCoachs;

    public CoachViewModel(@NonNull Application application) {
        super(application);
        repository = new CoachRepository(application.getApplicationContext());
        listeCoachs = new MutableLiveData<>();
        chargerTousLesCoachs();
    }

    // Getter pour observer la liste
    public LiveData<List<Coach>> getListeCoachs() {
        return listeCoachs;
    }

    // Charger tous les coachs depuis la base
    public void chargerTousLesCoachs() {
        listeCoachs.setValue(repository.listerCoachs());
    }

    // Ajouter un coach
    public void ajouterCoach(Coach coach) {
        repository.ajouterCoach(coach);
        chargerTousLesCoachs();
    }

    // Modifier un coach
    public void modifierCoach(Coach coach) {
        repository.modifierCoach(coach);
        chargerTousLesCoachs();
    }

    // Supprimer un coach
    public void supprimerCoach(int id) {
        repository.supprimerCoach(id);
        chargerTousLesCoachs();
    }

    // Récupérer un coach par ID
    public Coach obtenirCoachParId(int id) {
        return repository.obtenirCoachParId(id);
    }
}
