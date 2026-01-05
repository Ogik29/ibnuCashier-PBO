/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.util.List;

/**
 *
 * @author dimas
 */

public abstract class User {
    protected int userID;
    protected String username;
    private String passwordHash; 
    protected String role;
    protected boolean isActive;

    public User(int userID, String username, String passwordHash, String role, boolean isActive) {
        this.userID = userID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
    }

    public boolean login(String u, String p) { return true; /* implementasi di DAO/Auth Service */ }
    public abstract List<String> getMenu(int userID); // Parameter int userID

    public void setUsername(String u) { this.username = u; }
    public void setPasswordHash(String p) { this.passwordHash = p; }
    public void setRole(String r) { this.role = r; }

    public int getUserID() { return userID; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
}