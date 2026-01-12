package com.example.mykopayadmin

import com.example.mykopayadmin.PengajuanModel
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminAdapter(private val listData: ArrayList<PengajuanModel>) :
    RecyclerView.Adapter<AdminAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Sesuaikan ID dengan XML yang Anda kirim
        val tvLayanan: TextView = itemView.findViewById(R.id.tv_item_layanan)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_item_status)
        val tvPemohon: TextView = itemView.findViewById(R.id.tv_pemohon) // ID Baru
        val tvTiket: TextView = itemView.findViewById(R.id.tv_item_tiket) // ID Baru
        val btnAction: Button = itemView.findViewById(R.id.btn_action)    // ID Baru
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Pastikan nama filenya item_admin.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengajuan_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listData[position]

        // 1. Set Data ke View
        holder.tvLayanan.text = data.layanan
        holder.tvStatus.text = data.status ?: "Menunggu"
        holder.tvPemohon.text = "Pemohon: ${data.nama ?: "-"}"
        holder.tvTiket.text = "#${data.no_tiket}"

        // 2. Ubah Warna Status
        when (data.status) {
            "Menunggu" -> holder.tvStatus.setTextColor(Color.parseColor("#FF9800")) // Orange
            "Dalam Proses" -> holder.tvStatus.setTextColor(Color.parseColor("#2196F3")) // Biru
            "Diterima" -> holder.tvStatus.setTextColor(Color.parseColor("#2196F3")) // Biru
            "Selesai" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Hijau
            "Ditolak" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336")) // Merah
        }

        // 3. Atur Teks & Fungsi Tombol
        holder.btnAction.setOnClickListener {
            bukaHalamanProses(holder, data)
        }

        // Opsional: Klik card-nya juga bisa buka detail
        holder.itemView.setOnClickListener {
            bukaHalamanProses(holder, data)
        }

        // Sembunyikan tombol jika sudah selesai (Opsional, biar rapi)
        if (data.status == "Selesai" || data.status == "Ditolak") {
            holder.btnAction.text = "Lihat Detail"
            holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
        } else {
            holder.btnAction.text = "Proses Ajuan"
            holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))
        }
    }

    private fun bukaHalamanProses(holder: ViewHolder, data: PengajuanModel) {
        val intent = Intent(holder.itemView.context, AdminProcessActivity::class.java)
        // Kirim semua data yang diperlukan
        intent.putExtra("TIKET", data.no_tiket)
        intent.putExtra("LAYANAN", data.layanan)
        intent.putExtra("NAMA", data.nama)
        intent.putExtra("NIK", data.nik)
        intent.putExtra("KEPERLUAN", data.keperluan)
        intent.putExtra("WAKTU", data.waktu)
        intent.putExtra("STATUS", data.status)
        intent.putExtra("USERNAME", data.username_pemohon)

        holder.itemView.context.startActivity(intent)
    }

    override fun getItemCount(): Int = listData.size
}