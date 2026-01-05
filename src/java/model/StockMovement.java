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

public class StockMovement {
    private int movementID;
    private int produkID;
    private Timestamp waktu;
    private String tipeMutasi;
    private int qtyChange;
    private String referensiID;
    private int userID;

    public StockMovement() {}

    public static StockMovement create(int produkID, String tipe, int qty, String ref, int user) {
        StockMovement sm = new StockMovement();
        sm.produkID = produkID;
        sm.tipeMutasi = tipe;
        sm.qtyChange = qty;
        sm.referensiID = ref;
        sm.userID = user;
        sm.waktu = new Timestamp(System.currentTimeMillis());
        return sm;
    }

    public void setTipe(String tipe) { 
        this.tipeMutasi = tipe; 
    }
    
    public void setQty(int qty) { 
        this.qtyChange = qty; 
    }
    
    public void setReferensiID(String ref) { 
        this.referensiID = ref; 
    }
    
    public void setUserID(int userID) { 
        this.userID = userID; 
    }
    
    // getters
    public int getMovementID() { 
        return movementID; 
    }
    
    public int getProdukID() { 
        return produkID; 
    }
    
    public Timestamp getWaktu() { 
        return waktu; 
    }
    
    public String getTipeMutasi() { 
        return tipeMutasi; 
    }
    
    public int getQtyChange() { 
        return qtyChange; 
    }
    
    public String getReferensiID() { 
        return referensiID; 
    }
    
    public int getUserID() { 
        return userID; 
    }
}
