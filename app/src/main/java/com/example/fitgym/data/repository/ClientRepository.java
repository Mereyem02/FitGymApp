package com.example.fitgym.data.repository;

import android.content.Context;

import com.example.fitgym.data.dao.DAOClient;
import com.example.fitgym.data.model.Client;

import java.util.List;

public class ClientRepository {

    private DAOClient daoClient;

    public ClientRepository(Context context) {
        daoClient = new DAOClient(context);
    }

    // ------------------------------
    //    AJOUTER CLIENT
    // ------------------------------
    public long ajouterClient(Client client) {
        return daoClient.ajouterClient(client);
    }

    // ------------------------------
    //    LOGIN : OBTENIR CLIENT PAR EMAIL
    // ------------------------------
    public Client obtenirClientParEmail(String email) {
        return daoClient.obtenirClientParEmail(email);
    }

    // ------------------------------
    //    OBTENIR CLIENT PAR ID
    // ------------------------------
    public Client obtenirClientParId(int id) {
        return daoClient.obtenirClientParId(id);
    }

    // ------------------------------
    //    LISTER TOUS LES CLIENTS
    // ------------------------------
    public List<Client> listerClients() {
        return daoClient.listerClients();
    }


    // ------------------------------
    //    MODIFIER CLIENT
    // ------------------------------
    public int modifierClient(Client client) {
        return daoClient.modifierClient(client);
    }

    // ------------------------------
    //    SUPPRIMER CLIENT
    // ------------------------------
    public int supprimerClient(int id) {
        return daoClient.supprimerClient(id);
    }
}
