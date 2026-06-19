/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoting;
import javax.swing.JOptionPane;
import java.awt.CardLayout;

/**
 *
 * @author DZAKY
 */
public class Admin extends javax.swing.JFrame {
    // Variabel untuk melacak menu yang sedang aktif
    String menuAktif = "dashboard";

    /**
     * Creates new form Admin
     */
    public Admin() {
        initComponents();
        this.setLocationRelativeTo(null);
        loadDataDashboard();
        loadDataVoter();
        loadDataKandidat();
        loadDataResult();
        loadDataReport();
        tampilkanStatusSesiAktif();
        
        // Set Dashboard menyala biru di awal
        ubahWarnaMenu("dashboard"); 

        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "dashboard");
        
        // Tes Koneksi Database
        try {
            // Memanggil class Config yang tadi dibuat
            java.sql.Connection conn = Config.configDB();
            if(conn != null){
                 System.out.println("Hore! Database Berhasil Terkoneksi!");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void loadDataDashboard() {
        try {
            java.sql.Connection conn = Config.configDB();
            java.sql.Statement stm = conn.createStatement();
            java.sql.ResultSet res;

            // 1. KARTU TOTAL PEMILIH
            res = stm.executeQuery("SELECT COUNT(*) AS total FROM tb_pemilih");
            if (res.next()) {
                jLabel3.setText(res.getString("total"));
            }

            // 2. KARTU SUDAH MEMILIH
            res = stm.executeQuery("SELECT COUNT(*) AS total FROM tb_pemilih WHERE status_vote = 'Sudah'");
            if (res.next()) {
                jLabel5.setText(res.getString("total"));
            }

            // 3. KARTU BELUM MEMILIH
            res = stm.executeQuery("SELECT COUNT(*) AS total FROM tb_pemilih WHERE status_vote = 'Belum'");
            if (res.next()) {
                jLabel7.setText(res.getString("total"));
            }

            // 4. KARTU TOTAL KANDIDAT
            res = stm.executeQuery("SELECT COUNT(*) AS total FROM tb_kandidat");
            if (res.next()) {
                jLabel9.setText(res.getString("total"));
            }

            // ========================================================
            // 5. UPDATE PROGRESS BAR (MENDUKUNG HINGGA 4 KANDIDAT)
            // ========================================================

            // Bersihkan data awal terlebih dahulu
            lblKandidat1.setText("Kandidat 1: Belum Ada"); progKandidat1.setValue(0);
            lblKandidat2.setText("Kandidat 2: Belum Ada"); progKandidat2.setValue(0);
            lblKandidat3.setText("Kandidat 3: Belum Ada"); progKandidat3.setValue(0);
            lblKandidat4.setText("Kandidat 4: Belum Ada"); progKandidat4.setValue(0); // <-- Reset Kandidat 4

            // A. Cari total keseluruhan suara masuk
            int totalSemuaSuara = 0;
            res = stm.executeQuery("SELECT SUM(jumlah_suara) AS total FROM tb_kandidat");
            if (res.next()) {
                totalSemuaSuara = res.getInt("total");
            }

            // B. Ambil data kandidat - SEKARANG DI-LIMIT JADI 4
            res = stm.executeQuery("SELECT * FROM tb_kandidat ORDER BY no_urut ASC LIMIT 4");

            int urutan = 1;
            while (res.next()) {
                String namaKandidat = res.getString("nama_kandidat");
                int suaraKandidat = res.getInt("jumlah_suara");
                int persentase = 0;

                // Hitung Persentase Real-time
                if (totalSemuaSuara > 0) {
                    persentase = (int) Math.round(((double) suaraKandidat / totalSemuaSuara) * 100);
                }

                // Memasukkan nama dan nilai ke komponen masing-masing
                if (urutan == 1) {
                    lblKandidat1.setText("Kandidat 1: " + namaKandidat);
                    progKandidat1.setValue(persentase);
                } else if (urutan == 2) {
                    lblKandidat2.setText("Kandidat 2: " + namaKandidat);
                    progKandidat2.setValue(persentase);
                } else if (urutan == 3) {
                    lblKandidat3.setText("Kandidat 3: " + namaKandidat);
                    progKandidat3.setValue(persentase);
                } else if (urutan == 4) { // <-- Logika untuk mengisi data grafik Kandidat 4
                    lblKandidat4.setText("Kandidat 4: " + namaKandidat);
                    progKandidat4.setValue(persentase);
                }
                urutan++;
            }

        } catch (Exception e) {
            System.out.println("Gagal memuat data Dashboard: " + e.getMessage());
        }
    }
    
    private void loadDataVoter() {
        // 1. Membuat desain struktur tabel
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
        model.addColumn("NIM");
        model.addColumn("Nama Pemilih");
        model.addColumn("Token");
        model.addColumn("Status Vote");

            try {
                // 2. Query untuk mengambil seluruh data di tabel tb_pemilih
                String sql = "SELECT * FROM tb_pemilih";
                java.sql.Connection conn = Config.configDB();
                java.sql.Statement stm = conn.createStatement();
                java.sql.ResultSet res = stm.executeQuery(sql);

                // 3. Looping (perulangan) untuk membaca data baris demi baris
                while (res.next()) {
                    model.addRow(new Object[]{
                        res.getString("nim"),
                        res.getString("nama_pemilih"),
                        res.getString("token"),
                        res.getString("status_vote")
                    });
                }

                // 4. Memerintahkan JTable untuk menggunakan model yang sudah diisi data
                tblVoter.setModel(model);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal memuat data tabel: " + e.getMessage());
            }
        }

    private void loadDataKandidat() {
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
            model.addColumn("No Urut");
            model.addColumn("Nama Kandidat");
            model.addColumn("Visi");
            model.addColumn("Misi");
            model.addColumn("Path Foto"); // Disembunyikan tidak apa-apa, tapi wajib ada untuk ditarik datanya

            try {
                // Mengambil data diurutkan berdasarkan no urut terkecil
                String sql = "SELECT * FROM tb_kandidat ORDER BY no_urut ASC";
                java.sql.Connection conn = Config.configDB();
                java.sql.Statement stm = conn.createStatement();
                java.sql.ResultSet res = stm.executeQuery(sql);

                while (res.next()) {
                    model.addRow(new Object[]{
                        res.getString("no_urut"),
                        res.getString("nama_kandidat"),
                        res.getString("visi"),
                        res.getString("misi"),
                        res.getString("foto")
                    });
                }
                tblKandidat.setModel(model);

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat data kandidat: " + e.getMessage());
            }
        }
        
    private void ubahWarnaMenu(String menu) {
            // 1. Reset semua label kembali ke warna putih
            navDashboard.setForeground(java.awt.Color.WHITE);
            navVoterManagement.setForeground(java.awt.Color.WHITE);
            navCandidate.setForeground(java.awt.Color.WHITE);
            navResult.setForeground(java.awt.Color.WHITE);
            navReport.setForeground(java.awt.Color.WHITE);
            navSetting.setForeground(java.awt.Color.WHITE);
            navAdmin.setForeground(java.awt.Color.WHITE);
            // (Tambahkan label menu lain di sini jika ada)

            // 2. Ubah warna label yang sedang aktif menjadi biru cerah
            java.awt.Color biruAktif = new java.awt.Color(52, 152, 219);

            if (menu.equals("dashboard")) {
                navDashboard.setForeground(biruAktif);
            } else if (menu.equals("voter")) {
                navVoterManagement.setForeground(biruAktif);
            } else if (menu.equals("kandidat")) {
                navCandidate.setForeground(biruAktif);
            } else if (menu.equals("result")) {
                navResult.setForeground(biruAktif);
            } else if (menu.equals("report")) {
                navReport.setForeground(biruAktif);
            } else if (menu.equals("setting")) {
                navSetting.setForeground(biruAktif);
            } else if (menu.equals("kelolaadmin")){
                navAdmin.setForeground(biruAktif);
            }

            // Update penanda menu aktif
            menuAktif = menu;
        }
    
    private void loadDataResult() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
        model.addColumn("No Urut");
        model.addColumn("Nama Kandidat");
        model.addColumn("Total Suara");
        model.addColumn("Persentase");

        try {
            java.sql.Connection conn = Config.configDB();
            java.sql.Statement stm = conn.createStatement();

            // 1. Cari total KESELURUHAN suara yang masuk (untuk pembagi persentase)
            int totalSemuaSuara = 0;
            java.sql.ResultSet rsTotal = stm.executeQuery("SELECT SUM(jumlah_suara) AS total FROM tb_kandidat");
            if (rsTotal.next()) {
                totalSemuaSuara = rsTotal.getInt("total");
            }

            // 2. Ambil data per kandidat, dan urutkan dari suara terbanyak (DESCENDING)
            String sql = "SELECT * FROM tb_kandidat ORDER BY jumlah_suara DESC";
            java.sql.ResultSet res = stm.executeQuery(sql);

            // Class untuk memformat angka agar persentase rapi (contoh: 45.52 %)
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

            while (res.next()) {
                int suaraKandidat = res.getInt("jumlah_suara");
                String persentase = "0 %";

                // 3. Rumus Persentase: (Suara Kandidat / Total Semua Suara) * 100
                if (totalSemuaSuara > 0) {
                    double hitungPersen = ((double) suaraKandidat / totalSemuaSuara) * 100;
                    persentase = df.format(hitungPersen) + " %";
                }

                model.addRow(new Object[]{
                    res.getString("no_urut"),
                    res.getString("nama_kandidat"),
                    suaraKandidat,
                    persentase
                });
            }

            // Memasukkan data ke tabel
            tblResult.setModel(model);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat hasil suara: " + e.getMessage());
        }
    }
    
    private void loadDataReport() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
        model.addColumn("No Urut");
        model.addColumn("Nama Kandidat");
        model.addColumn("Total Suara");
        model.addColumn("Persentase");

        try {
            java.sql.Connection conn = Config.configDB();
            java.sql.Statement stm = conn.createStatement();

            // Cari total semua suara
            int totalSemuaSuara = 0;
            java.sql.ResultSet rsTotal = stm.executeQuery("SELECT SUM(jumlah_suara) AS total FROM tb_kandidat");
            if (rsTotal.next()) {
                totalSemuaSuara = rsTotal.getInt("total");
            }

            // Ambil data paslon
            java.sql.ResultSet res = stm.executeQuery("SELECT * FROM tb_kandidat ORDER BY jumlah_suara DESC");
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

            while (res.next()) {
                int suaraKandidat = res.getInt("jumlah_suara");
                String persentase = "0 %";

                if (totalSemuaSuara > 0) {
                    double hitungPersen = ((double) suaraKandidat / totalSemuaSuara) * 100;
                    persentase = df.format(hitungPersen) + " %";
                }

                model.addRow(new Object[]{
                    res.getString("no_urut"),
                    res.getString("nama_kandidat"),
                    suaraKandidat,
                    persentase
                });
            }
            tblReport.setModel(model);
            
            // --- KODE UNTUK MENGISI MINI PREVIEW ---
            StringBuilder preview = new StringBuilder();
            preview.append("=== LAPORAN E-VOTING ===\n\n");

            // Mengambil data baris pertama (Pemenang Sementara)
            if (tblReport.getRowCount() > 0) {
                String pemenang = tblReport.getValueAt(0, 1).toString();
                String persenPemenang = tblReport.getValueAt(0, 3).toString();

                preview.append("Kandidat Unggul:\n");
                preview.append("- ").append(pemenang).append("\n");
                preview.append("- Perolehan: ").append(persenPemenang).append("\n\n");
            }

            preview.append("Total Suara Masuk: ").append(totalSemuaSuara).append("\n");
            preview.append("======================\n");
            preview.append("Status: Menunggu Cetak");

            txtPreview.setText(preview.toString());

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat data laporan: " + e.getMessage());
        }
    }
    
