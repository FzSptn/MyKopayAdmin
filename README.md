# MyKopay Admin 🛠️

Aplikasi Panel Admin khusus untuk pengurus Desa Koto Panjang (Kopay). Aplikasi ini berfungsi sebagai pusat kendali untuk memproses, memvalidasi, dan mengelola seluruh permohonan surat administrasi yang diajukan oleh warga melalui aplikasi MyKopay User.

> **Catatan:** Repository ini adalah bagian *backend/management* untuk Admin. Untuk sisi warga, silakan cek repository [MyKopay User].

## 📸 Screenshots

| Admin Login | Dashboard Proses | Detail & Verifikasi |
|:---:|:---:|:---:|
| <img width="626" height="1309" alt="image" src="https://github.com/user-attachments/assets/6d411c40-47b4-4bd1-9d93-d5490e85c461" />
/> | <img width="619" height="1314" alt="image" src="https://github.com/user-attachments/assets/70d5b71f-36d8-413e-880b-3f40033dbdac" />
/> | <img width="618" height="1314" alt="image" src="https://github.com/user-attachments/assets/11061bf1-494d-4dba-b00f-1dda8b42f092" />
 /> |

## ✨ Fitur Utama Admin

* **Login Admin & Keamanan**: Masuk menggunakan kredensial admin disertai verifikasi CAPTCHA matematika untuk mencegah akses bot.
* **Manajemen Pengajuan**: Melihat daftar permohonan warga secara *real-time* termasuk nama pemohon, NIK, jenis layanan, dan detail keperluan.
* **Verifikasi & Aksi**: Memberikan keputusan terhadap pengajuan (ACC/Terima atau Tolak) disertai dengan catatan admin.
* **Draf Surat**: Fitur *Preview Surat (Draft)* untuk melihat tampilan dokumen sebelum diselesaikan atau dicetak.

## 📱 Spesifikasi Sistem (Admin App)

Aplikasi admin dirancang untuk berjalan pada perangkat operasional kantor desa dengan spesifikasi:

### Perangkat Keras (Hardware)
* **Sistem Operasi**: Android 7.0 (Nougat) atau lebih baru.
* **RAM**: Minimal 3GB (Disarankan 4GB untuk kelancaran multitasking).
* **Koneksi**: Koneksi internet stabil wajib tersedia untuk sinkronisasi Firebase.

## 🛠️ Teknologi & Integrasi

* **Bahasa**: Kotlin (Android Native).
* **Database**: Firebase Realtime Database (Terhubung ke URL: `https://mykopaykp-default-rtdb.asia-southeast1.firebasedatabase.app/`).
* **Version Control**: Git dengan branch `main` sebagai default branch.

## 💻 Panduan Pengembangan (Developer)

1.  **Clone Repository**
    ```bash
    git clone [https://github.com/username-anda/mykopay-admin.git](https://github.com/username-anda/mykopay-admin.git)
    ```

2.  **Konfigurasi SDK**
    * Pastikan Android Studio Anda versi terbaru.
    * Masukkan file `google-services.json` yang sesuai ke folder `app/`.

3.  **Sinkronisasi Git**
    Jika terjadi kendala saat *push*, pastikan untuk melakukan merge terlebih dahulu agar data lokal dan remote sinkron:
    ```bash
    git pull origin main
    git push origin main
    ```

## 📂 Struktur Data Firebase

Admin memiliki otoritas untuk mengubah field `status` dan menambahkan `catatan_admin` pada struktur berikut:

```json
{
  "Pengajuan": {
    "UserID": {
      "TiketID": {
        "nama": "Bro",
        "nik": "-",
        "layanan": "Surat Keterangan Kelahiran",
        "status": "Proses Pengajuan",
        "catatan_admin": "Catatan dari pengurus desa..."
      }
    }
  }
}
