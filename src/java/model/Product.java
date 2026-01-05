/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author dimas
 */
public class Product {
    private int produkID;
    private String sku;
    private String namaProduk;
    private double hargaBeli;
    private double hargaJual;
    private int stok;

    public Product() {}
    public Product(int id, String s, String n, double hb, double hj, int stok) {
        this.produkID = id;
        this.sku = s;
        this.namaProduk = n;
        this.hargaBeli = hb;
        this.hargaJual = hj;
        this.stok = stok;
    }
    
    public boolean updateHarga(double hargaBaru, Admin admin) {
        if (admin != null) {
            this.hargaJual = hargaBaru;
            return true;
        }
        return false;
    }

    public void setSku(String s) { this.sku = s; }
    public void setNamaProduk(String n) { this.namaProduk = n; }
    public void setHargaBeli(double h) { this.hargaBeli = h; }
    public void setHargaJual(double h) { this.hargaJual = h; }
    public void setStok(int s) { this.stok = s; }
    
    public int getProdukID() { return produkID; }
    public String getSKU() { return sku; }
    public String getNamaProduk() { return namaProduk; }
    public int getStok() { return stok; }
    public double getHargaJual() { return hargaJual; }
    
    @Override
    // Method ini menentukan apakah dua objek dianggap "SAMA" secara logika
    public boolean equals(Object o) {
         // objek dibandingkan dengan dirinya sendiri (alamat memori sama), maka TRUE.
         if (this == o) return true;

         // Jika objek lawan kosong (null) atau beda kelas (misal Product vs User), return FALSE.
         // logic ini mencegah error saat program berjalan
         if (o == null || getClass() != o.getClass()) return false;

         // mengubah objek lawan ('o' yang bertipe Object) menjadi tipe 'Product' 
         // agar bisa mengakses atribut di dalamnya (seperti produkID).
         Product p = (Product) o;

         // Dua produk dianggap sama persis HANYA JIKA 'produkID' mereka sama.
         // (Meskipun nama objek di Java beda, kalau ID database sama, berarti barangnya sama)
         return produkID == p.produkID;
    }

    @Override
    // Method ini menghasilkan kode unik (integer) untuk objek ini
    public int hashCode() { 
        // Menggenerate angka unik berdasarkan produkID.
        // Jika equals() return true, maka hashCode() harus menghasilkan angka yang sama.
        // berfungsi agar pencarian produk di dalam HashMap (Keranjang) menjadi sangat cepat.
        return Integer.hashCode(produkID); 
    }
}