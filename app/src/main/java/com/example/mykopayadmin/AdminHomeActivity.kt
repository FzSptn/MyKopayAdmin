package com.example.mykopayadmin

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var rvAdmin: RecyclerView
    private lateinit var adapter: AdminAdapter

    // listData = yang ditampilkan di layar
    private val listData = ArrayList<PengajuanModel>()
    // listBackup = database lengkap (untuk filter)
    private val listBackup = ArrayList<PengajuanModel>()

    private lateinit var tvEmpty: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // Tombol Filter
    private lateinit var btnBaru: MaterialButton
    private lateinit var btnProses: MaterialButton
    private lateinit var btnSelesai: MaterialButton

    // Status yang sedang aktif (Default: Baru)
    private var currentStatusFilter = "Baru"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        initViews()
        setupRecyclerView()

        // Ambil Semua Data
        fetchData()
    }

    private fun initViews() {
        rvAdmin = findViewById(R.id.rv_admin)
        tvEmpty = findViewById(R.id.tv_empty)
        swipeRefresh = findViewById(R.id.swipe_refresh_home)

        btnBaru = findViewById(R.id.btn_nav_ajuan)    // Tombol "Baru"
        btnProses = findViewById(R.id.btn_nav_proses)  // Tombol "Proses"
        btnSelesai = findViewById(R.id.btn_nav_history)// Tombol "Selesai"

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        // --- LOGIKA KLIK TOMBOL FILTER ---
        btnBaru.setOnClickListener {
            filterData("Baru")
            updateButtonUI(btnBaru)
        }

        btnProses.setOnClickListener {
            filterData("Proses") // Menangani "Proses" & "Dalam Proses"
            updateButtonUI(btnProses)
        }

        btnSelesai.setOnClickListener {
            filterData("Selesai") // Menangani "Selesai", "Diterima", "Ditolak"
            updateButtonUI(btnSelesai)
        }

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_dark)
        swipeRefresh.setOnRefreshListener {
            fetchData()
        }
    }

    private fun setupRecyclerView() {
        rvAdmin.layoutManager = LinearLayoutManager(this)
        adapter = AdminAdapter(listData)
        rvAdmin.adapter = adapter
    }

    private fun fetchData() {
        val ref = FirebaseDatabase.getInstance().getReference("Pengajuan")

        Handler(Looper.getMainLooper()).postDelayed({ stopLoading() }, 8000)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listBackup.clear() // Bersihkan data cadangan

                // Variabel Hitung Jumlah
                var countBaru = 0
                var countProses = 0
                var countSelesai = 0

                if (snapshot.exists()) {
                    for (userFolder in snapshot.children) {
                        for (tiketSnapshot in userFolder.children) {
                            try {
                                val data = tiketSnapshot.getValue(PengajuanModel::class.java)
                                if (data != null) {
                                    // Masukkan semua ke list cadangan
                                    listBackup.add(data)

                                    // Hitung Jumlah per Kategori
                                    when (data.status) {
                                        "Baru", "Menunggu" -> countBaru++
                                        "Proses", "Dalam Proses" -> countProses++
                                        "Selesai", "Diterima", "Ditolak" -> countSelesai++
                                    }
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    // Sortir dari terbaru
                    listBackup.sortByDescending { it.no_tiket }
                }

                // --- UPDATE TEKS TOMBOL (ANGKA DI KIRI) ---
                btnBaru.text = "$countBaru Baru"
                btnProses.text = "$countProses Proses"
                btnSelesai.text = "$countSelesai Selesai"

                // Terapkan filter yang sedang aktif saat ini (agar tidak reset ke Baru terus)
                filterData(currentStatusFilter)

                // Update warna tombol awal
                when(currentStatusFilter) {
                    "Baru" -> updateButtonUI(btnBaru)
                    "Proses" -> updateButtonUI(btnProses)
                    "Selesai" -> updateButtonUI(btnSelesai)
                }

                stopLoading()
            }

            override fun onCancelled(error: DatabaseError) {
                stopLoading()
                Toast.makeText(applicationContext, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- FUNGSI FILTER LIST ---
    private fun filterData(statusKey: String) {
        currentStatusFilter = statusKey
        listData.clear()

        for (item in listBackup) {
            val status = item.status ?: ""

            // Logika pencocokan status yang fleksibel
            val isMatch = when (statusKey) {
                "Baru" -> status == "Baru" || status == "Menunggu"
                "Proses" -> status == "Proses" || status == "Dalam Proses"
                "Selesai" -> status == "Selesai" || status == "Diterima" || status == "Ditolak"
                else -> true // Tampilkan semua jika key tidak dikenal
            }

            if (isMatch) {
                listData.add(item)
            }
        }

        adapter.notifyDataSetChanged()

        // Cek Kosong
        if (listData.isEmpty()) {
            tvEmpty.text = "Tidak ada data '$statusKey'"
            tvEmpty.visibility = View.VISIBLE
            rvAdmin.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvAdmin.visibility = View.VISIBLE
        }
    }

    // --- FUNGSI GANTI WARNA TOMBOL ---
    private fun updateButtonUI(activeButton: MaterialButton) {
        // Reset semua tombol ke warna putih (Teks Hitam)
        setButtonState(btnBaru, false)
        setButtonState(btnProses, false)
        setButtonState(btnSelesai, false)

        // Set tombol aktif ke warna Biru (Teks Putih)
        setButtonState(activeButton, true)
    }

    private fun setButtonState(btn: MaterialButton, isActive: Boolean) {
        if (isActive) {
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))
            btn.setTextColor(Color.WHITE)
        } else {
            btn.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.white)
            btn.setTextColor(Color.BLACK)
        }
    }

    private fun stopLoading() {
        if (swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = false
        }
    }
}