package com.example.fitgym.data.model;

public class Client {

    private String id;
    private String nom;
    private String email;
    private String motDePasse;
    private String telephone;


    public Client() {}

    public Client(String id, String nom, String email, String password, String telephone) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = password;
        this.telephone = telephone;
    }

    public Client(String nom, String email, String password, String telephone) {
        this.nom = nom;
        this.email = email;
        this.motDePasse = password;
        this.telephone = telephone;

    }


    // Getters
    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }



    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    private boolean synced;

    public boolean isSynced() { return synced; }

    public void setSynced(boolean synced) { this.synced = synced; }



}