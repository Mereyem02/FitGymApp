package com.example.fitgym.data.model;

public class Categorie {

    private String categorieId;
    private String nom;
    private String description;

    public Categorie() {}

    public Categorie(String categorieId, String nom, String description) {
        this.categorieId = categorieId;
        this.nom = nom;
        this.description = description;
    }

    public String getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(String categorieId) {
        this.categorieId = categorieId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

