/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.sql.Timestamp;
import java.util.List;
import java.io.Serializable;

public class FakturPembelian implements Approvable, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int fakturID;
    private String noFaktur;
    private Timestamp waktu;
    private String status;
    private List<FakturPembelianItem> items;
    private int adminID_approver;

    @Override
    public boolean approve(Admin admin) {
        if(admin != null) {
            this.status = "APPROVED";
            this.adminID_approver = admin.getUserID();
            return true;
        }
        return false;
    }

    @Override
    public boolean reject(Admin admin) {
        if(admin != null) {
            this.status = "REJECTED";
            this.adminID_approver = admin.getUserID();
            return true;
        }
        return false;
    }

    @Override
    public String getStatus() { return status; }

    // Getters Setters
    public int getFakturID() { 
        return fakturID; 
    }
    
    public String getNoFaktur() { 
        return noFaktur; 
    }
    
    public List<FakturPembelianItem> getItems() { 
        return items; 
    }
    
    public void setItems(List<FakturPembelianItem> items) { 
        this.items = items; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    } 
}
