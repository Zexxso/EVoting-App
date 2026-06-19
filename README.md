# 🗳️ Aplikasi E-Voting (Desktop)

Aplikasi *Electronic Voting* (E-Voting) berbasis Desktop yang dikembangkan menggunakan **Java Swing** dan terintegrasi dengan basis data **MySQL**. Aplikasi ini dirancang untuk memfasilitasi proses pemilihan umum skala menengah seperti pemilihan ketua BEM kampus, ketua OSIS, maupun organisasi lainnya dengan sistem yang transparan, terpusat, dan *real-time*.

## ✨ Fitur Utama

Sistem ini membagi hak akses ke dalam dua aktor utama: **Admin** dan **Pemilih (User)**.

### 🛡️ Panel Admin
* **Dashboard *Real-time*:** Menampilkan *Quick Count* (grafik batang) perolehan suara secara langsung untuk maksimal 4 kandidat.
* **Manajemen Kandidat & Pemilih (Voter):** Mengelola data profil kandidat (Visi, Misi, Foto) dan mendaftarkan pemilih (NIM & Token).
* **Sistem Penguncian Sesi:** Admin memiliki kontrol penuh untuk membuka atau menutup sesi pemilihan (disimpan secara persisten di *database*).
* **Laporan Terintegrasi (Report/Result):** Menampilkan rincian suara yang masuk.

### 👤 Panel Pemilih (User)
* **Login Aman:** Autentikasi menggunakan NIM (atau NIK) dan Token unik yang diberikan oleh panitia.
* **Validasi Anti-Golput & Suara Ganda:** Sistem otomatis mengunci akun pemilih setelah mereka berhasil menggunakan hak suaranya (*One-time voting*).
* **UI/UX Modern (Card Layout):** Pemilih dapat melihat foto, nama, serta cuplikan visi & misi kandidat dalam bentuk *Card*. Terdapat tombol *Expand* untuk membaca profil kandidat secara lengkap.
* **Konfirmasi Pilihan:** Fitur *Double-check* sebelum suara dikirim ke dalam *database*.

---

## 🛠️ Teknologi yang Digunakan
* **Bahasa Pemrograman:** Java (JDK 8 / versi terbaru)
* **GUI Builder:** Java Swing (NetBeans IDE)
* **Database:** MySQL (phpMyAdmin / XAMPP)
* **Library Ekstra:** JDBC Driver (MySQL Connector)

---

## ⚙️ Cara Instalasi & Menjalankan

1. **Clone Repositori Ini:**
   Buka Terminal/Git Bash Anda dan ketikkan perintah berikut:
```bash
   git clone [https://github.com/Zexxso/EVoting-App.git](https://github.com/Zexxso/EVoting-App.git)
