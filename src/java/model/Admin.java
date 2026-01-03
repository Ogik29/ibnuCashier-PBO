/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.util.Arrays;
import java.util.List;

public class Admin extends User {
    public Admin(int userID, String username, boolean isActive) {
        super(userID, username, "ADMIN", isActive);
    }

    public boolean kelolaPengguna(User pengguna, String aksi) { 
        return true; 
    }
    
    public boolean kelolaProduk(Produk produk, String aksi) { 
        return true; 
    }
    
    public boolean voidSale(int transaksiID) { 
        return true; 
    }

    @Override
    public List<String> getMenu(int userID) {
        return Arrays.asList("Kelola Pengguna", "Master Produk", "Laporan");
    }
}
