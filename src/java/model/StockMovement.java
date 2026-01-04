/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
import java.sql.Timestamp;

public class StockMovement {
    private int movementID;
    private int produkID;
    private Timestamp waktu;
    private String tipeMutasi;
    private int qtyChange;
    private String referensiID;
    private int userID;

    public static StockMovement create(int prodID, String tipe, int qty, String ref, int uid) {
        StockMovement sm = new StockMovement();
        sm.produkID = prodID;
        sm.tipeMutasi = tipe;
        sm.qtyChange = qty;
        sm.referensiID = ref;
        sm.userID = uid;
        sm.waktu = new Timestamp(System.currentTimeMillis());
        return sm;
    }
}
