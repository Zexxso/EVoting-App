/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoting;

/**
 *
 * @author DZAKY
 */
public class FormPemilih extends javax.swing.JFrame {
    String nimPemilihAktif;
    String kandidatTerpilih = ""; // Menyimpan Nomor Urut yang diklik
    
    // Menyimpan data lengkap untuk ditampilkan saat Expand diklik
    String[] idKandidat = new String[5];
    String[] namaLengkap = new String[5];
    String[] visiLengkap = new String[5];
    String[] misiLengkap = new String[5];
    String[] pathFoto = new String[5];

    // Ubah Constructor-nya agar meminta data NIM
    public FormPemilih(String nimLogin) {
        initComponents();
        this.setLocationRelativeTo(null); // Agar form di tengah layar
        this.nimPemilihAktif = nimLogin; // Menyimpan NIM dari form login
        tampilkanNamaUser();
        loadDataKandidat();
    }
    
    private void tampilkanNamaUser() {
        try {
            java.sql.Connection conn = Config.configDB();
            String sql = "SELECT nama_pemilih FROM tb_pemilih WHERE nim = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, nimPemilihAktif); // Menggunakan NIM yang dibawa dari form login
            java.sql.ResultSet res = pst.executeQuery();

            if (res.next()) {
                // Mengubah teks label menjadi nama asli dari database
                lblNamaUserLog.setText( res.getString("nama_pemilih") + " (" + nimPemilihAktif + ")");
            }
        } catch (Exception e) {
            System.out.println("Gagal mengambil nama pemilih: " + e.getMessage());
        }
    }

