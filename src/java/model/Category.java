/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
public class Category {
    private int kategoriID;
    private String namaKategori;
    
    public Category(int id, String nama) {
        this.kategoriID = id;
        this.namaKategori = nama;
    }

    public int getKategoriID() { 
        return kategoriID; 
    }
}
