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

public class StockOpnam implements Approvable {
    private int opnamID;
    private Timestamp waktu;
    private String status;
    private List<StockOpnamItem> items;
    private int kasirID_creator;
    private int adminID_approver;

    @Override
    public boolean approve(Admin admin) {
        if(admin != null && "DRAFT".equals(this.status)) {
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

    public boolean postChanges() {
        // Logic: Memposting perubahan.
        // Di MVC Real: Status berubah jadi 'POSTED' -> Flag buat DAO update stok.
        if ("APPROVED".equals(this.status)) {
            this.status = "POSTED";
            return true;
        }
        return false;
    }
    
    // Getters Setters
    public void setItems(List<StockOpnamItem> items) { this.items = items; }
    public List<StockOpnamItem> getItems() { return items; }
    public void setKasirID_creator(int id) { this.kasirID_creator = id; }
}