    private void loadDataKandidat() {
        // 1. Sembunyikan semua card terlebih dahulu
        card1.setVisible(false);
        card2.setVisible(false);
        card3.setVisible(false);
        card4.setVisible(false);

        try {
            // Ambil data kandidat (dibatasi maksimal 4)
            String sql = "SELECT * FROM tb_kandidat ORDER BY no_urut ASC LIMIT 4";
            java.sql.Connection conn = Config.configDB();
            java.sql.Statement stm = conn.createStatement();
            java.sql.ResultSet res = stm.executeQuery(sql);

            int i = 1; // Penghitung urutan card
            while (res.next()) {
                // Simpan data lengkap ke dalam array (untuk popup expand dan proses vote)
                idKandidat[i] = res.getString("no_urut");
                namaLengkap[i] = res.getString("nama_kandidat");
                visiLengkap[i] = res.getString("visi");
                misiLengkap[i] = res.getString("misi");
                pathFoto[i] = res.getString("foto");

                // Membuat teks preview (memotong teks jika terlalu panjang)
                String previewVisi = visiLengkap[i].length() > 25 ? visiLengkap[i].substring(0, 25) + "..." : visiLengkap[i];
                String previewMisi = misiLengkap[i].length() > 25 ? misiLengkap[i].substring(0, 25) + "..." : misiLengkap[i];
                String teksPreview = "Visi:\n" + previewVisi + "\n\nMisi:\n" + previewMisi;

                // Memasukkan gambar ke label
                javax.swing.ImageIcon icon = null;
                if (pathFoto[i] != null && !pathFoto[i].isEmpty()) {
                    try {
                        java.awt.Image img = new javax.swing.ImageIcon(pathFoto[i]).getImage().getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
                        icon = new javax.swing.ImageIcon(img);
                    } catch (Exception e) { /* Gambar tidak ditemukan */ }
                }

                // Memasukkan data ke komponen UI dan memunculkan card-nya
                if (i == 1) {
                    lblNama1.setText(namaLengkap[i]);
                    txtPreview1.setText(teksPreview);
                    if(icon != null) lblFoto1.setIcon(icon); else lblFoto1.setText("Tak Ada Foto");
                    card1.setVisible(true);
                } else if (i == 2) {
                    lblNama2.setText(namaLengkap[i]);
                    txtPreview2.setText(teksPreview);
                    if(icon != null) lblFoto2.setIcon(icon); else lblFoto2.setText("Tak Ada Foto");
                    card2.setVisible(true);
                } else if (i == 3) {
                    lblNama3.setText(namaLengkap[i]);
                    txtPreview3.setText(teksPreview);
                    if(icon != null) lblFoto3.setIcon(icon); else lblFoto3.setText("Tak Ada Foto");
                    card3.setVisible(true);
                } else if (i == 4) {
                    lblNama4.setText(namaLengkap[i]);
                    txtPreview4.setText(teksPreview);
                    if(icon != null) lblFoto4.setIcon(icon); else lblFoto4.setText("Tak Ada Foto");
                    card4.setVisible(true);
                }
                i++;
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat kandidat: " + e.getMessage());
        }
    }
    
    private void setPilihanKandidat(int nomorCard) {
        // Reset semua warna border card kembali ke warna abu-abu / default
        javax.swing.border.Border borderBiasa = javax.swing.BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY, 1);
        card1.setBorder(borderBiasa);
        card2.setBorder(borderBiasa);
        card3.setBorder(borderBiasa);
        card4.setBorder(borderBiasa);

        // Beri border tebal warna biru pada card yang dipilih
        javax.swing.border.Border borderAktif = javax.swing.BorderFactory.createLineBorder(new java.awt.Color(52, 152, 219), 5);

        if (nomorCard == 1) {
            card1.setBorder(borderAktif);
            kandidatTerpilih = idKandidat[1];
        } else if (nomorCard == 2) {
            card2.setBorder(borderAktif);
            kandidatTerpilih = idKandidat[2];
        } else if (nomorCard == 3) {
            card3.setBorder(borderAktif);
            kandidatTerpilih = idKandidat[3];
        } else if (nomorCard == 4) {
            card4.setBorder(borderAktif);
            kandidatTerpilih = idKandidat[4];
        }
    }
    /**
     * Creates new form FormPemilih
     */
    public FormPemilih() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        card1 = new javax.swing.JPanel();
        lblFoto1 = new javax.swing.JLabel();
        lblNama1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtPreview1 = new javax.swing.JTextArea();
        btnExpand1 = new javax.swing.JButton();
        card2 = new javax.swing.JPanel();
        lblFoto2 = new javax.swing.JLabel();
        lblNama2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtPreview2 = new javax.swing.JTextArea();
        btnExpand2 = new javax.swing.JButton();
        card3 = new javax.swing.JPanel();
        lblFoto3 = new javax.swing.JLabel();
        lblNama3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtPreview3 = new javax.swing.JTextArea();
        btnExpand3 = new javax.swing.JButton();
        card4 = new javax.swing.JPanel();
        lblFoto4 = new javax.swing.JLabel();
        lblNama4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtPreview4 = new javax.swing.JTextArea();
        btnExpand4 = new javax.swing.JButton();
        btnVote = new javax.swing.JButton();
        lblNamaUserLog = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Poppins ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("SELAMAT DATANG DI E-VOTING");

        jLabel2.setFont(new java.awt.Font("Poppins", 0, 18)); // NOI18N
        jLabel2.setText("Silakan gunakan hak suara Anda dengan bijak");

        card1.setMaximumSize(new java.awt.Dimension(260, 380));
        card1.setMinimumSize(new java.awt.Dimension(260, 380));
        card1.setPreferredSize(new java.awt.Dimension(260, 380));

