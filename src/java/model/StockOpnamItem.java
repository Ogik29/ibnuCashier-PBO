/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
public class StockOpnamItem {
    private int itemID;
    private int produkID;
    private int qtyFisik;
    private int qtySistem;
    private int selisih;
    private String keterangan;
    private int opnamID;

    public int getSelisih() {
        this.selisih = this.qtyFisik - this.qtySistem;
        return this.selisih;
    }

    public void setProdukID(int id) { this.produkID = id; }
    public void setQtyFisik(int q) { this.qtyFisik = q; }
    public void setQtySistem(int q) { this.qtySistem = q; }
    public void setKeterangan(String k) { this.keterangan = k; }

    public int getProdukID() { return produkID; }
    public int getQtyFisik() { return qtyFisik; }
    public int getQtySistem() { return qtySistem; }
    public String getKeterangan() { return keterangan; }
}
