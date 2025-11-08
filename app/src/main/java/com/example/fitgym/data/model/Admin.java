package com.example.fitgym.data.model;


public class Admin {
    private String login;
    private String motDePasse;
    private byte[] photo;

    public Admin() {}

    public Admin(String login, String motDePasse) {
        this.login = login;
        this.motDePasse = motDePasse;
    }

    public Admin(String login, String motDePasse, byte[] image) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.photo = image;
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }
}
