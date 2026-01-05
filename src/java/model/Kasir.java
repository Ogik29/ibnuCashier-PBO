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
import java.util.Arrays;
import java.util.ArrayList;

public class Kasir extends User {
    public Kasir(int id, String u, String p, boolean active) {
        super(id, u, p, "KASIR", active);
    }

    // Diagram behaviors
    public Transaction buatTransaksi(List<SaleItem> items) {
        Transaction trx = new Transaction();
        trx.setItems(items);
        return trx;
    }

    public boolean bacaFakturPusat(String noFaktur) { return true; }
    
    public StockOpnam buatDraftStokOpnam(List<StockOpnamItem> items) {
        StockOpnam opnam = new StockOpnam();
        opnam.setItems(items);
        opnam.setKasirID_creator(this.userID);
        return opnam; 
    }

    @Override
    public List<String> getMenu(int userID) {
        return Arrays.asList("POS", "Cek Stok");
    }
}