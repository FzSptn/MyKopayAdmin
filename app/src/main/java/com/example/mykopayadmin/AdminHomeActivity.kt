package com.example.mykopayadmin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    // Variabel Data
    private val listData = ArrayList<PengajuanModel>()
    private val listBackup = ArrayList<PengajuanModel>()

    private lateinit var databaseRef: DatabaseReference
    private var databaseListener: ValueEventListener? = null

    private lateinit var tvEmpty: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btnBaru: MaterialButton
    private lateinit var btnProses: MaterialButton
    private lateinit var btnSelesai: MaterialButton

    private var currentStatusFilter = "Baru"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        databaseRef = FirebaseDatabase.getInstance().getReference("Pengajuan")

        initViews()
        setupRecyclerView()
        startRealtimeData()
    }

    private fun initViews() {
        rvAdmin = findViewById(R.id.rv_admin)
        tvEmpty = findViewById(R.id.tv_empty)
        swipeRefresh = findViewById(R.id.swipe_refresh_home)

        btnBaru = findViewById(R.id.btn_nav_ajuan)
        btnProses = findViewById(R.id.btn_nav_proses)
        btnSelesai = findViewById(R.id.btn_nav_history)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        btnBaru.setOnClickListener { filterData("Baru"); updateButtonUI(btnBaru) }
        btnProses.setOnClickListener { filterData("Proses"); updateButtonUI(btnProses) }
        btnSelesai.setOnClickListener { filterData("Selesai"); updateButtonUI(btnSelesai) }

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_dark)
        swipeRefresh.setOnRefreshListener { startRealtimeData() }
    }

    private fun setupRecyclerView() {
        rvAdmin.layoutManager = LinearLayoutManager(this)
        adapter = AdminAdapter(listData)
        rvAdmin.adapter = adapter
    }

    private fun startRealtimeData() {
        if (databaseListener != null) databaseRef.removeEventListener(databaseListener!!)

        databaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listBackup.clear()
                var countBaru = 0; var countProses = 0; var countSelesai = 0

                if (snapshot.exists()) {
                    // LOOP 1: Ambil Nama Folder (Ini adalah USERNAME)
                    for (userFolder in snapshot.children) {
                        val keyUsername = userFolder.key

                        // LOOP 2: Ambil Data Tiket
                        for (tiketSnapshot in userFolder.children) {
                            try {
                                val data = tiketSnapshot.getValue(PengajuanModel::class.java)
                                if (data != null) {
                                    // PENTING: Masukkan Username ke dalam Data Model
                                    data.username_pemohon = keyUsername

                                    listBackup.add(data)

                                    when (data.status) {
                                        "Baru", "Menunggu" -> countBaru++
                                        "Proses", "Dalam Proses" -> countProses++
                                        "Selesai", "Diterima", "Ditolak" -> countSelesai++
                                    }
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    listBackup.sortByDescending { it.no_tiket }
                }

                btnBaru.text = "$countBaru Baru"
                btnProses.text = "$countProses Proses"
                btnSelesai.text = "$countSelesai Selesai"

                filterData(currentStatusFilter)
                if (swipeRefresh.isRefreshing) swipeRefresh.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                if (swipeRefresh.isRefreshing) swipeRefresh.isRefreshing = false
            }
        }
        databaseRef.addValueEventListener(databaseListener!!)

        when(currentStatusFilter) {
            "Baru" -> updateButtonUI(btnBaru)
            "Proses" -> updateButtonUI(btnProses)
            "Selesai" -> updateButtonUI(btnSelesai)
        }
    }

    private fun filterData(statusKey: String) {
        currentStatusFilter = statusKey
        listData.clear()
        for (item in listBackup) {
            val status = item.status ?: ""
            val isMatch = when (statusKey) {
                "Baru" -> status == "Baru" || status == "Menunggu"
                "Proses" -> status == "Proses" || status == "Dalam Proses"
                "Selesai" -> status == "Selesai" || status == "Diterima" || status == "Ditolak"
                else -> true
            }
            if (isMatch) listData.add(item)
        }
        adapter.notifyDataSetChanged()

        if (listData.isEmpty()) {
            tvEmpty.text = "Tidak ada data '$statusKey'"
            tvEmpty.visibility = View.VISIBLE; rvAdmin.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE; rvAdmin.visibility = View.VISIBLE
        }
    }

    private fun updateButtonUI(activeButton: MaterialButton) {
        setButtonState(btnBaru, false); setButtonState(btnProses, false); setButtonState(btnSelesai, false)
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

    override fun onDestroy() {
        super.onDestroy()
        if (databaseListener != null) databaseRef.removeEventListener(databaseListener!!)
    }
}