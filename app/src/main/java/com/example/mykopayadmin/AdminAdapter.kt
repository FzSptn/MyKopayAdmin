package com.example.mykopayadmin

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
        // Pastikan ID ini sesuai dengan layout item_pengajuan_admin.xml
        val tvLayanan: TextView = itemView.findViewById(R.id.tv_item_layanan)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_item_status)
        val tvPemohon: TextView = itemView.findViewById(R.id.tv_pemohon)
        val tvTiket: TextView = itemView.findViewById(R.id.tv_item_tiket)
        val btnAction: Button = itemView.findViewById(R.id.btn_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengajuan_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listData[position]

        // 1. Set Data Teks
        holder.tvLayanan.text = data.layanan
        holder.tvStatus.text = data.status ?: "Menunggu"
        holder.tvPemohon.text = "Pemohon: ${data.nama ?: "-"}"
        holder.tvTiket.text = "#${data.no_tiket}"

        // 2. Logika Warna Status & Teks Tombol
        when (data.status) {
            "Baru", "Menunggu" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")) // Orange
                holder.btnAction.text = "Proses Ajuan"
                holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))
            }
            "Proses", "Dalam Proses" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")) // Biru
                holder.btnAction.text = "Lanjut / Selesai" // Tombol berubah fungsi
                holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))
            }
            "Selesai", "Diterima" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Hijau
                holder.btnAction.text = "Lihat Detail"
                holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
            }
            "Ditolak" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#F44336")) // Merah
                holder.btnAction.text = "Lihat Detail"
                holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
            }
            else -> {
                holder.tvStatus.setTextColor(Color.GRAY)
                holder.btnAction.text = "Lihat"
            }
        }

        // 3. Klik Tombol Action
        holder.btnAction.setOnClickListener {
            bukaHalamanProses(holder, data)
        }

        // 4. Klik Card (Body) juga membuka detail
        holder.itemView.setOnClickListener {
            bukaHalamanProses(holder, data)
        }
    }

    private fun bukaHalamanProses(holder: ViewHolder, data: PengajuanModel) {
        val intent = Intent(holder.itemView.context, AdminProcessActivity::class.java)

        // Kirim Semua Data Penting
        intent.putExtra("TIKET", data.no_tiket)
        intent.putExtra("LAYANAN", data.layanan)
        intent.putExtra("NAMA", data.nama)
        intent.putExtra("NIK", data.nik)
        intent.putExtra("KEPERLUAN", data.keperluan)
        intent.putExtra("WAKTU", data.waktu)

        // PENTING: Mengirim USERNAME dan STATUS agar AdminProcessActivity tidak error
        intent.putExtra("STATUS", data.status)
        intent.putExtra("USERNAME", data.username_pemohon)

        holder.itemView.context.startActivity(intent)
    }

    override fun getItemCount(): Int = listData.size
}