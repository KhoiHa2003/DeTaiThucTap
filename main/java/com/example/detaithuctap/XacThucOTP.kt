package com.example.detaithuctap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.detaithuctap.databinding.ActivityXacThucOtpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase

class XacThucOTP : AppCompatActivity() {
    private lateinit var binding: ActivityXacThucOtpBinding
    private var storedVerificationId: String? = ""
    private lateinit var auth: FirebaseAuth
    private var userData: Map<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityXacThucOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Lấy mã xác minh
        storedVerificationId = intent.getStringExtra("storedVerificationId")
        // Lấy dữ liệu người dùng
        userData = intent.getSerializableExtra("userData") as? Map<String, String>
        // Xử lý sự kiện khi ấn xác thực otp
        binding.btnXacThuc.setOnClickListener {
            val otp = binding.edtNhapMaOTP.text.toString()
            if (otp.isNotEmpty()) {
                verifyPhoneNumberWithCode(storedVerificationId, otp)
            } else {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Hàm kiểm tra OTP và mã xác minh được gửi đến
    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        if (verificationId.isNullOrEmpty()) {
            Toast.makeText(this, "Mã xác minh không tồn tại!", Toast.LENGTH_SHORT).show()
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }
    // Hàm xử lý Xác thực OTP
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserToFirebase()
                    Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("failed", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Mã OTP không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
    // Hàm lưu thông tin người dùng vào firebase
    private fun saveUserToFirebase() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = database.child("users").push().key ?: return
        val user = userData ?: return

        database.child("users").child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase", "Dữ liệu đã được lưu thành công")
                } else {
                    Log.e("Firebase", "Lỗi lưu dữ liệu: ${task.exception?.message}")
                }
            }
    }
}
