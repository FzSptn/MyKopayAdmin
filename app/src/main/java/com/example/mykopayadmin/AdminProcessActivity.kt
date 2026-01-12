package com.example.mykopayadmin

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminProcessActivity : AppCompatActivity() {

    // Views
    private lateinit var tvLayanan: TextView
    private lateinit var tvTiket: TextView
    private lateinit var etCatatan: EditText

    // View untuk Surat (Sekarang Read-Only / Preview)
    private lateinit var tvPreviewSurat: TextView
    private lateinit var cardSurat: View

    // Tombol
    private lateinit var btnTerima: MaterialButton
    private lateinit var btnTolak: MaterialButton
    private lateinit var btnKirimOtomatis: MaterialButton // Tombol Baru
    private lateinit var layoutTombolAksi: LinearLayout

    // Data
    private var noTiket: String? = null
    private var statusSaatIni: String? = null
    private var usernamePemohon: String? = null
    private var namaPemohon: String? = null
    private var nikPemohon: String? = null
    private var layanan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_process)

        initViews()

        // Ambil Data Intent
        val intent = intent
        noTiket = intent.getStringExtra("TIKET")
        statusSaatIni = intent.getStringExtra("STATUS")
        usernamePemohon = intent.getStringExtra("USERNAME")
        namaPemohon = intent.getStringExtra("NAMA")
        nikPemohon = intent.getStringExtra("NIK")
        layanan = intent.getStringExtra("LAYANAN")

        // Set Text View
        tvLayanan.text = layanan
        tvTiket.text = "#$noTiket"
        findViewById<TextView>(R.id.tv_proc_nama).text = namaPemohon ?: "-"
        findViewById<TextView>(R.id.tv_proc_nik).text = nikPemohon ?: "-"
        findViewById<TextView>(R.id.tv_proc_keperluan).text = intent.getStringExtra("KEPERLUAN") ?: "-"

        setupUIBasedOnStatus(statusSaatIni)
        setupListeners()
    }

    private fun initViews() {
        tvLayanan = findViewById(R.id.tv_proc_layanan)
        tvTiket = findViewById(R.id.tv_proc_tiket)
        etCatatan = findViewById(R.id.et_catatan_admin)

        // Gunakan ID yang ada di layout sebelumnya (et_isi_surat kita ganti fungsinya jadi preview saja)
        // Atau Anda bisa ganti EditText di XML jadi TextView biar tidak bisa diedit
        // Di sini saya asumsikan pakai ID 'et_isi_surat' tapi kita disable
        cardSurat = findViewById(R.id.et_isi_surat) // Anggap ini area preview

        btnTerima = findViewById(R.id.btn_terima)
        btnTolak = findViewById(R.id.btn_tolak)
        btnKirimOtomatis = findViewById(R.id.btn_kirim_surat) // Pakai ID tombol kirim yg sudah ada
        layoutTombolAksi = findViewById(R.id.layout_tombol_aksi)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupUIBasedOnStatus(status: String?) {
        // Reset Visibility
        layoutTombolAksi.visibility = View.GONE
        btnKirimOtomatis.visibility = View.GONE
        cardSurat.visibility = View.GONE
        etCatatan.visibility = View.VISIBLE
        etCatatan.isEnabled = true

        when (status) {
            "Menunggu" -> {
                layoutTombolAksi.visibility = View.VISIBLE
            }
            "Dalam Proses" -> {
                // Sembunyikan catatan biasa
                etCatatan.visibility = View.GONE
                findViewById<View>(R.id.lbl_catatan).visibility = View.GONE

                // Munculkan Tombol Otomatis
                btnKirimOtomatis.text = "TERBITKAN SURAT OTOMATIS"
                btnKirimOtomatis.visibility = View.VISIBLE

                // Opsional: Tampilkan preview text jika layout mendukung
                cardSurat.visibility = View.VISIBLE
                if (cardSurat is EditText) {
                    (cardSurat as EditText).hint = "Preview: Surat akan dibuat otomatis oleh sistem..."
                    (cardSurat as EditText).isEnabled = false // User gak perlu ngetik
                }
            }
            "Selesai", "Ditolak" -> {
                etCatatan.isEnabled = false
            }
        }
    }

    private fun setupListeners() {
        btnTerima.setOnClickListener {
            updateStatusDatabase("Dalam Proses", etCatatan.text.toString(), null)
        }

        btnTolak.setOnClickListener {
            val catatan = etCatatan.text.toString()
            if (catatan.isEmpty()) {
                etCatatan.error = "Wajib isi alasan penolakan!"
                return@setOnClickListener
            }
            updateStatusDatabase("Ditolak", catatan, null)
        }

        // UPDATE PENTING: GENERATE TEXT OTOMATIS
        btnKirimOtomatis.setOnClickListener {
            val tanggal = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

            // Template Surat Otomatis
            val isiSuratOtomatis = """
                PEMERINTAH KABUPATEN/KOTA
                KECAMATAN PAYAKUMBUH
                ==========================================
                SURAT KETERANGAN
                Nomor: $noTiket/SK/${SimpleDateFormat("M/yyyy").format(Date())}
                
                Yang bertanda tangan di bawah ini, Kepala Admin Layanan MyKopay menerangkan bahwa:
                
                Nama : $namaPemohon
                NIK  : $nikPemohon
                
                Adalah benar telah mengajukan permohonan layanan $layanan dan data tersebut telah kami verifikasi.
                
                Demikian surat keterangan ini dibuat untuk dapat dipergunakan sebagaimana mestinya.
                
                Payakumbuh, $tanggal
                
                Admin Petugas
                (Tanda Tangan Valid)
            """.trimIndent()

            // Langsung Kirim
            updateStatusDatabase("Selesai", "-", isiSuratOtomatis)
        }
    }

    private fun updateStatusDatabase(statusBaru: String, catatan: String, isiSurat: String?) {
        if (noTiket == null || usernamePemohon == null) return

        val ref = FirebaseDatabase.getInstance().getReference("Pengajuan")
            .child(usernamePemohon!!)
            .child(noTiket!!)

        val updates = HashMap<String, Any>()
        updates["status"] = statusBaru

        if (isiSurat != null) {
            updates["isi_surat"] = isiSurat
        } else if (catatan.isNotEmpty()) {
            updates["catatan"] = catatan
        }

        ref.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Berhasil! Status: $statusBaru", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}