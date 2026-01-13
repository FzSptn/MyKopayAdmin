package com.example.mykopayadmin

import com.google.firebase.database.Exclude

data class PengajuanModel(
    var no_tiket: String? = "",
    var nama: String? = "",
    var nik: String? = "",
    var layanan: String? = "",
    var keperluan: String? = "",
    var waktu: String? = "",
    var status: String? = "",

    // Field for PDF link (Specific to Admin/User flow)
    val file_hasil_url: String? = null,

    // Helper to track whose data this is (Admin specific)
    @get:Exclude
    var username_pemohon: String? = null
)