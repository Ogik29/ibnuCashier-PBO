/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.util.List;
import java.sql.Timestamp;

public class Transaction {
    private int transaksiID;
    private String noStruk;
    private Timestamp waktu;
    private double total;
    private String metodeBayar;
    protected List<SaleItem> items;
    private int kasirID;

    public double hitungTotal() {
        total = 0;
        if(items != null) for(SaleItem i : items) total += i.getSubtotal();
        return total;
    }
    public void cetakStruk() { System.out.println("Printing..." + noStruk); }
    
    public void setNoStruk(String s) { 
        this.noStruk = s; 
    }
    
    public void setKasirID(int id) { 
        this.kasirID = id; 
    }
    
    public void setItems(List<SaleItem> items) { 
        this.items = items; 
    }
    
    public List<SaleItem> getItems() { 
        return items; 
    }
    
    public double getTotal() { 
        return total; 
    }
    
    public int getKasirID() { 
        return kasirID; 
    }
}
