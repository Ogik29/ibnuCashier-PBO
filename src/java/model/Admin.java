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

public class Admin extends User {
    public Admin(int id, String u, String p, boolean active) {
        super(id, u, p, "ADMIN", active);
    }

    public boolean kelolaPengguna(User pengguna, String aksi) { return true; }
    public boolean kelolaProduk(Product produk, String aksi) { return true; }
    public boolean voidSale(int transaksiID) { return true; }

    @Override
    public List<String> getMenu(int userID) {
        return Arrays.asList("Dashboard", "Approve Opnam", "Approve Faktur");
    }
}