    private void loadDataAdmin() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
        model.addColumn("Username Admin");
        model.addColumn("Password");

        try {
            String sql = "SELECT * FROM tb_admin";
            java.sql.Connection conn = Config.configDB();
            java.sql.Statement stm = conn.createStatement();
            java.sql.ResultSet res = stm.executeQuery(sql);

            while (res.next()) {
                model.addRow(new Object[]{
                    res.getString("username"),
                    res.getString("password")
                });
            }
            tblAdmin.setModel(model);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat data admin: " + e.getMessage());
        }
    }
    
    private void tampilkanStatusSesiAktif() {
        try {
            java.sql.Connection conn = Config.configDB();
            String sql = "SELECT status_sesi FROM tb_setting WHERE id = 1";
            java.sql.Statement stm = conn.createStatement();
            java.sql.ResultSet res = stm.executeQuery(sql);

            if (res.next()) {
                String status = res.getString("status_sesi");
                if (status.equals("Buka")) {
                    lblStatusSesi.setText("Status E-Voting: DIBUKA");
                    lblStatusSesi.setForeground(new java.awt.Color(0, 153, 0));
                } else {
                    lblStatusSesi.setText("Status E-Voting: DITUTUP");
                    lblStatusSesi.setForeground(new java.awt.Color(255, 0, 0));
                }
            }
        } catch (Exception e) {
            System.out.println("Gagal memuat status sesi ke label: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelSidebar = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        BBB6 = new javax.swing.JButton();
        navDashboard = new javax.swing.JLabel();
        navVoterManagement = new javax.swing.JLabel();
        navCandidate = new javax.swing.JLabel();
        navResult = new javax.swing.JLabel();
        navSetting = new javax.swing.JLabel();
        navReport = new javax.swing.JLabel();
        navAdmin = new javax.swing.JLabel();
        PanelUtama = new javax.swing.JPanel();
        PanelVoter = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVoter = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtNim = new javax.swing.JTextField();
        txtNama = new javax.swing.JTextField();
        txtToken = new javax.swing.JTextField();
        btnGenerate = new javax.swing.JButton();
        btnUpdateVoter = new javax.swing.JButton();
        btnSaveVoter = new javax.swing.JButton();
        btnDeleteVoter = new javax.swing.JButton();
        btnClearVoter = new javax.swing.JButton();
        PanelKandidat = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblKandidat = new javax.swing.JTable();
        jLabel17 = new javax.swing.JLabel();
        txtNoUrut = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtNamaKandidat = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtVisi = new javax.swing.JTextArea();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtMisi = new javax.swing.JTextArea();
        lblFotoPreview = new javax.swing.JLabel();
        btnBrowse = new javax.swing.JButton();
        txtPathFoto = new javax.swing.JLabel();
        btnSaveKandidat = new javax.swing.JButton();
        btnUpdateKandidat = new javax.swing.JButton();
        btnDeleteKandidat = new javax.swing.JButton();
        btnClearKandidat = new javax.swing.JButton();
        PanelDashboard = new javax.swing.JPanel();
        cardTotalPemilih = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cardSudahVote = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cardBelumVote = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cardTotalKandidat = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        panelGrafik = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        lblKandidat1 = new javax.swing.JLabel();
        progKandidat1 = new javax.swing.JProgressBar();
        lblKandidat2 = new javax.swing.JLabel();
        progKandidat2 = new javax.swing.JProgressBar();
        lblKandidat3 = new javax.swing.JLabel();
        progKandidat3 = new javax.swing.JProgressBar();
        lblKandidat4 = new javax.swing.JLabel();
        progKandidat4 = new javax.swing.JProgressBar();
        PanelResult = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblResult = new javax.swing.JTable();
        btnRefreshResult = new javax.swing.JButton();
        PanelReport = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblReport = new javax.swing.JTable();
        btnPrint = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        txtPreview = new javax.swing.JTextArea();
        PanelSetting = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        btnResetSuara = new javax.swing.JButton();
        btnBukaSesi = new javax.swing.JButton();
        btnTutupSesi = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        lblStatusSesi = new javax.swing.JLabel();
        PanelKelolaAdmin = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tblAdmin = new javax.swing.JTable();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtUserAdmin = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        txtPassAdmin = new javax.swing.JTextField();
        btnSaveAdmin = new javax.swing.JButton();
        btnUpdateAdmin = new javax.swing.JButton();
        btnDeleteAdmin = new javax.swing.JButton();
        btnClearAdmin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PanelSidebar.setBackground(new java.awt.Color(51, 51, 51));

        jLabel1.setFont(new java.awt.Font("Poppins ExtraBold", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("ADMIN PANEL");

        BBB6.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        BBB6.setText("LOG OUT");
        BBB6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BBB6ActionPerformed(evt);
            }
        });

        navDashboard.setFont(new java.awt.Font("Poppins SemiBold", 0, 20)); // NOI18N
        navDashboard.setForeground(new java.awt.Color(255, 255, 255));
        navDashboard.setText("DASHBOARD");
        navDashboard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        navDashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navDashboardMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navDashboardMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                navDashboardMouseExited(evt);
            }
        });

        navVoterManagement.setFont(new java.awt.Font("Poppins SemiBold", 0, 20)); // NOI18N
        navVoterManagement.setForeground(new java.awt.Color(255, 255, 255));
        navVoterManagement.setText("VOTER MANAGEMENT");
        navVoterManagement.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        navVoterManagement.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navVoterManagementMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navVoterManagementMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                navVoterManagementMouseExited(evt);
            }
        });

        navCandidate.setFont(new java.awt.Font("Poppins SemiBold", 0, 20)); // NOI18N
        navCandidate.setForeground(new java.awt.Color(255, 255, 255));
        navCandidate.setText("CANDIDATE");
        navCandidate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        navCandidate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navCandidateMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navCandidateMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                navCandidateMouseExited(evt);
            }
        });

        navResult.setFont(new java.awt.Font("Poppins SemiBold", 0, 20)); // NOI18N
        navResult.setForeground(new java.awt.Color(255, 255, 255));
        navResult.setText("RESULT");
        navResult.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        navResult.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navResultMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navResultMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                navResultMouseExited(evt);
            }
        });

        navSetting.setFont(new java.awt.Font("Poppins SemiBold", 0, 20)); // NOI18N
        navSetting.setForeground(new java.awt.Color(255, 255, 255));
        navSetting.setText("SETTING");
        navSetting.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        navSetting.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navSettingMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navSettingMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                navSettingMouseExited(evt);
            }
        });

        navReport.setFont(new java.awt.Font("Poppins SemiBold", 0, 20)); // NOI18N
        navReport.setForeground(new java.awt.Color(255, 255, 255));
        navReport.setText("REPORT");
        navReport.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        navReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navReportMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navReportMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                navReportMouseExited(evt);
            }
        });

        navAdmin.setFont(new java.awt.Font("Poppins SemiBold", 0, 20)); // NOI18N
        navAdmin.setForeground(new java.awt.Color(255, 255, 255));
        navAdmin.setText("ADMIN MANAGEMENT");
        navAdmin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        navAdmin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navAdminMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navAdminMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                navAdminMouseExited(evt);
            }
        });

        javax.swing.GroupLayout PanelSidebarLayout = new javax.swing.GroupLayout(PanelSidebar);
        PanelSidebar.setLayout(PanelSidebarLayout);
        PanelSidebarLayout.setHorizontalGroup(
            PanelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSidebarLayout.createSequentialGroup()
                .addGroup(PanelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelSidebarLayout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addComponent(BBB6))
                    .addGroup(PanelSidebarLayout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(jLabel1)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(PanelSidebarLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(PanelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(navCandidate)
                    .addComponent(navResult)
                    .addComponent(navSetting)
                    .addComponent(navAdmin)
                    .addComponent(navReport)
                    .addComponent(navDashboard)
                    .addComponent(navVoterManagement))
                .addContainerGap(107, Short.MAX_VALUE))
        );
        PanelSidebarLayout.setVerticalGroup(
            PanelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSidebarLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addGap(68, 68, 68)
                .addComponent(navDashboard)
                .addGap(18, 18, 18)
                .addComponent(navVoterManagement)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(navCandidate)
                .addGap(18, 18, 18)
                .addComponent(navResult)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(navSetting)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(navReport)
                .addGap(18, 18, 18)
                .addComponent(navAdmin)
                .addGap(40, 40, 40)
                .addComponent(BBB6)
                .addGap(92, 92, 92))
        );

        PanelUtama.setBackground(new java.awt.Color(255, 255, 0));
        PanelUtama.setLayout(new java.awt.CardLayout());

        PanelVoter.setBackground(new java.awt.Color(255, 255, 255));

        tblVoter.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        tblVoter.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "NIM", "Nama Pemilih", "Token", "Status Vote"
            }
        ));
        tblVoter.setRowHeight(40);
        tblVoter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblVoterMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblVoter);

        jLabel14.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel14.setText("NIM");

        jLabel15.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel15.setText("TOKEN");

        jLabel16.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel16.setText("NAMA LENGKAP");

        txtNim.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        txtNama.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        txtToken.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        btnGenerate.setBackground(new java.awt.Color(255, 255, 0));
        btnGenerate.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnGenerate.setText("GENERATE");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });

        btnUpdateVoter.setBackground(new java.awt.Color(255, 153, 0));
        btnUpdateVoter.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnUpdateVoter.setText("UPDATE");
        btnUpdateVoter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateVoterActionPerformed(evt);
            }
        });

        btnSaveVoter.setBackground(new java.awt.Color(0, 204, 0));
        btnSaveVoter.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnSaveVoter.setText("SAVE");
        btnSaveVoter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveVoterActionPerformed(evt);
            }
        });

        btnDeleteVoter.setBackground(new java.awt.Color(255, 0, 51));
        btnDeleteVoter.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnDeleteVoter.setText("DELETE");
        btnDeleteVoter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteVoterActionPerformed(evt);
            }
        });

        btnClearVoter.setBackground(new java.awt.Color(255, 255, 102));
        btnClearVoter.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnClearVoter.setText("CLEAR");
        btnClearVoter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearVoterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelVoterLayout = new javax.swing.GroupLayout(PanelVoter);
        PanelVoter.setLayout(PanelVoterLayout);
        PanelVoterLayout.setHorizontalGroup(
            PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelVoterLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSaveVoter, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel14)
                    .addComponent(jLabel16))
                .addGroup(PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelVoterLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(btnUpdateVoter, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteVoter, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearVoter, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(110, Short.MAX_VALUE))
                    .addGroup(PanelVoterLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PanelVoterLayout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(txtNim, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtToken, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNama, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnGenerate)
                        .addGap(49, 49, 49))))
        );
        PanelVoterLayout.setVerticalGroup(
            PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelVoterLayout.createSequentialGroup()
                .addGap(141, 141, 141)
                .addGroup(PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtNim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtToken, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerate))
                .addGap(92, 92, 92)
                .addGroup(PanelVoterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveVoter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnUpdateVoter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteVoter)
                    .addComponent(btnClearVoter))
                .addGap(311, 311, 311))
            .addGroup(PanelVoterLayout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PanelUtama.add(PanelVoter, "voter");

        PanelKandidat.setBackground(new java.awt.Color(255, 255, 255));

        tblKandidat.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        tblKandidat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "No.Urut", "Nama Kandidat", "Visi", "Misi"
            }
        ));
        tblKandidat.setRowHeight(40);
        tblKandidat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKandidatMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblKandidat);
        if (tblKandidat.getColumnModel().getColumnCount() > 0) {
            tblKandidat.getColumnModel().getColumn(2).setHeaderValue("Visi");
            tblKandidat.getColumnModel().getColumn(3).setHeaderValue("Misi");
        }

        jLabel17.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel17.setText("No Urut");

        txtNoUrut.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel18.setText("NAMA KANDIDAT");

        txtNamaKandidat.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        jLabel19.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel19.setText("VISI");

        jScrollPane3.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        txtVisi.setColumns(20);
        txtVisi.setRows(5);
        jScrollPane3.setViewportView(txtVisi);

        jLabel20.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel20.setText("MISI");

        jScrollPane4.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        txtMisi.setColumns(20);
        txtMisi.setRows(5);
        jScrollPane4.setViewportView(txtMisi);

        lblFotoPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        lblFotoPreview.setPreferredSize(new java.awt.Dimension(150, 160));

        btnBrowse.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnBrowse.setText("Browse");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        txtPathFoto.setText("PATH");

        btnSaveKandidat.setBackground(new java.awt.Color(51, 204, 0));
        btnSaveKandidat.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnSaveKandidat.setText("SAVE");
        btnSaveKandidat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveKandidatActionPerformed(evt);
            }
        });

        btnUpdateKandidat.setBackground(new java.awt.Color(255, 255, 0));
        btnUpdateKandidat.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnUpdateKandidat.setText("UPDATE");
        btnUpdateKandidat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateKandidatActionPerformed(evt);
            }
        });

        btnDeleteKandidat.setBackground(new java.awt.Color(255, 0, 51));
        btnDeleteKandidat.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnDeleteKandidat.setText("DELETE");
        btnDeleteKandidat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteKandidatActionPerformed(evt);
            }
        });

        btnClearKandidat.setBackground(new java.awt.Color(255, 153, 51));
        btnClearKandidat.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnClearKandidat.setText("CLEAR");
        btnClearKandidat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearKandidatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelKandidatLayout = new javax.swing.GroupLayout(PanelKandidat);
        PanelKandidat.setLayout(PanelKandidatLayout);
        PanelKandidatLayout.setHorizontalGroup(
            PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelKandidatLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSaveKandidat)
                    .addGroup(PanelKandidatLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelKandidatLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdateKandidat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteKandidat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearKandidat))
                    .addGroup(PanelKandidatLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNoUrut, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtNamaKandidat)
                                .addComponent(jScrollPane3)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PanelKandidatLayout.createSequentialGroup()
                                .addComponent(lblFotoPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnBrowse)
                                    .addComponent(txtPathFoto))))))
                .addContainerGap(182, Short.MAX_VALUE))
        );
        PanelKandidatLayout.setVerticalGroup(
            PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelKandidatLayout.createSequentialGroup()
                .addContainerGap(99, Short.MAX_VALUE)
                .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelKandidatLayout.createSequentialGroup()
                        .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFotoPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PanelKandidatLayout.createSequentialGroup()
                                .addComponent(btnBrowse)
                                .addGap(18, 18, 18)
                                .addComponent(txtPathFoto)))
                        .addGap(43, 43, 43)
                        .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(txtNoUrut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNamaKandidat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel20)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 504, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(PanelKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveKandidat)
                    .addComponent(btnUpdateKandidat)
                    .addComponent(btnDeleteKandidat)
                    .addComponent(btnClearKandidat))
                .addGap(32, 32, 32))
        );

        PanelUtama.add(PanelKandidat, "kandidat");

        PanelDashboard.setBackground(new java.awt.Color(255, 255, 255));

        cardTotalPemilih.setBackground(new java.awt.Color(52, 152, 219));
        cardTotalPemilih.setPreferredSize(new java.awt.Dimension(211, 202));

        jLabel2.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("TOTAL PEMILIH");

        jLabel3.setFont(new java.awt.Font("Poppins SemiBold", 0, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("0");

        javax.swing.GroupLayout cardTotalPemilihLayout = new javax.swing.GroupLayout(cardTotalPemilih);
        cardTotalPemilih.setLayout(cardTotalPemilihLayout);
        cardTotalPemilihLayout.setHorizontalGroup(
            cardTotalPemilihLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalPemilihLayout.createSequentialGroup()
                .addGroup(cardTotalPemilihLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardTotalPemilihLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel2))
                    .addGroup(cardTotalPemilihLayout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(jLabel3)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        cardTotalPemilihLayout.setVerticalGroup(
            cardTotalPemilihLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalPemilihLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(45, 45, 45)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardSudahVote.setBackground(new java.awt.Color(46, 204, 113));
        cardSudahVote.setPreferredSize(new java.awt.Dimension(211, 202));

        jLabel4.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("SUDAH MEMILIH");

        jLabel5.setFont(new java.awt.Font("Poppins SemiBold", 0, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("0");

        javax.swing.GroupLayout cardSudahVoteLayout = new javax.swing.GroupLayout(cardSudahVote);
        cardSudahVote.setLayout(cardSudahVoteLayout);
        cardSudahVoteLayout.setHorizontalGroup(
            cardSudahVoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardSudahVoteLayout.createSequentialGroup()
                .addGroup(cardSudahVoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardSudahVoteLayout.createSequentialGroup()
                        .addGap(93, 93, 93)
                        .addComponent(jLabel5))
                    .addGroup(cardSudahVoteLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel4)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        cardSudahVoteLayout.setVerticalGroup(
            cardSudahVoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardSudahVoteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(45, 45, 45)
                .addComponent(jLabel5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cardBelumVote.setBackground(new java.awt.Color(231, 76, 60));
        cardBelumVote.setPreferredSize(new java.awt.Dimension(211, 202));

        jLabel6.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("BELUM MEMILIH");

        jLabel7.setFont(new java.awt.Font("Poppins SemiBold", 0, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("0");

        javax.swing.GroupLayout cardBelumVoteLayout = new javax.swing.GroupLayout(cardBelumVote);
        cardBelumVote.setLayout(cardBelumVoteLayout);
        cardBelumVoteLayout.setHorizontalGroup(
            cardBelumVoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBelumVoteLayout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addGroup(cardBelumVoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardBelumVoteLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(30, 30, 30))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardBelumVoteLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(94, 94, 94))))
        );
        cardBelumVoteLayout.setVerticalGroup(
            cardBelumVoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBelumVoteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(44, 44, 44)
                .addComponent(jLabel7)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        cardTotalKandidat.setBackground(new java.awt.Color(155, 89, 182));
        cardTotalKandidat.setPreferredSize(new java.awt.Dimension(211, 202));

        jLabel8.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("TOTAL KANDIDAT");

        jLabel9.setFont(new java.awt.Font("Poppins SemiBold", 0, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("0");

        javax.swing.GroupLayout cardTotalKandidatLayout = new javax.swing.GroupLayout(cardTotalKandidat);
        cardTotalKandidat.setLayout(cardTotalKandidatLayout);
        cardTotalKandidatLayout.setHorizontalGroup(
            cardTotalKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalKandidatLayout.createSequentialGroup()
                .addGroup(cardTotalKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardTotalKandidatLayout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addComponent(jLabel9))
                    .addGroup(cardTotalKandidatLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel8)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        cardTotalKandidatLayout.setVerticalGroup(
            cardTotalKandidatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardTotalKandidatLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(53, 53, 53)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelGrafik.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel10.setText("PEROLEHAN SUARA SEMENTARA");

        lblKandidat1.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        lblKandidat1.setText("Kandidat 1: [Nama Kandidat]");

        progKandidat1.setForeground(new java.awt.Color(255, 51, 51));
        progKandidat1.setStringPainted(true);

        lblKandidat2.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        lblKandidat2.setText("Kandidat 2: [Nama Kandidat]");

        progKandidat2.setForeground(new java.awt.Color(255, 255, 0));
        progKandidat2.setStringPainted(true);

        lblKandidat3.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        lblKandidat3.setText("Kandidat 3: [Nama Kandidat]");

        progKandidat3.setForeground(new java.awt.Color(51, 204, 0));
        progKandidat3.setStringPainted(true);

        lblKandidat4.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        lblKandidat4.setText("Kandidat 4: [Nama Kandidat]");

        progKandidat4.setForeground(new java.awt.Color(0, 51, 204));
        progKandidat4.setStringPainted(true);

        javax.swing.GroupLayout panelGrafikLayout = new javax.swing.GroupLayout(panelGrafik);
        panelGrafik.setLayout(panelGrafikLayout);
        panelGrafikLayout.setHorizontalGroup(
            panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGrafikLayout.createSequentialGroup()
                .addGroup(panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGrafikLayout.createSequentialGroup()
                        .addGap(280, 280, 280)
                        .addComponent(jLabel10))
                    .addGroup(panelGrafikLayout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addGroup(panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblKandidat1)
                            .addComponent(lblKandidat2)
                            .addComponent(lblKandidat3)
                            .addComponent(lblKandidat4))
                        .addGap(27, 27, 27)
                        .addGroup(panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progKandidat2, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(progKandidat1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(progKandidat3, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(progKandidat4, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelGrafikLayout.setVerticalGroup(
            panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGrafikLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addGap(53, 53, 53)
                .addGroup(panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblKandidat1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progKandidat1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKandidat2)
                    .addComponent(progKandidat2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKandidat3, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(progKandidat3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKandidat4, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(progKandidat4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(145, 145, 145))
        );

        javax.swing.GroupLayout PanelDashboardLayout = new javax.swing.GroupLayout(PanelDashboard);
        PanelDashboard.setLayout(PanelDashboardLayout);
        PanelDashboardLayout.setHorizontalGroup(
            PanelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelDashboardLayout.createSequentialGroup()
                .addContainerGap(184, Short.MAX_VALUE)
                .addGroup(PanelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelGrafik, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PanelDashboardLayout.createSequentialGroup()
                        .addComponent(cardTotalPemilih, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardSudahVote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardBelumVote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cardTotalKandidat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(135, 135, 135))
        );
        PanelDashboardLayout.setVerticalGroup(
            PanelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelDashboardLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(PanelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cardTotalKandidat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardBelumVote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardSudahVote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cardTotalPemilih, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(63, 63, 63)
                .addComponent(panelGrafik, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        PanelUtama.add(PanelDashboard, "dashboard");

        PanelResult.setBackground(new java.awt.Color(255, 255, 255));

        jLabel21.setFont(new java.awt.Font("Poppins ExtraBold", 0, 24)); // NOI18N
        jLabel21.setText("REKAPITULASI HASIL SUARA");

        tblResult.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        tblResult.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "No Urut", "Nama Kandidat", "Total Suara", "Persentase"
            }
        ));
        tblResult.setRowHeight(40);
        jScrollPane5.setViewportView(tblResult);

        btnRefreshResult.setBackground(new java.awt.Color(255, 255, 0));
        btnRefreshResult.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        btnRefreshResult.setText("REFRESH");
        btnRefreshResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshResultActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelResultLayout = new javax.swing.GroupLayout(PanelResult);
        PanelResult.setLayout(PanelResultLayout);
        PanelResultLayout.setHorizontalGroup(
            PanelResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelResultLayout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addGroup(PanelResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(PanelResultLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(159, 159, 159)
                        .addComponent(btnRefreshResult))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 855, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(251, Short.MAX_VALUE))
        );
        PanelResultLayout.setVerticalGroup(
            PanelResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelResultLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(PanelResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(btnRefreshResult))
                .addGap(62, 62, 62)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(282, Short.MAX_VALUE))
        );

        PanelUtama.add(PanelResult, "result");

        jLabel22.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel22.setText("CETAK LAPORAN HASIL PEMILIHAN");

        tblReport.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        tblReport.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "No Urut", "Nama Kandidat", "Total Suara", "Persentase"
            }
        ));
        tblReport.setRowHeight(40);
        jScrollPane6.setViewportView(tblReport);

        btnPrint.setBackground(new java.awt.Color(0, 153, 255));
        btnPrint.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        btnPrint.setText("CETAK LAPORAN");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        txtPreview.setEditable(false);
        txtPreview.setColumns(20);
        txtPreview.setLineWrap(true);
        txtPreview.setRows(5);
        txtPreview.setWrapStyleWord(true);
        jScrollPane7.setViewportView(txtPreview);

        javax.swing.GroupLayout PanelReportLayout = new javax.swing.GroupLayout(PanelReport);
        PanelReport.setLayout(PanelReportLayout);
        PanelReportLayout.setHorizontalGroup(
            PanelReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelReportLayout.createSequentialGroup()
                .addGroup(PanelReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelReportLayout.createSequentialGroup()
                        .addGap(374, 374, 374)
                        .addComponent(jLabel22))
                    .addGroup(PanelReportLayout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(btnPrint)
                        .addGap(259, 259, 259)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelReportLayout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 864, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(191, Short.MAX_VALUE))
        );
        PanelReportLayout.setVerticalGroup(
            PanelReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelReportLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel22)
                .addGap(53, 53, 53)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(PanelReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPrint)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(77, 77, 77))
        );

        PanelUtama.add(PanelReport, "report");

        jLabel23.setFont(new java.awt.Font("Poppins SemiBold", 1, 24)); // NOI18N
        jLabel23.setText("PENGATURAN SISTEM");

        btnResetSuara.setBackground(new java.awt.Color(204, 0, 0));
        btnResetSuara.setFont(new java.awt.Font("Poppins SemiBold", 1, 24)); // NOI18N
        btnResetSuara.setText("RESET SELURUH DATA SUARA");
        btnResetSuara.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetSuaraActionPerformed(evt);
            }
        });

        btnBukaSesi.setBackground(new java.awt.Color(0, 204, 0));
        btnBukaSesi.setFont(new java.awt.Font("Poppins SemiBold", 1, 24)); // NOI18N
        btnBukaSesi.setText("BUKA SESI");
        btnBukaSesi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBukaSesiActionPerformed(evt);
            }
        });

        btnTutupSesi.setBackground(new java.awt.Color(255, 255, 0));
        btnTutupSesi.setFont(new java.awt.Font("Poppins SemiBold", 1, 24)); // NOI18N
        btnTutupSesi.setText("TUTUP SESI");
        btnTutupSesi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTutupSesiActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        jLabel24.setText("Versi Aplikasi: v1.0 ");

        jLabel25.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        jLabel25.setText("Dikembangkan oleh: Zexxso and Team");

        jLabel26.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        jLabel26.setText("Tahun : 2026");

        lblStatusSesi.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        lblStatusSesi.setText("Status E-Voting: DITUTUP");

        javax.swing.GroupLayout PanelSettingLayout = new javax.swing.GroupLayout(PanelSetting);
        PanelSetting.setLayout(PanelSettingLayout);
        PanelSettingLayout.setHorizontalGroup(
            PanelSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSettingLayout.createSequentialGroup()
                .addContainerGap(332, Short.MAX_VALUE)
                .addGroup(PanelSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25)
                    .addComponent(jLabel26))
                .addGap(507, 507, 507))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelSettingLayout.createSequentialGroup()
                .addContainerGap(338, Short.MAX_VALUE)
                .addGroup(PanelSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel23)
                    .addComponent(lblStatusSesi)
                    .addGroup(PanelSettingLayout.createSequentialGroup()
                        .addComponent(btnBukaSesi)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTutupSesi))
                    .addComponent(btnResetSuara))
                .addGap(457, 457, 457))
        );
        PanelSettingLayout.setVerticalGroup(
            PanelSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSettingLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel23)
                .addGap(81, 81, 81)
                .addComponent(lblStatusSesi)
                .addGap(26, 26, 26)
                .addGroup(PanelSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTutupSesi)
                    .addComponent(btnBukaSesi))
                .addGap(18, 18, 18)
                .addComponent(btnResetSuara)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 223, Short.MAX_VALUE)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel26)
                .addGap(54, 54, 54))
        );

        PanelUtama.add(PanelSetting, "setting");

        tblAdmin.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        tblAdmin.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Username", "Password"
            }
        ));
        tblAdmin.setRowHeight(40);
        tblAdmin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblAdminMouseClicked(evt);
            }
        });
        jScrollPane8.setViewportView(tblAdmin);

        jLabel27.setFont(new java.awt.Font("Poppins SemiBold", 1, 24)); // NOI18N
        jLabel27.setText("MANAJEMEN ADMIN");

        jLabel28.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel28.setText("Username");

        txtUserAdmin.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        jLabel29.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel29.setText("Password");

        txtPassAdmin.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N

        btnSaveAdmin.setBackground(new java.awt.Color(51, 204, 0));
        btnSaveAdmin.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnSaveAdmin.setText("SAVE");
        btnSaveAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveAdminActionPerformed(evt);
            }
        });

        btnUpdateAdmin.setBackground(new java.awt.Color(255, 255, 0));
        btnUpdateAdmin.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnUpdateAdmin.setText("UPDATE");
        btnUpdateAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateAdminActionPerformed(evt);
            }
        });

        btnDeleteAdmin.setBackground(new java.awt.Color(255, 0, 51));
        btnDeleteAdmin.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnDeleteAdmin.setText("DELETE");
        btnDeleteAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteAdminActionPerformed(evt);
            }
        });

        btnClearAdmin.setBackground(new java.awt.Color(255, 153, 51));
        btnClearAdmin.setFont(new java.awt.Font("Poppins SemiBold", 1, 14)); // NOI18N
        btnClearAdmin.setText("CLEAR");
        btnClearAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAdminActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelKelolaAdminLayout = new javax.swing.GroupLayout(PanelKelolaAdmin);
        PanelKelolaAdmin.setLayout(PanelKelolaAdminLayout);
        PanelKelolaAdminLayout.setHorizontalGroup(
            PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelKelolaAdminLayout.createSequentialGroup()
                .addGap(488, 488, 488)
                .addComponent(jLabel27)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelKelolaAdminLayout.createSequentialGroup()
                .addContainerGap(295, Short.MAX_VALUE)
                .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelKelolaAdminLayout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 625, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(270, 270, 270))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelKelolaAdminLayout.createSequentialGroup()
                        .addComponent(btnSaveAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdateAdmin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteAdmin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearAdmin)
                        .addGap(390, 390, 390))))
            .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(PanelKelolaAdminLayout.createSequentialGroup()
                    .addGap(392, 392, 392)
                    .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel28)
                        .addComponent(jLabel29))
                    .addGap(38, 38, 38)
                    .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtPassAdmin)
                        .addComponent(txtUserAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
                    .addGap(392, 392, 392)))
        );
        PanelKelolaAdminLayout.setVerticalGroup(
            PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelKelolaAdminLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel27)
                .addGap(65, 65, 65)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 190, Short.MAX_VALUE)
                .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveAdmin)
                    .addComponent(btnUpdateAdmin)
                    .addComponent(btnDeleteAdmin)
                    .addComponent(btnClearAdmin))
                .addGap(217, 217, 217))
            .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(PanelKelolaAdminLayout.createSequentialGroup()
                    .addGap(309, 309, 309)
                    .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel28)
                        .addComponent(txtUserAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(PanelKelolaAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtPassAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel29))
                    .addContainerGap(309, Short.MAX_VALUE)))
        );

        PanelUtama.add(PanelKelolaAdmin, "kelolaadmin");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(PanelSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelUtama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(PanelUtama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnUpdateVoterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateVoterActionPerformed
        // TODO add your handling code here:
        try {
            // Query untuk mengubah data berdasarkan NIM
            String sql = "UPDATE tb_pemilih SET nama_pemilih = ?, token = ? WHERE nim = ?";

            java.sql.Connection conn = Config.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);

            // Memasukkan data ke dalam tanda tanya (?)
            pst.setString(1, txtNama.getText());
            pst.setString(2, txtToken.getText());
            pst.setString(3, txtNim.getText()); // NIM ditaruh di parameter ketiga (WHERE)

            pst.execute();
            JOptionPane.showMessageDialog(null, "Data Pemilih Berhasil Diubah!");

            // Memanggil method agar tabel refresh & form kosong kembali
            loadDataVoter();
            txtNim.setText("");
            txtNama.setText("");
            txtToken.setText("");
            txtNim.setEditable(true); // Buka kembali kunci NIM

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Perubahan Data Gagal: " + e.getMessage());
        }
    }//GEN-LAST:event_btnUpdateVoterActionPerformed

    private void btnSaveVoterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveVoterActionPerformed
        // TODO add your handling code here:
        try {
            // 1. Mengambil data dari Text Field
            String nim = txtNim.getText();
            String nama = txtNama.getText();
            String token = txtToken.getText();

            // 2. Validasi: Memastikan tidak ada kolom yang kosong
            if (nim.isEmpty() || nama.isEmpty() || token.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua kolom (NIM, Nama, Token) wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return; // Menghentikan proses jika ada yang kosong
            }

            // 3. Query SQL untuk memasukkan data (status_vote otomatis diset 'Belum')
            String sql = "INSERT INTO tb_pemilih (nim, nama_pemilih, token, status_vote) VALUES (?, ?, ?, 'Belum')";

            // 4. Memanggil koneksi dan menyiapkan statement
            java.sql.Connection conn = Config.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);

            // 5. Memasukkan data ke dalam tanda tanya (?) pada query
            pst.setString(1, nim);
            pst.setString(2, nama);
            pst.setString(3, token);

            // 6. Menjalankan perintah eksekusi ke database
            pst.execute();

            // 7. Menampilkan pesan sukses
            JOptionPane.showMessageDialog(null, "Data Pemilih Berhasil Disimpan!");

            // 8. Mengosongkan form kembali setelah berhasil menyimpan
            txtNim.setText("");
            txtNama.setText("");
            txtToken.setText("");
            
            loadDataVoter();

        } catch (Exception e) {
            // Menampilkan pesan error jika terjadi kesalahan (misal: NIM/Token sudah ada yang sama)
            JOptionPane.showMessageDialog(this, "Data Gagal Disimpan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveVoterActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        // TODO add your handling code here:
        try {
            // 1. Membuka Jendela Pencarian File
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();

            // 2. Membatasi agar hanya bisa memilih file gambar (JPG, PNG)
            javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg");
            fileChooser.setFileFilter(filter);

            // 3. Jika panitia memilih sebuah file dan klik "Open"
            int result = fileChooser.showOpenDialog(this);
            if (result == javax.swing.JFileChooser.APPROVE_OPTION) {

                java.io.File selectedFile = fileChooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath(); // Mengambil lokasi file (misal: C:\foto\paslon1.png)

                // Memasukkan lokasi file ke text field path
                txtPathFoto.setText(path);

                // 4. Menampilkan foto ke kotak label (lblFotoPreview) dan menyesuaikan ukurannya
                javax.swing.ImageIcon imageIcon = new javax.swing.ImageIcon(path);
                // Sesuaikan angka 120 dan 160 dengan lebar & tinggi lblFotoPreview milik Anda
                java.awt.Image image = imageIcon.getImage().getScaledInstance(120, 160, java.awt.Image.SCALE_SMOOTH); 

                lblFotoPreview.setIcon(new javax.swing.ImageIcon(image));
                lblFotoPreview.setText(""); // Menghilangkan teks jika sebelumnya ada
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat foto: " + e.getMessage());
        }
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void tblVoterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVoterMouseClicked
        // TODO add your handling code here:
        // Mendapatkan baris yang diklik
        int baris = tblVoter.rowAtPoint(evt.getPoint());

        // Memindahkan teks dari tabel ke kotak input
        String nim = tblVoter.getValueAt(baris, 0).toString();
        txtNim.setText(nim);
        txtNama.setText(tblVoter.getValueAt(baris, 1).toString());
        txtToken.setText(tblVoter.getValueAt(baris, 2).toString());

        // Mengunci kolom NIM agar tidak bisa diedit (karena NIM adalah Primary Key)
        txtNim.setEditable(false);
    }//GEN-LAST:event_tblVoterMouseClicked

    private void btnClearVoterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearVoterActionPerformed
        // TODO add your handling code here:
        txtNim.setText("");
        txtNama.setText("");
        txtToken.setText("");

        // Membuka kembali kunci NIM agar bisa dipakai untuk input data baru
        txtNim.setEditable(true);
    }//GEN-LAST:event_btnClearVoterActionPerformed

    private void btnDeleteVoterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteVoterActionPerformed
        // TODO add your handling code here:
        // Memunculkan kotak konfirmasi Yes/No
        int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data pemilih ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        // Jika user memilih opsi Yes (1)
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM tb_pemilih WHERE nim = ?";

                java.sql.Connection conn = Config.configDB();
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);

                // Parameter hapus berdasarkan NIM yang ada di text field
                pst.setString(1, txtNim.getText());
                pst.execute();

                JOptionPane.showMessageDialog(null, "Data Berhasil Dihapus!");

                // Refresh tabel dan bersihkan form
                loadDataVoter();
                txtNim.setText("");
                txtNama.setText("");
                txtToken.setText("");
                txtNim.setEditable(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal Menghapus Data: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteVoterActionPerformed

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        // TODO add your handling code here:
        // 1. Tentukan karakter apa saja yang boleh digunakan (Huruf Kapital & Angka)
        String karakterBoleh = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder tokenAcak = new StringBuilder();
        java.util.Random rnd = new java.util.Random();

        // 2. Tentukan panjang token (misalnya 6 karakter)
        int panjangToken = 6;

        // 3. Looping untuk mengambil karakter secara acak
        for (int i = 0; i < panjangToken; i++) {
            // Memilih satu posisi acak dari daftar karakter
            int index = rnd.nextInt(karakterBoleh.length());
            // Memasukkan karakter terpilih ke dalam pembuat token
            tokenAcak.append(karakterBoleh.charAt(index));
        }

        // 4. Memasukkan hasil token acak ke dalam text field
        txtToken.setText(tokenAcak.toString());
    }//GEN-LAST:event_btnGenerateActionPerformed

    private void btnSaveKandidatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveKandidatActionPerformed
        // TODO add your handling code here:
        try {
            // 1. Mengambil data dari form
            String noUrut = txtNoUrut.getText();
            String nama = txtNamaKandidat.getText();
            String visi = txtVisi.getText();
            String misi = txtMisi.getText();
            String pathFoto = txtPathFoto.getText(); // Lokasi foto yang tadi dipilih

            // 2. Validasi kolom kosong
            if (noUrut.isEmpty() || nama.isEmpty() || visi.isEmpty() || misi.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Semua data profil Kandidat wajib diisi!");
                return;
            }

            // 3. Query INSERT (jumlah_suara otomatis diisi 0 karena baru daftar)
            String sql = "INSERT INTO tb_kandidat (no_urut, nama_kandidat, visi, misi, foto, jumlah_suara) VALUES (?, ?, ?, ?, ?, 0)";

            java.sql.Connection conn = Config.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, noUrut);
            pst.setString(2, nama);
            pst.setString(3, visi);
            pst.setString(4, misi);
            pst.setString(5, pathFoto); // Menyimpan lokasi file fotonya

            // 4. Eksekusi
            pst.execute();
            javax.swing.JOptionPane.showMessageDialog(null, "Data Kandidat Berhasil Disimpan!");

            // 5. Bersihkan form setelah simpan
            txtNoUrut.setText("");
            txtNamaKandidat.setText("");
            txtVisi.setText("");
            txtMisi.setText("");
            txtPathFoto.setText("");
            lblFotoPreview.setIcon(null); // Kosongkan preview foto
            loadDataKandidat();

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Data Kandidat Gagal Disimpan: " + e.getMessage());
        }
    }//GEN-LAST:event_btnSaveKandidatActionPerformed

    private void tblKandidatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblKandidatMouseClicked
        // TODO add your handling code here:
        int baris = tblKandidat.rowAtPoint(evt.getPoint());

        // 1. Memindahkan data teks ke form
        String noUrut = tblKandidat.getValueAt(baris, 0).toString();
        txtNoUrut.setText(noUrut);
        txtNoUrut.setEditable(false); // Kunci Nomor Urut (Primary Key)

        txtNamaKandidat.setText(tblKandidat.getValueAt(baris, 1).toString());
        txtVisi.setText(tblKandidat.getValueAt(baris, 2).toString());
        txtMisi.setText(tblKandidat.getValueAt(baris, 3).toString());

        // 2. Memuat ulang foto dari Path yang tersimpan
        String pathFoto = tblKandidat.getValueAt(baris, 4) != null ? tblKandidat.getValueAt(baris, 4).toString() : "";
        txtPathFoto.setText(pathFoto);

        if (!pathFoto.isEmpty()) {
            try {
                javax.swing.ImageIcon imageIcon = new javax.swing.ImageIcon(pathFoto);
                java.awt.Image image = imageIcon.getImage().getScaledInstance(120, 160, java.awt.Image.SCALE_SMOOTH);
                lblFotoPreview.setIcon(new javax.swing.ImageIcon(image));
            } catch (Exception e) {
                lblFotoPreview.setIcon(null);
                lblFotoPreview.setText("Foto Hilang");
            }
        } else {
            lblFotoPreview.setIcon(null);
        }
    }//GEN-LAST:event_tblKandidatMouseClicked

    private void btnClearKandidatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearKandidatActionPerformed
        // TODO add your handling code here:
        txtNoUrut.setText("");
        txtNamaKandidat.setText("");
        txtVisi.setText("");
        txtMisi.setText("");
        txtPathFoto.setText("");
        lblFotoPreview.setIcon(null);
        txtNoUrut.setEditable(true);
    }//GEN-LAST:event_btnClearKandidatActionPerformed

    private void btnUpdateKandidatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateKandidatActionPerformed
        // TODO add your handling code here:
        try {
            String sql = "UPDATE tb_kandidat SET nama_kandidat=?, visi=?, misi=?, foto=? WHERE no_urut=?";

            java.sql.Connection conn = Config.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, txtNamaKandidat.getText());
            pst.setString(2, txtVisi.getText());
            pst.setString(3, txtMisi.getText());
            pst.setString(4, txtPathFoto.getText());
            pst.setString(5, txtNoUrut.getText()); // WHERE no_urut = ?

            pst.execute();
            javax.swing.JOptionPane.showMessageDialog(null, "Data Kandidat Berhasil Diperbarui!");

            // Refresh tabel & bersihkan form
            loadDataKandidat();
            txtNoUrut.setText("");
            txtNamaKandidat.setText("");
            txtVisi.setText("");
            txtMisi.setText("");
            txtPathFoto.setText("");
            lblFotoPreview.setIcon(null);
            txtNoUrut.setEditable(true);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Perubahan Data Gagal: " + e.getMessage());
        }
    }//GEN-LAST:event_btnUpdateKandidatActionPerformed

    private void btnDeleteKandidatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteKandidatActionPerformed
        // TODO add your handling code here:
        int confirm = javax.swing.JOptionPane.showConfirmDialog(null, "Yakin ingin menghapus kandidat ini?", "Konfirmasi Hapus", javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM tb_kandidat WHERE no_urut = ?";

                java.sql.Connection conn = Config.configDB();
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, txtNoUrut.getText());
                pst.execute();

                javax.swing.JOptionPane.showMessageDialog(null, "Kandidat Berhasil Dihapus!");

                loadDataKandidat();
                txtNoUrut.setText("");
                txtNamaKandidat.setText("");
                txtVisi.setText("");
                txtMisi.setText("");
                txtPathFoto.setText("");
                lblFotoPreview.setIcon(null);
                txtNoUrut.setEditable(true);

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal Menghapus Kandidat: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteKandidatActionPerformed

    private void BBB6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BBB6ActionPerformed
        // TODO add your handling code here:
        // Menutup form Admin saat ini
        this.dispose();

        // Membuka kembali Form Login (Sesuaikan 'FormLogin' dengan nama class JFrame login Anda)
        new FormLogin().setVisible(true);
    }//GEN-LAST:event_BBB6ActionPerformed

    private void navDashboardMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navDashboardMouseEntered
        // TODO add your handling code here:
        // Ubah ke warna biru saat di-hover
        navDashboard.setForeground(new java.awt.Color(52, 152, 219));
    }//GEN-LAST:event_navDashboardMouseEntered

    private void navDashboardMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navDashboardMouseExited
        // TODO add your handling code here:
        // Mengembalikan warna background menyatu dengan sidebar
        if (!menuAktif.equals("dashboard")) {
            navDashboard.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_navDashboardMouseExited

    private void navDashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navDashboardMouseClicked
        // TODO add your handling code here:
        // Update status warna menu
        ubahWarnaMenu("dashboard");
        loadDataDashboard();
        // Pindah panel CardLayout
        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "dashboard");
    }//GEN-LAST:event_navDashboardMouseClicked

    private void navVoterManagementMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navVoterManagementMouseClicked
        // TODO add your handling code here:
        ubahWarnaMenu("voter");
        loadDataVoter();

        // Pindah panel CardLayout
        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "voter");
    }//GEN-LAST:event_navVoterManagementMouseClicked

    private void navVoterManagementMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navVoterManagementMouseEntered
        // TODO add your handling code here:
        // Ubah ke warna biru saat di-hover
        navVoterManagement.setForeground(new java.awt.Color(52, 152, 219));
    }//GEN-LAST:event_navVoterManagementMouseEntered

    private void navVoterManagementMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navVoterManagementMouseExited
        // TODO add your handling code here:
        if (!menuAktif.equals("voter")) {
            navVoterManagement.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_navVoterManagementMouseExited

    private void navCandidateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navCandidateMouseClicked
        // TODO add your handling code here:
        ubahWarnaMenu("kandidat");

        // Pindah panel CardLayout
        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "kandidat");
    }//GEN-LAST:event_navCandidateMouseClicked

    private void navCandidateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navCandidateMouseEntered
        // TODO add your handling code here:
        // Ubah ke warna biru saat di-hover
        navCandidate.setForeground(new java.awt.Color(52, 152, 219));
    }//GEN-LAST:event_navCandidateMouseEntered

    private void navCandidateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navCandidateMouseExited
        // TODO add your handling code here:
         if (!menuAktif.equals("kandidat")) {
            navCandidate.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_navCandidateMouseExited

    private void navResultMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navResultMouseClicked
        // TODO add your handling code here:
        ubahWarnaMenu("result"); // Jika Anda memasukkan "result" ke method warna yang tadi
        loadDataResult(); // <-- Agar tabel langsung update saat menu diklik

        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "result");
    }//GEN-LAST:event_navResultMouseClicked

    private void navResultMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navResultMouseEntered
        // TODO add your handling code here:
        navResult.setForeground(new java.awt.Color(52, 152, 219));
    }//GEN-LAST:event_navResultMouseEntered

    private void navResultMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navResultMouseExited
        // TODO add your handling code here:
        if (!menuAktif.equals("result")) {
            navResult.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_navResultMouseExited

    private void navSettingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navSettingMouseClicked
        // TODO add your handling code here:
        ubahWarnaMenu("setting");
        tampilkanStatusSesiAktif();

        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "setting");
    }//GEN-LAST:event_navSettingMouseClicked

    private void navSettingMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navSettingMouseEntered
        // TODO add your handling code here:
        navSetting.setForeground(new java.awt.Color(52, 152, 219));
    }//GEN-LAST:event_navSettingMouseEntered

    private void navSettingMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navSettingMouseExited
        // TODO add your handling code here:
            if (!menuAktif.equals("setting")) {
            navSetting.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_navSettingMouseExited

    private void navReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navReportMouseClicked
        // TODO add your handling code here:
        ubahWarnaMenu("report"); 
        loadDataReport(); // Memuat data terbaru sebelum ditampilkan

        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "report");
    }//GEN-LAST:event_navReportMouseClicked

    private void navReportMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navReportMouseEntered
        // TODO add your handling code here:
        navReport.setForeground(new java.awt.Color(52, 152, 219));
    }//GEN-LAST:event_navReportMouseEntered

    private void navReportMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navReportMouseExited
        // TODO add your handling code here:
        if (!menuAktif.equals("report")) {
            navReport.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_navReportMouseExited

    private void btnRefreshResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshResultActionPerformed
        // TODO add your handling code here:
        loadDataResult();
        javax.swing.JOptionPane.showMessageDialog(this, "Data hasil suara berhasil diperbarui!");
    }//GEN-LAST:event_btnRefreshResultActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        // TODO add your handling code here:
        // 1. Membuka jendela "Save As"
        javax.swing.JFileChooser dialog = new javax.swing.JFileChooser();
        dialog.setDialogTitle("Simpan Laporan PDF");
        dialog.setSelectedFile(new java.io.File("Laporan_EVoting.pdf"));

        int dialogResult = dialog.showSaveDialog(this);
        if (dialogResult == javax.swing.JFileChooser.APPROVE_OPTION) {
            String filePath = dialog.getSelectedFile().getPath();

            // Pastikan ekstensinya .pdf
            if (!filePath.endsWith(".pdf")) {
                filePath += ".pdf";
            }

            try {
                // 2. Membuat dokumen PDF baru menggunakan iTextPDF
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filePath));
                document.open();

                // 3. Menambahkan Judul Laporan
                com.itextpdf.text.Font fontJudul = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph judul = new com.itextpdf.text.Paragraph("LAPORAN RESMI HASIL E-VOTING\n\n", fontJudul);
                judul.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(judul);

                // 4. Memindahkan isi JTable ke PDF Table
                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(tblReport.getColumnCount());

                // Memasukkan header tabel (No Urut, Nama, dll)
                for (int i = 0; i < tblReport.getColumnCount(); i++) {
                    pdfTable.addCell(new com.itextpdf.text.Phrase(tblReport.getColumnName(i)));
                }

                // Memasukkan baris data
                for (int rows = 0; rows < tblReport.getRowCount(); rows++) {
                    for (int cols = 0; cols < tblReport.getColumnCount(); cols++) {
                        pdfTable.addCell(tblReport.getModel().getValueAt(rows, cols).toString());
                    }
                }
                document.add(pdfTable);

                // 5. Menambahkan Footer / Area Tanda Tangan
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy");
                String tanggalSekarang = sdf.format(new java.util.Date());

                com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph("\n\nBekasi, " + tanggalSekarang + "\nMengetahui,\nPanitia Pemilihan\n\n\n\n(.........................................)");
                footer.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                document.add(footer);

                document.close();

                javax.swing.JOptionPane.showMessageDialog(this, "Laporan PDF berhasil disimpan di:\n" + filePath);

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnResetSuaraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetSuaraActionPerformed
        // TODO add your handling code here:
        // 1. Munculkan Kotak Peringatan Keras (Warning)
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, 
                "PERINGATAN BAHAYA!\n\nApakah Anda yakin ingin MERESET SELURUH DATA SUARA?\n- Semua suara kandidat akan menjadi 0\n- Status semua pemilih akan kembali menjadi 'Belum'\n\nAksi ini tidak dapat dibatalkan!", 
                "Konfirmasi Reset Darurat", 
                javax.swing.JOptionPane.YES_NO_OPTION, 
                javax.swing.JOptionPane.WARNING_MESSAGE);

        // 2. Jika user menekan "Yes"
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                java.sql.Connection conn = Config.configDB();

                // A. Perintah mengembalikan suara kandidat jadi 0
                String sqlResetKandidat = "UPDATE tb_kandidat SET jumlah_suara = 0";
                java.sql.PreparedStatement pst1 = conn.prepareStatement(sqlResetKandidat);
                pst1.execute();

                // B. Perintah mengembalikan status pemilih jadi Belum
                String sqlResetPemilih = "UPDATE tb_pemilih SET status_vote = 'Belum'";
                java.sql.PreparedStatement pst2 = conn.prepareStatement(sqlResetPemilih);
                pst2.execute();

                // C. Refresh semua data tabel di belakang layar agar langsung update
                loadDataVoter();
                loadDataResult();
                loadDataReport();

                javax.swing.JOptionPane.showMessageDialog(this, "Sistem Berhasil Di-Reset! Semua data suara kembali menjadi 0.");

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal mereset sistem: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnResetSuaraActionPerformed

    private void btnBukaSesiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBukaSesiActionPerformed
        try {
            java.sql.Connection conn = Config.configDB();
            String sql = "UPDATE tb_setting SET status_sesi = 'Buka' WHERE id = 1";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.execute();

            lblStatusSesi.setText("Status E-Voting: DIBUKA");
            lblStatusSesi.setForeground(new java.awt.Color(0, 153, 0)); // Hijau
            javax.swing.JOptionPane.showMessageDialog(this, "Sesi Pemilihan Telah Dibuka! Status disimpan ke database.");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal membuka sesi: " + e.getMessage());
        }
    }//GEN-LAST:event_btnBukaSesiActionPerformed

    private void btnTutupSesiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTutupSesiActionPerformed
        // TODO add your handling code here:
        try {
            java.sql.Connection conn = Config.configDB();
            String sql = "UPDATE tb_setting SET status_sesi = 'Tutup' WHERE id = 1";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.execute();

            lblStatusSesi.setText("Status E-Voting: DITUTUP");
            lblStatusSesi.setForeground(new java.awt.Color(255, 0, 0)); // Merah
            javax.swing.JOptionPane.showMessageDialog(this, "Sesi Pemilihan Ditutup! Status disimpan ke database.");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menutup sesi: " + e.getMessage());
        }
    }//GEN-LAST:event_btnTutupSesiActionPerformed

    private void navAdminMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navAdminMouseClicked
        // TODO add your handling code here:
        ubahWarnaMenu("kelolaadmin"); 
        loadDataAdmin(); // Memuat data terbaru sebelum ditampilkan

        java.awt.CardLayout cl = (java.awt.CardLayout) PanelUtama.getLayout();
        cl.show(PanelUtama, "kelolaadmin");
    }//GEN-LAST:event_navAdminMouseClicked

    private void navAdminMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navAdminMouseEntered
        // TODO add your handling code here:
        navAdmin.setForeground(new java.awt.Color(52, 152, 219));
    }//GEN-LAST:event_navAdminMouseEntered

    private void navAdminMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_navAdminMouseExited
        // TODO add your handling code here:
        if (!menuAktif.equals("kelolaadmin")) {
            navAdmin.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_navAdminMouseExited

    private void tblAdminMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblAdminMouseClicked
        // TODO add your handling code here:
        int baris = tblAdmin.rowAtPoint(evt.getPoint());
        txtUserAdmin.setText(tblAdmin.getValueAt(baris, 0).toString());
        txtPassAdmin.setText(tblAdmin.getValueAt(baris, 1).toString());
        txtUserAdmin.setEditable(false); // Username tidak boleh diedit (Primary Key)
    }//GEN-LAST:event_tblAdminMouseClicked

    private void btnSaveAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAdminActionPerformed
        // TODO add your handling code here:
        try {
            String sql = "INSERT INTO tb_admin (username, password) VALUES (?, ?)";
            java.sql.Connection conn = Config.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtUserAdmin.getText());
            pst.setString(2, txtPassAdmin.getText());
            pst.execute();
            javax.swing.JOptionPane.showMessageDialog(null, "Admin Berhasil Ditambahkan!");
            loadDataAdmin();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }//GEN-LAST:event_btnSaveAdminActionPerformed

    private void btnUpdateAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateAdminActionPerformed
        // TODO add your handling code here:
        try {
            String sql = "UPDATE tb_admin SET password = ? WHERE username = ?";
            java.sql.Connection conn = Config.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtPassAdmin.getText());
            pst.setString(2, txtUserAdmin.getText()); // Username sbg parameter WHERE
            pst.execute();
            javax.swing.JOptionPane.showMessageDialog(null, "Password Admin Diubah!");
            loadDataAdmin();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }//GEN-LAST:event_btnUpdateAdminActionPerformed

    private void btnDeleteAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteAdminActionPerformed
        // TODO add your handling code here:
        try {
            String sql = "DELETE FROM tb_admin WHERE username = ?";
            java.sql.Connection conn = Config.configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtUserAdmin.getText());
            pst.execute();
            javax.swing.JOptionPane.showMessageDialog(null, "Admin Dihapus!");
            loadDataAdmin();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteAdminActionPerformed

    private void btnClearAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAdminActionPerformed
        // TODO add your handling code here:
        txtUserAdmin.setText("");
        txtPassAdmin.setText("");
        txtUserAdmin.setEditable(true);
    }//GEN-LAST:event_btnClearAdminActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Admin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Admin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BBB6;
    private javax.swing.JPanel PanelDashboard;
    private javax.swing.JPanel PanelKandidat;
    private javax.swing.JPanel PanelKelolaAdmin;
    private javax.swing.JPanel PanelReport;
    private javax.swing.JPanel PanelResult;
    private javax.swing.JPanel PanelSetting;
    private javax.swing.JPanel PanelSidebar;
    private javax.swing.JPanel PanelUtama;
    private javax.swing.JPanel PanelVoter;
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnBukaSesi;
    private javax.swing.JButton btnClearAdmin;
    private javax.swing.JButton btnClearKandidat;
    private javax.swing.JButton btnClearVoter;
    private javax.swing.JButton btnDeleteAdmin;
    private javax.swing.JButton btnDeleteKandidat;
    private javax.swing.JButton btnDeleteVoter;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRefreshResult;
    private javax.swing.JButton btnResetSuara;
    private javax.swing.JButton btnSaveAdmin;
    private javax.swing.JButton btnSaveKandidat;
    private javax.swing.JButton btnSaveVoter;
    private javax.swing.JButton btnTutupSesi;
    private javax.swing.JButton btnUpdateAdmin;
    private javax.swing.JButton btnUpdateKandidat;
    private javax.swing.JButton btnUpdateVoter;
    private javax.swing.JPanel cardBelumVote;
    private javax.swing.JPanel cardSudahVote;
    private javax.swing.JPanel cardTotalKandidat;
    private javax.swing.JPanel cardTotalPemilih;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel lblFotoPreview;
    private javax.swing.JLabel lblKandidat1;
    private javax.swing.JLabel lblKandidat2;
    private javax.swing.JLabel lblKandidat3;
    private javax.swing.JLabel lblKandidat4;
    private javax.swing.JLabel lblStatusSesi;
    private javax.swing.JLabel navAdmin;
    private javax.swing.JLabel navCandidate;
    private javax.swing.JLabel navDashboard;
    private javax.swing.JLabel navReport;
    private javax.swing.JLabel navResult;
    private javax.swing.JLabel navSetting;
    private javax.swing.JLabel navVoterManagement;
    private javax.swing.JPanel panelGrafik;
    private javax.swing.JProgressBar progKandidat1;
    private javax.swing.JProgressBar progKandidat2;
    private javax.swing.JProgressBar progKandidat3;
    private javax.swing.JProgressBar progKandidat4;
    private javax.swing.JTable tblAdmin;
    private javax.swing.JTable tblKandidat;
    private javax.swing.JTable tblReport;
    private javax.swing.JTable tblResult;
    private javax.swing.JTable tblVoter;
    private javax.swing.JTextArea txtMisi;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtNamaKandidat;
    private javax.swing.JTextField txtNim;
    private javax.swing.JTextField txtNoUrut;
    private javax.swing.JTextField txtPassAdmin;
    private javax.swing.JLabel txtPathFoto;
    private javax.swing.JTextArea txtPreview;
    private javax.swing.JTextField txtToken;
    private javax.swing.JTextField txtUserAdmin;
    private javax.swing.JTextArea txtVisi;
    // End of variables declaration//GEN-END:variables
}
