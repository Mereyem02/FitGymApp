package com.example.fitgym.data.model;

import java.util.List; // <-- N'OUBLIEZ PAS CET IMPORT

public class Coach {
    private String id;
    private String nom;
    private String prenom;
    private String photoUrl; // Gardé de votre code
    private String contact;

    private String description;
    private double rating;
    private int reviewCount;
    private int sessionCount;
    private List<String> specialites;


    public Coach() {}

    public Coach(String nom, String prenom, String photoUrl, String contact,
                 String description, double rating, int reviewCount,
                 int sessionCount, List<String> specialites) {
        this.nom = nom;
        this.prenom = prenom;
        this.photoUrl = photoUrl;
        this.contact = contact;
        this.description = description;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.sessionCount = sessionCount;
        this.specialites = specialites;
    }

    // --- Getters et Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }

    public List<String> getSpecialites() { return specialites; }
    public void setSpecialites(List<String> specialites) { this.specialites = specialites; }

    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    public String getIdFirebase() {
        return id; // retourne l'ID Firebase du coach
    }

}