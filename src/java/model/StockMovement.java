/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.util.Date;

public class StockMovement {
    private int movementID;
    private int produkID;
    private Date waktu;
    private String tipeMutasi;
    private int qtyChange;
    private String referensiID;
    private int userID;

    public static StockMovement create(int produkID, String tipe, int qty, String ref, int user) {
        StockMovement sm = new StockMovement();
        sm.produkID = produkID;
        sm.tipeMutasi = tipe;
        sm.qtyChange = qty;
        sm.referensiID = ref;
        sm.userID = user;
        sm.waktu = new Date();
        return sm;
    }
    
    public void setTipe(String tipe) { 
        this.tipeMutasi = tipe; 
    }
    
    public void setQty(int qty) { 
        this.qtyChange = qty; 
    }
    
    public void setReferensiID(String referensiID) { 
        this.referensiID = referensiID; 
    }
    
    public void setUserID(int userID) { 
        this.userID = userID; 
    }
}
