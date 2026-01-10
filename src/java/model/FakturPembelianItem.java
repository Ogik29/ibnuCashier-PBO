/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.io.Serializable;

public class FakturPembelianItem implements Serializable {
    private int produkID;
    private int qty;
    private double hargaBeliSatuan;

    public void setStatus(String status) { 
        // Method placeholder sesuai diagram (logika validasi per item)
    }
    
    public void setAdminIDApprover(int id) {
        // Logic pencatatan approver per item jika butuh detail
    }
    
    public void setProdukID(int id) { this.produkID = id; }
    public int getProdukID() { return produkID; }
    public void setQty(int q) { this.qty = q; }
    public int getQty() { return qty; }
    public void setHargaBeliSatuan(double h) { this.hargaBeliSatuan = h; }
    public double getHargaBeliSatuan() { return hargaBeliSatuan; }
}
