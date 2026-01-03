/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kasir extends User {
    public Kasir(int userID, String username, boolean isActive) {
        super(userID, username, "KASIR", isActive);
    }

    public Transaksi buatTransaksi(List<SaleItem> items) {
        return new Transaksi(); // Logic details handled in Controller
    }
    
    public boolean bacaFakturPusat(String noFaktur) { 
        return true; 
    }
    // public StokOpnam buatDraftStokOpnam(List items) { return new StokOpnam(); } 

    @Override
    public List<String> getMenu(int userID) {
        return Arrays.asList("Penjualan (POS)", "Cek Stok");
    }
}