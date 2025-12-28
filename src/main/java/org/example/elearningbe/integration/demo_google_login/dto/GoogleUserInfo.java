package org.example.elearningbe.integration.demo_google_login.dto;

public class GoogleUserInfo {
    private String sub;
    private String email;
    private boolean emailVerified;
    private String name;
    private String picture;

    public GoogleUserInfo(String sub, String email, boolean emailVerified, String name, String picture) {
        this.sub = sub;
        this.email = email;
        this.emailVerified = emailVerified;
        this.name = name;
        this.picture = picture;
    }

    public String getSub() {
        return sub;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }
}
