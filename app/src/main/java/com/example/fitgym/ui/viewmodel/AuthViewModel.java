package com.example.fitgym.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.fitgym.data.model.Client;
import com.example.fitgym.data.repository.ClientRepository;

public class AuthViewModel extends AndroidViewModel {

    private ClientRepository clientRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        clientRepository = new ClientRepository(application);
    }

    public boolean login(String email, String password) {
        Client client = clientRepository.obtenirClientParEmail(email);
        if (client != null) {
            return client.getMotDePasse().equals(password);
        }
        return false;
    }
}
