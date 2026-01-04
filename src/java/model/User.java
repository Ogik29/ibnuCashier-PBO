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

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.passwordHash.equals(password);
    }
    
//    public void logout() { /* Logic logout handle session */ }
    
    public abstract List<String> getMenu(int userID);

    // Getters & Setters
    public int getUserID() { 
        return userID; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public String getPasswordHash() { 
        return passwordHash; 
    }
    
    public String getRole() { 
        return role; 
    }
}