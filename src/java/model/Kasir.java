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

    public Kasir(int userID, String username, String passwordHash, boolean isActive) {
        super(userID, username, passwordHash, "KASIR", isActive);
    }

    public Transaction buatTransaksi(List<SaleItem> items) {
        // Mengembalikan object Transaksi baru
        Transaction trx = new Transaction();
        trx.setItems(items);
        trx.hitungTotal();
        return trx;
    }

    public boolean bacaFakturPusat(String noFaktur) { return true; }

    @Override
    public List<String> getMenu(int userID) {
        return Arrays.asList("POS (Kasir)", "Cek Stok", "Buat Stok Opnam");
    }
}