package com.example.fitgym.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitgym.data.db.FirebaseHelper;
import com.example.fitgym.data.model.Coach;

import java.util.ArrayList;
import java.util.List;

public class CoachViewModel extends AndroidViewModel {

    private MutableLiveData<List<Coach>> coachsLiveData = new MutableLiveData<>(new ArrayList<>());
    private FirebaseHelper firebaseHelper;

    public CoachViewModel(@NonNull Application application) {
        super(application);
        firebaseHelper = new FirebaseHelper();
        chargerCoachs();
    }

    // LiveData à observer dans ton fragment
    public LiveData<List<Coach>> getListeCoachs() {
        return coachsLiveData;
    }

    // Charger les coachs depuis Firebase
    private void chargerCoachs() {
        firebaseHelper.getAllCoaches(fireBaseCoachs -> {
            coachsLiveData.postValue(fireBaseCoachs != null ? fireBaseCoachs : new ArrayList<>());
        });
    }

    // Ajouter un coach (UI mise à jour immédiatement)
    public void ajouterCoach(Coach coach) {
        List<Coach> current = coachsLiveData.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(coach);
        coachsLiveData.setValue(current); // Update instantané UI

        // Ajouter sur Firebase
        List<Coach> finalCurrent = current;
        firebaseHelper.ajouterCoach(coach, success -> {
            if (!success) {
                // rollback si nécessaire
                finalCurrent.remove(coach);
                coachsLiveData.postValue(finalCurrent);
            }
        });
    }

    // Supprimer un coach par ID
    public void supprimerCoach(String coachId) {
        List<Coach> current = coachsLiveData.getValue();
        if (current != null) {
            // Retirer le coach correspondant à l'ID
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).getId() != null && current.get(i).getId().equals(coachId)) {
                    current.remove(i);
                    break;
                }
            }
            coachsLiveData.setValue(current);
        }

        // Supprimer sur Firebase
        firebaseHelper.supprimerCoach(coachId, success -> {
            if (!success) {
                // rollback si nécessaire (optionnel)
            }
        });
    }


    // Modifier un coach
    public void modifierCoach(Coach coach) {
        List<Coach> current = coachsLiveData.getValue();
        if (current != null) {
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).getIdFirebase() != null &&
                        current.get(i).getIdFirebase().equals(coach.getIdFirebase())) {
                    current.set(i, coach);
                    break;
                }
            }
            coachsLiveData.setValue(current);
        }
        firebaseHelper.modifierCoach(coach, success -> {});
    }
}
