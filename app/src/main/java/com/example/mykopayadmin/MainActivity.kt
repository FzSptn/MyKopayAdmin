package com.example.mykopayadmin

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private var jawabanBenar = 0
    private lateinit var etCaptcha: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUser = findViewById<EditText>(R.id.et_admin_user)
        val etPass = findViewById<EditText>(R.id.et_admin_pass)

        val btnLogin = findViewById<MaterialButton>(R.id.btn_admin_login)
        val btnCekCaptcha = findViewById<TextView>(R.id.btn_captcha_check)
        etCaptcha = findViewById(R.id.et_captcha_input)

        // 2. Generate Captcha saat pertama buka
        generateCaptcha()

        // 3. Tombol Cek Captcha Kecil (Opsional)
        btnCekCaptcha.setOnClickListener {
            val input = etCaptcha.text.toString()
            if (input.isNotEmpty() && input.toIntOrNull() == jawabanBenar) {
                Toast.makeText(this, "Captcha Benar!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Hitungan Salah!", Toast.LENGTH_SHORT).show()
                generateCaptcha()
            }
        }

        // 4. Tombol Login Utama
        btnLogin.setOnClickListener {
            val username = etUser.text.toString().trim()
            val password = etPass.text.toString().trim()
            val captchaInput = etCaptcha.text.toString().trim()

            // --- A. Validasi Input Kosong ---
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username & Password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- B. Validasi Captcha ---
            if (captchaInput.isEmpty() || captchaInput.toIntOrNull() != jawabanBenar) {
                Toast.makeText(this, "Captcha Salah! Hitung ulang.", Toast.LENGTH_SHORT).show()
                generateCaptcha()
                return@setOnClickListener
            }

            // --- C. PROSES LOGIN FIREBASE ---
            // Kita cek ke database apakah username tersebut ada di folder "Admins"
            val database = FirebaseDatabase.getInstance().reference

            // Path: Admins -> [username yang diinput]
            database.child("Admins").child(username).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // 1. Username Ditemukan, sekarang ambil password dari database
                    val passwordDiDb = snapshot.child("password").value.toString()

                    // 2. Bandingkan password input dengan password database
                    if (password == passwordDiDb) {
                        // --- LOGIN SUKSES ---
                        Toast.makeText(this, "Login Admin Berhasil!", Toast.LENGTH_SHORT).show()

                        // Pindah ke Dashboard
                        val intent = Intent(this, AdminHomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Password Salah
                        Toast.makeText(this, "Password Admin Salah!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Username tidak ditemukan di node Admins
                    Toast.makeText(this, "Username Admin tidak terdaftar!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Gagal koneksi internet atau masalah database
                Toast.makeText(this, "Gagal koneksi database: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi Membuat Soal Matematika Random
    private fun generateCaptcha() {
        val angka1 = (1..10).random()
        val angka2 = (1..10).random()
        jawabanBenar = angka1 + angka2

        etCaptcha.setText("")
        etCaptcha.hint = "$angka1 + $angka2"
    }
}