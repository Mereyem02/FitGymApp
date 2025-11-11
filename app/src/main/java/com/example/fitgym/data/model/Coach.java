package com.example.fitgym.data.model;

import java.util.List;

public class Coach {
    private String id;
    private String nom;
    private String prenom;
    private String photoUrl;
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

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

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
        String p = prenom != null ? prenom.trim() : "";
        String n = nom != null ? nom.trim() : "";
        if (p.isEmpty() && n.isEmpty()) return "";
        if (p.isEmpty()) return n;
        if (n.isEmpty()) return p;
        return p + " " + n;
    }

    public String getIdFirebase() {
        return id;
    }
}