        lblFoto1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFoto1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFoto1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblFoto1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblFoto1.setPreferredSize(new java.awt.Dimension(150, 150));
        lblFoto1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFoto1MouseClicked(evt);
            }
        });

        lblNama1.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        lblNama1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNama1.setText("jLabel3");
        lblNama1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        txtPreview1.setEditable(false);
        txtPreview1.setColumns(20);
        txtPreview1.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        txtPreview1.setLineWrap(true);
        txtPreview1.setRows(5);
        jScrollPane1.setViewportView(txtPreview1);

        btnExpand1.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        btnExpand1.setText("Expand");
        btnExpand1.setToolTipText("");
        btnExpand1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExpand1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout card1Layout = new javax.swing.GroupLayout(card1);
        card1.setLayout(card1Layout);
        card1Layout.setHorizontalGroup(
            card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblFoto1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNama1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExpand1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        card1Layout.setVerticalGroup(
            card1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(lblFoto1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNama1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(btnExpand1)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        card2.setMaximumSize(new java.awt.Dimension(260, 380));
        card2.setMinimumSize(new java.awt.Dimension(260, 380));
        card2.setPreferredSize(new java.awt.Dimension(260, 380));

        lblFoto2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFoto2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFoto2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblFoto2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblFoto2.setPreferredSize(new java.awt.Dimension(150, 150));
        lblFoto2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFoto2MouseClicked(evt);
            }
        });

        lblNama2.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        lblNama2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNama2.setText("jLabel3");
        lblNama2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        txtPreview2.setEditable(false);
        txtPreview2.setColumns(20);
        txtPreview2.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        txtPreview2.setLineWrap(true);
        txtPreview2.setRows(5);
        jScrollPane2.setViewportView(txtPreview2);

        btnExpand2.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        btnExpand2.setText("Expand");
        btnExpand2.setToolTipText("");
        btnExpand2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExpand2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout card2Layout = new javax.swing.GroupLayout(card2);
        card2.setLayout(card2Layout);
        card2Layout.setHorizontalGroup(
            card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card2Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addGroup(card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblFoto2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNama2)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExpand2, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(60, 60, 60))
        );
        card2Layout.setVerticalGroup(
            card2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(lblFoto2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblNama2)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnExpand2)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        card3.setMaximumSize(new java.awt.Dimension(260, 380));
        card3.setMinimumSize(new java.awt.Dimension(260, 380));
        card3.setPreferredSize(new java.awt.Dimension(260, 380));

        lblFoto3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFoto3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFoto3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblFoto3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblFoto3.setPreferredSize(new java.awt.Dimension(150, 150));
        lblFoto3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFoto3MouseClicked(evt);
            }
        });

        lblNama3.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        lblNama3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNama3.setText("jLabel3");
        lblNama3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        txtPreview3.setEditable(false);
        txtPreview3.setColumns(20);
        txtPreview3.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        txtPreview3.setLineWrap(true);
        txtPreview3.setRows(5);
        jScrollPane3.setViewportView(txtPreview3);

        btnExpand3.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        btnExpand3.setText("Expand");
        btnExpand3.setToolTipText("");
        btnExpand3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExpand3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout card3Layout = new javax.swing.GroupLayout(card3);
        card3.setLayout(card3Layout);
        card3Layout.setHorizontalGroup(
            card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblFoto3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNama3)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExpand3, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57))
        );
        card3Layout.setVerticalGroup(
            card3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card3Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(lblFoto3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNama3)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnExpand3)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        card4.setMaximumSize(new java.awt.Dimension(260, 380));
        card4.setMinimumSize(new java.awt.Dimension(260, 380));
        card4.setPreferredSize(new java.awt.Dimension(260, 380));

        lblFoto4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFoto4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFoto4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblFoto4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblFoto4.setPreferredSize(new java.awt.Dimension(150, 150));
        lblFoto4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFoto4MouseClicked(evt);
            }
        });

        lblNama4.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        lblNama4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNama4.setText("jLabel3");
        lblNama4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        txtPreview4.setEditable(false);
        txtPreview4.setColumns(20);
        txtPreview4.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        txtPreview4.setLineWrap(true);
        txtPreview4.setRows(5);
        jScrollPane4.setViewportView(txtPreview4);

        btnExpand4.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        btnExpand4.setText("Expand");
        btnExpand4.setToolTipText("");
        btnExpand4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExpand4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout card4Layout = new javax.swing.GroupLayout(card4);
        card4.setLayout(card4Layout);
        card4Layout.setHorizontalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addGroup(card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblFoto4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNama4)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExpand4, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52))
        );
        card4Layout.setVerticalGroup(
            card4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(card4Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(lblFoto4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblNama4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(btnExpand4)
                .addContainerGap(39, Short.MAX_VALUE))
        );

        btnVote.setBackground(new java.awt.Color(0, 0, 0));
        btnVote.setFont(new java.awt.Font("Poppins SemiBold", 0, 24)); // NOI18N
        btnVote.setForeground(new java.awt.Color(255, 255, 255));
        btnVote.setText("VOTE");
        btnVote.setToolTipText("");
        btnVote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoteActionPerformed(evt);
            }
        });

        lblNamaUserLog.setFont(new java.awt.Font("Poppins SemiBold", 0, 18)); // NOI18N
        lblNamaUserLog.setText("jLabel3");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(113, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnVote, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(416, 416, 416))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(card1, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(card2, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(card3, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(card4, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblNamaUserLog))
                        .addGap(88, 88, 88))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(431, 431, 431))))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {card1, card2, card3, card4});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(27, 27, 27)
                .addComponent(lblNamaUserLog)
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(card4, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                    .addComponent(card1, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                    .addComponent(card2, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                    .addComponent(card3, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE))
                .addGap(93, 93, 93)
                .addComponent(btnVote)
                .addContainerGap(179, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {card1, card2, card3, card4});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblFoto1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFoto1MouseClicked
        // TODO add your handling code here:
        setPilihanKandidat(1);
    }//GEN-LAST:event_lblFoto1MouseClicked

    private void lblFoto2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFoto2MouseClicked
        // TODO add your handling code here:
        setPilihanKandidat(2);
    }//GEN-LAST:event_lblFoto2MouseClicked

    private void lblFoto3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFoto3MouseClicked
        // TODO add your handling code here:
        setPilihanKandidat(3);
    }//GEN-LAST:event_lblFoto3MouseClicked

    private void lblFoto4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFoto4MouseClicked
        // TODO add your handling code here:
        setPilihanKandidat(4);
    }//GEN-LAST:event_lblFoto4MouseClicked

    private void btnExpand1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpand1ActionPerformed
        // TODO add your handling code here:
        javax.swing.JTextArea ta = new javax.swing.JTextArea(10, 30);
        ta.setText("VISI:\n" + visiLengkap[1] + "\n\nMISI:\n" + misiLengkap[1]);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setEditable(false);

        // Menampilkan gambar di popup
        javax.swing.ImageIcon iconDetail = null;
        if (pathFoto[1] != null) {
            java.awt.Image img = new javax.swing.ImageIcon(pathFoto[1]).getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            iconDetail = new javax.swing.ImageIcon(img);
        }

        javax.swing.JOptionPane.showMessageDialog(this, new javax.swing.JScrollPane(ta), "Profil Lengkap: " + namaLengkap[1], javax.swing.JOptionPane.INFORMATION_MESSAGE, iconDetail);
    }//GEN-LAST:event_btnExpand1ActionPerformed

    private void btnExpand2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpand2ActionPerformed
        // TODO add your handling code here:
        javax.swing.JTextArea ta = new javax.swing.JTextArea(10, 30);
        ta.setText("VISI:\n" + visiLengkap[2] + "\n\nMISI:\n" + misiLengkap[2]);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setEditable(false);

        // Menampilkan gambar di popup
        javax.swing.ImageIcon iconDetail = null;
        if (pathFoto[2] != null) {
            java.awt.Image img = new javax.swing.ImageIcon(pathFoto[2]).getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            iconDetail = new javax.swing.ImageIcon(img);
        }

        javax.swing.JOptionPane.showMessageDialog(this, new javax.swing.JScrollPane(ta), "Profil Lengkap: " + namaLengkap[2], javax.swing.JOptionPane.INFORMATION_MESSAGE, iconDetail);
    }//GEN-LAST:event_btnExpand2ActionPerformed

    private void btnExpand3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpand3ActionPerformed
        // TODO add your handling code here:
        javax.swing.JTextArea ta = new javax.swing.JTextArea(10, 30);
        ta.setText("VISI:\n" + visiLengkap[3] + "\n\nMISI:\n" + misiLengkap[3]);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setEditable(false);

        // Menampilkan gambar di popup
        javax.swing.ImageIcon iconDetail = null;
        if (pathFoto[1] != null) {
            java.awt.Image img = new javax.swing.ImageIcon(pathFoto[3]).getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            iconDetail = new javax.swing.ImageIcon(img);
        }

        javax.swing.JOptionPane.showMessageDialog(this, new javax.swing.JScrollPane(ta), "Profil Lengkap: " + namaLengkap[3], javax.swing.JOptionPane.INFORMATION_MESSAGE, iconDetail);
    }//GEN-LAST:event_btnExpand3ActionPerformed

    private void btnExpand4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpand4ActionPerformed
        // TODO add your handling code here:
        javax.swing.JTextArea ta = new javax.swing.JTextArea(10, 30);
        ta.setText("VISI:\n" + visiLengkap[4] + "\n\nMISI:\n" + misiLengkap[4]);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setEditable(false);

        // Menampilkan gambar di popup
        javax.swing.ImageIcon iconDetail = null;
        if (pathFoto[4] != null) {
            java.awt.Image img = new javax.swing.ImageIcon(pathFoto[4]).getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            iconDetail = new javax.swing.ImageIcon(img);
        }

        javax.swing.JOptionPane.showMessageDialog(this, new javax.swing.JScrollPane(ta), "Profil Lengkap: " + namaLengkap[4], javax.swing.JOptionPane.INFORMATION_MESSAGE, iconDetail);
    }//GEN-LAST:event_btnExpand4ActionPerformed

    private void btnVoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoteActionPerformed
        // TODO add your handling code here:
        // 1. Validasi jika pemilih belum mengklik foto kandidat manapun
        if (kandidatTerpilih.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Silakan klik foto salah satu kandidat terlebih dahulu!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Kotak Konfirmasi (Double Check) agar tidak salah klik
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin memberikan suara kepada Kandidat Nomor Urut " + kandidatTerpilih + "?\n(Pilihan Anda tidak dapat diubah kembali setelah ini!)", 
                "Konfirmasi Hak Suara", 
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);

        // 3. Jika pemilih menekan tombol "YES"
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                java.sql.Connection conn = Config.configDB();

                // SQL A: Menambah perolehan suara kandidat sebanyak +1
                String sqlVote = "UPDATE tb_kandidat SET jumlah_suara = jumlah_suara + 1 WHERE no_urut = ?";
                java.sql.PreparedStatement pstVote = conn.prepareStatement(sqlVote);
                pstVote.setString(1, kandidatTerpilih); // Variabel nomor urut dari foto yang diklik
                pstVote.execute();

                // SQL B: Mengubah status_vote pemilih menjadi 'Sudah' berdasarkan NIM-nya
                String sqlStatus = "UPDATE tb_pemilih SET status_vote = 'Sudah' WHERE nim = ?";
                java.sql.PreparedStatement pstStatus = conn.prepareStatement(sqlStatus);
                pstStatus.setString(1, nimPemilihAktif); 
                pstStatus.execute();

                // 4. Notifikasi Sukses & Keluar Otomatis
                javax.swing.JOptionPane.showMessageDialog(this, "Terima kasih! Hak suara Anda telah berhasil disimpan.");

                this.dispose(); // Menutup halaman pemilihan
                new FormLogin().setVisible(true); // Membuka kembali Form Login untuk user berikutnya

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Terjadi kesalahan sistem saat memproses suara: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnVoteActionPerformed

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
            java.util.logging.Logger.getLogger(FormPemilih.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormPemilih.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormPemilih.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormPemilih.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FormPemilih("").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExpand1;
    private javax.swing.JButton btnExpand2;
    private javax.swing.JButton btnExpand3;
    private javax.swing.JButton btnExpand4;
    private javax.swing.JButton btnVote;
    private javax.swing.JPanel card1;
    private javax.swing.JPanel card2;
    private javax.swing.JPanel card3;
    private javax.swing.JPanel card4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblFoto1;
    private javax.swing.JLabel lblFoto2;
    private javax.swing.JLabel lblFoto3;
    private javax.swing.JLabel lblFoto4;
    private javax.swing.JLabel lblNama1;
    private javax.swing.JLabel lblNama2;
    private javax.swing.JLabel lblNama3;
    private javax.swing.JLabel lblNama4;
    private javax.swing.JLabel lblNamaUserLog;
    private javax.swing.JTextArea txtPreview1;
    private javax.swing.JTextArea txtPreview2;
    private javax.swing.JTextArea txtPreview3;
    private javax.swing.JTextArea txtPreview4;
    // End of variables declaration//GEN-END:variables
}
