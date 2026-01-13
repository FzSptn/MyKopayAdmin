package com.example.mykopayadmin

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AdminProcessActivity : AppCompatActivity() {

    // Views
    private var tvNama: TextView? = null
    private var tvNik: TextView? = null
    private var tvLayanan: TextView? = null
    private var tvKeperluan: TextView? = null
    private var etCatatan: EditText? = null

    // Layout Wrapper untuk area aksi admin (Catatan + Tombol Tolak/Terima)
    private var layoutAksiAdmin: LinearLayout? = null

    private var btnTolak: MaterialButton? = null
    private var btnTerima: MaterialButton? = null
    private var btnPreview: MaterialButton? = null

    // Data Logic
    private var ticketId: String? = null
    private var usernamePemohon: String? = null
    private var currentStatus: String? = null

    // Data Surat
    private var namaPemohon: String = ""
    private var nikPemohon: String = ""
    private var layananPemohon: String = ""
    private var keperluanPemohon: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_process)

        initViews()
        getDataIntent()
        setupUI()

        btnTolak?.setOnClickListener { prosesUpdate("Ditolak") }

        btnTerima?.setOnClickListener {
            if (currentStatus == "Baru" || currentStatus == "Menunggu") {
                prosesUpdate("Proses")
            } else if (currentStatus == "Proses" || currentStatus == "Dalam Proses") {
                prosesUpdate("Selesai")
            }
        }

        btnPreview?.setOnClickListener {
            // Jika status selesai, preview dianggap arsip final, jika tidak dianggap draft
            createProPdf(isPreview = true)
        }
    }

    private fun initViews() {
        // Inisialisasi View sesuai ID di XML baru
        tvNama = findViewById(R.id.tv_detail_nama)
        tvNik = findViewById(R.id.tv_detail_nik)
        tvLayanan = findViewById(R.id.tv_detail_layanan)
        tvKeperluan = findViewById(R.id.tv_detail_keperluan)

        etCatatan = findViewById(R.id.et_catatan)
        layoutAksiAdmin = findViewById(R.id.layout_aksi_admin) // Layout pembungkus baru

        btnTolak = findViewById(R.id.btn_tolak)
        btnTerima = findViewById(R.id.btn_terima)
        btnPreview = findViewById(R.id.btn_preview_pdf)

        findViewById<View>(R.id.btn_back)?.setOnClickListener { finish() }
    }

    private fun getDataIntent() {
        val intent = intent
        namaPemohon = intent.getStringExtra("NAMA") ?: "-"
        nikPemohon = intent.getStringExtra("NIK") ?: "-"
        layananPemohon = intent.getStringExtra("LAYANAN") ?: "-"
        keperluanPemohon = intent.getStringExtra("KEPERLUAN") ?: "-"

        tvNama?.text = namaPemohon
        tvNik?.text = nikPemohon
        tvLayanan?.text = layananPemohon
        tvKeperluan?.text = keperluanPemohon

        ticketId = intent.getStringExtra("TIKET")
        usernamePemohon = intent.getStringExtra("USERNAME")
        currentStatus = intent.getStringExtra("STATUS")
    }

    // --- LOGIKA UI UTAMA ---
    private fun setupUI() {
        val status = currentStatus ?: "Baru"

        if (status == "Baru" || status == "Menunggu") {
            // TAHAP 1: BARU Masuk
            // Admin harus memproses atau menolak
            layoutAksiAdmin?.visibility = View.VISIBLE
            btnTerima?.text = "PROSES AJUAN"
            btnTerima?.backgroundTintList = getColorStateListCompat("#4CAF50") // Hijau

            // Belum bisa preview surat karena belum diproses
            btnPreview?.visibility = View.GONE
        }
        else if (status == "Proses" || status == "Dalam Proses") {
            // TAHAP 2: SEDANG DIPROSES
            // Admin bisa melihat draft PDF dan melakukan ACC Akhir
            layoutAksiAdmin?.visibility = View.VISIBLE
            btnTerima?.text = "SELESAI"
            btnTerima?.backgroundTintList = getColorStateListCompat("#2196F3") // Biru

            btnPreview?.visibility = View.VISIBLE
            btnPreview?.text = "PREVIEW SURAT (DRAFT)"
        }
        else if (status.contains("Selesai") || status.contains("Disetujui") || status.contains("ACC")) {
            // TAHAP 3: SELESAI
            // Admin tidak bisa edit lagi (layout aksi hilang), hanya bisa lihat arsip
            layoutAksiAdmin?.visibility = View.GONE

            btnPreview?.visibility = View.VISIBLE
            btnPreview?.text = "LIHAT ARSIP SURAT"
            btnPreview?.backgroundTintList = getColorStateListCompat("#4CAF50") // Hijau
        }
        else {
            // TAHAP 4: DITOLAK
            layoutAksiAdmin?.visibility = View.GONE
            btnPreview?.visibility = View.GONE
        }
    }

    // Helper untuk warna tombol
    private fun getColorStateListCompat(colorHex: String): android.content.res.ColorStateList {
        return android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorHex))
    }

    private fun prosesUpdate(statusBaru: String) {
        if (ticketId == null || usernamePemohon == null) {
            Toast.makeText(this, "Error: Data Tiket Hilang", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Pengajuan")
            .child(usernamePemohon!!)
            .child(ticketId!!)

        val updateData = HashMap<String, Any>()
        updateData["status"] = statusBaru

        val catatan = etCatatan?.text.toString().trim()
        if (catatan.isNotEmpty()) updateData["catatan_admin"] = catatan

        if (statusBaru == "Selesai") {
            val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
            updateData["tgl_selesai"] = sdf.format(Date())
            updateData["disetujui_oleh"] = "Admin Desa MyKopay"
        }

        ref.updateChildren(updateData).addOnSuccessListener {
            val msg = if (statusBaru == "Proses") "Pengajuan Diproses" else "Status: $statusBaru"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish() // Kembali ke list
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal update database", Toast.LENGTH_SHORT).show()
        }
    }

    // --- LOGIKA PDF (TIDAK BERUBAH) ---
    private fun createProPdf(isPreview: Boolean) {
        try {
            val directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val fileName = "Surat_${ticketId}.pdf"
            val file = File(directory, fileName)

            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            // Font Setup
            val fontHeader = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.BOLD)
            val fontSubHeader = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
            val fontBody = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
            val fontBold = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.BOLD)

            // 1. KOP SURAT
            val p1 = Paragraph("PEMERINTAH KOTA PAYAKUMBUH", fontHeader)
            p1.alignment = Element.ALIGN_CENTER
            document.add(p1)

            val p2 = Paragraph("KECAMATAN PAYAKUMBUH BARAT", fontSubHeader)
            p2.alignment = Element.ALIGN_CENTER
            document.add(p2)

            document.add(Paragraph(" "))
            val line = LineSeparator(); line.lineWidth = 2f
            document.add(line)
            document.add(Paragraph(" "))

            // 2. JUDUL SURAT
            val pJudul = Paragraph("SURAT KETERANGAN", fontBold)
            pJudul.alignment = Element.ALIGN_CENTER
            document.add(pJudul)

            val pNomor = Paragraph("Nomor: $ticketId", fontSubHeader)
            pNomor.alignment = Element.ALIGN_CENTER
            document.add(pNomor)
            document.add(Paragraph(" \n"))

            // 3. PEMBUKA
            val pPembuka = Paragraph("Yang bertanda tangan di bawah ini menerangkan bahwa:", fontBody)
            pPembuka.alignment = Element.ALIGN_JUSTIFIED
            document.add(pPembuka)
            document.add(Paragraph(" "))

            // 4. TABEL BIODATA
            val table = PdfPTable(3)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3f, 0.5f, 6.5f))

            addCellNoBorder(table, "Nama")
            addCellNoBorder(table, ":")
            addCellNoBorder(table, namaPemohon)

            addCellNoBorder(table, "NIK")
            addCellNoBorder(table, ":")
            addCellNoBorder(table, nikPemohon)

            addCellNoBorder(table, "Keperluan")
            addCellNoBorder(table, ":")
            addCellNoBorder(table, keperluanPemohon)

            val tglNow = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("id", "ID")).format(Date())
            addCellNoBorder(table, "Tanggal Pengajuan")
            addCellNoBorder(table, ":")
            addCellNoBorder(table, tglNow)

            document.add(table)
            document.add(Paragraph(" "))

            // 5. ISI LAYANAN
            val pMid = Paragraph("Adalah benar warga desa kami yang mengajukan layanan:", fontBody)
            document.add(pMid)
            document.add(Paragraph(" "))

            val pLayanan = Paragraph(layananPemohon.uppercase(), fontBold)
            pLayanan.alignment = Element.ALIGN_LEFT
            pLayanan.indentationLeft = 20f
            document.add(pLayanan)
            document.add(Paragraph(" "))

            // 6. PENUTUP
            val pPenutup = Paragraph("Demikian surat keterangan ini dibuat untuk digunakan sebagaimana mestinya.", fontBody)
            pPenutup.alignment = Element.ALIGN_JUSTIFIED
            document.add(pPenutup)
            document.add(Paragraph(" \n\n"))

            // 7. TANDA TANGAN
            val tableTTD = PdfPTable(2)
            tableTTD.widthPercentage = 100f

            tableTTD.addCell(getCellNoBorder(" "))

            val selTandaTangan = PdfPCell()
            selTandaTangan.border = Rectangle.NO_BORDER

            selTandaTangan.addElement(Paragraph("Mengetahui,", fontBody))
            selTandaTangan.addElement(Paragraph("Admin Desa,", fontBody))
            selTandaTangan.addElement(Paragraph("\n\n\n"))
            selTandaTangan.addElement(Paragraph("( ADMIN MYKOPAY )", fontBold))

            tableTTD.addCell(selTandaTangan)
            document.add(tableTTD)

            document.close()

            if (isPreview) openPDF(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCellNoBorder(table: PdfPTable, text: String) {
        val cell = PdfPCell(Phrase(text, Font(Font.FontFamily.TIMES_ROMAN, 12f)))
        cell.border = Rectangle.NO_BORDER
        cell.paddingBottom = 6f
        table.addCell(cell)
    }

    private fun getCellNoBorder(text: String): PdfPCell {
        val cell = PdfPCell(Phrase(text))
        cell.border = Rectangle.NO_BORDER
        return cell
    }

    private fun openPDF(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi pembuka PDF!", Toast.LENGTH_SHORT).show()
        }
    }
}