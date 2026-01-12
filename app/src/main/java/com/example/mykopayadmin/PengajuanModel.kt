package com.example.mykopayadmin

import com.google.firebase.database.Exclude

data class PengajuanModel(
    val no_tiket: String? = null,
    val nama: String? = null,
    val nik: String? = null,
    val layanan: String? = null,
    val keperluan: String? = null,
    val status: String? = null,
    val waktu: String? = null,
    val catatan: String? = null,

    // Field for PDF link (Specific to Admin/User flow)
    val file_hasil_url: String? = null,

    // Helper to track whose data this is (Admin specific)
    @get:Exclude
    var username_pemohon: String? = null
)