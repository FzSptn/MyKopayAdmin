# MyKopay Admin - Dashboard Manajemen Layanan

MyKopay Admin adalah aplikasi khusus untuk administrator guna mengelola pengajuan layanan yang masuk dari aplikasi MyKopay User. Aplikasi ini memungkinkan admin untuk memantau, memverifikasi, dan mengubah status pengajuan secara real-time.

## 🛡️ Fitur Utama Admin

* **Dashboard Monitoring:** Tampilan ringkas jumlah pengajuan berdasarkan status:
    * 🆕 **Baru:** Pengajuan yang baru masuk dan belum diproses.
    * 🔄 **Proses:** Pengajuan yang sedang ditindaklanjuti.
    * ✅ **Selesai:** Pengajuan yang telah tuntas.
* **Filter Cepat:** Navigasi mudah antar kategori status tanpa perlu berpindah halaman.
* **Manajemen Tiket:**
    * Melihat detail lengkap pemohon (NIK, Nama, Jenis Layanan, Keperluan).
    * Mengubah status pengajuan (Proses, Selesai, atau Tolak).
* **Indikator Warna:** Status dibedakan dengan warna agar mudah dipindai (Orange, Biru, Hijau, Merah).
* **Auto-Refresh:** Fitur *Swipe-to-Refresh* untuk memperbarui data terbaru dari server.

## 🛠 Teknologi

* **Bahasa:** Kotlin
* **Database:** Firebase Realtime Database
* **Arsitektur:** MVVM (Model-View-ViewModel) concept in Activity
* **Komponen UI:**
    * SwipeRefreshLayout (Buffering)
    * RecyclerView & CardView
    * Material Design Buttons

## 🚀 Cara Instalasi

1.  **Clone Repository:**
    ```bash
    git clone [https://github.com/username-anda/MyKopay-Admin.git](https://github.com/username-anda/MyKopay-Admin.git)
    ```
2.  **Konfigurasi Firebase:**
    * Gunakan project Firebase yang **SAMA** dengan aplikasi User.
    * Unduh `google-services.json` dari console Firebase.
    * Masukkan file tersebut ke folder `app/`.
3.  **Jalankan:**
    * Sync Project dengan Gradle Files.
    * Run di Emulator atau Device Fisik.

## 📂 Alur Kerja Data

Aplikasi Admin membaca data dari *root* yang sama dengan User App, namun memiliki akses *Read/Write* ke semua folder user.

**Logika Perubahan Status:**
1.  Admin menerima data dengan status **"Baru"**.
2.  Admin menekan tombol "Proses" -> Status di Firebase berubah jadi **"Proses"**.
3.  Setelah selesai, Admin menekan "Selesai" -> Status berubah jadi **"Selesai"**.
4.  Perubahan ini langsung muncul di notifikasi/riwayat User App.

## 📸 Screenshot

*(Tambahkan screenshot Dashboard Admin dan Halaman Proses di sini)*

---
**Admin Dashboard untuk Kerja Praktek**
