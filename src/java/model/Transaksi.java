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
import java.util.List;

public class Transaksi {
    private int transaksiID;
    private String noStruk;
    private Date waktu;
    private double total;
    private String metodeBayar;
    protected List<SaleItem> items;
    private int kasirID;

    public double hitungTotal() {
        double hitung = 0;
        if(items != null) {
            for(SaleItem item : items) {
                hitung += item.getSubtotal();
            }
        }
        this.total = hitung;
        return total;
    }

    public void cetakStruk() {
        System.out.println("Cetak Struk: " + noStruk);
    }
    
    public void setNoStruk(String noStruk) { 
        this.noStruk = noStruk; 
    }
    
    public void setKasirID(int kasirID) { 
        this.kasirID = kasirID; 
    }
}