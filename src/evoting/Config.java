/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoting;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author DZAKY
 */
public class Config {
    private static Connection mysqlconfig;
    
    public static Connection configDB() throws SQLException {
        try {
            // URL Database (Sesuaikan nama database: db_evoting)
            String url = "jdbc:mysql://localhost:3306/db_evoting"; 
            String user = "root"; // username bawaan XAMPP
            String pass = "";     // password bawaan XAMPP (kosong)
            
            // Mendaftarkan driver MySQL
            DriverManager.registerDriver(new com.mysql.jdbc.Driver()); // atau com.mysql.cj.jdbc.Driver untuk versi baru
            mysqlconfig = DriverManager.getConnection(url, user, pass);            
        } catch (Exception e) {
            System.err.println("Koneksi Gagal: " + e.getMessage()); // Tampil di output (konsol)
            JOptionPane.showMessageDialog(null, "Koneksi Database Gagal! Pastikan MySQL menyala."); // Tampil sebagai pop-up
        }
        return mysqlconfig;
    }
    
    public static boolean sesiVotingBuka = false;
}
