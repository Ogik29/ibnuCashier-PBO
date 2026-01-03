/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */

// Abstraction: User tidak bisa dibuat objeknya, harus lewat child (Kasir/Admin)
import java.util.List;

public abstract class User {
    protected int userID;
    protected String username;
    protected String passwordHash;
    protected String role;
    protected boolean isActive;

    public User(int userID, String username, String role, boolean isActive) {
        this.userID = userID;
        this.username = username;
        this.role = role;
        this.isActive = isActive;
    }

    public boolean login(String username, String password) {
        // representasi behavior objek
        return this.username.equals(username); 
    }
    
    public void logout() {
        // Logic cleanup session ada di Servlet
    }
    
    // Abstract method untuk ambil menu sesuai role
    public abstract List<String> getMenu(int userID);

    public int getUserID() { 
        return userID; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public String getRole() { 
        return role; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }
    
    public void setRole(String role) { 
        this.role = role; 
    }
}