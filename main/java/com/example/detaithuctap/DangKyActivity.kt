package com.example.detaithuctap

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.detaithuctap.databinding.ActivityDangKyBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class DangKyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDangKyBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var progressDialog: ProgressDialog

    private var userData: Map<String, String> = mapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityDangKyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Auth và Database
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        // Khởi tạo ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Đang xử lý...")
            setCancelable(false)
        }

        // Thiết lập callback cho xác minh số điện thoại
        setupCallbacks()

        // Xử lý sự kiện khi nhấn nút "Tiếp tục"
        binding.btnTiepTuc.setOnClickListener {
            val hoTen = binding.edtHoTen.text.toString().trim()
            val soDienThoai = binding.edtSDT.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val diaChi = binding.edtDiaChi.text.toString().trim()
            val tinh = binding.edtTinh.text.toString().trim()
            val quanHuyen = binding.edtQuanHuyen.text.toString().trim()
            val phuongXa = binding.edtPhuongXa.text.toString().trim()
            val matKhau = binding.edtMatKhau.text.toString().trim()
            val nhapLaiMatKhau = binding.edtNhapLaiMatKhau.text.toString().trim()

            if (hoTen.isEmpty() || soDienThoai.isEmpty() || email.isEmpty() || diaChi.isEmpty() ||
                tinh.isEmpty() || quanHuyen.isEmpty() || phuongXa.isEmpty() || matKhau.isEmpty() || nhapLaiMatKhau.isEmpty()
            ) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (matKhau != nhapLaiMatKhau) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lưu dữ liệu người dùng
            userData = mapOf(
                "hoTen" to hoTen,
                "soDienThoai" to soDienThoai,
                "email" to email,
                "diaChi" to diaChi,
                "tinh" to tinh,
                "quanHuyen" to quanHuyen,
                "phuongXa" to phuongXa,
                "matKhau" to matKhau
            )

            // Chuẩn hóa số điện thoại và bắt đầu xác minh
            val formattedPhoneNumber = formatPhoneNumber(soDienThoai)
            startPhoneNumberVerification(formattedPhoneNumber)
        }
    }

    // Hàm định dạng số điện thoại
    private fun formatPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.startsWith("0")) {
            phoneNumber.replaceFirst("0", "+84")
        } else if (!phoneNumber.startsWith("+")) {
            "+84$phoneNumber"
        } else {
            phoneNumber
        }
    }

    // Hàm bắt đầu xác minh số điện thoại
    private fun startPhoneNumberVerification(phoneNumber: String) {
        progressDialog.show()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Hàm thiết lập callback
    private fun setupCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                progressDialog.dismiss()
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                saveUserToDatabase(userId, userData)
                            }
                            val intent = Intent(this@DangKyActivity, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@DangKyActivity,
                                "Đăng nhập thất bại: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

            // Hàm xử lý khi xác thực số điện thoại thất bại
            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Toast.makeText(
                    this@DangKyActivity,
                    "Xác thực thất bại: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Hàm xử lý khi xác thực số điện thoại thành công
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                progressDialog.dismiss()
                storedVerificationId = verificationId
                resendToken = token

                val intent = Intent(this@DangKyActivity, XacThucOTP::class.java).apply {
                    putExtra("storedVerificationId", storedVerificationId)
                    putExtra("userData", HashMap(userData))
                }
                startActivity(intent)
            }
        }
    }

    // Hàm lưu dữ liệu người dùng vào Firebase Realtime Database
    private fun saveUserToDatabase(userId: String, userData: Map<String, String>) {
        databaseReference.child(userId).setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Không thể lưu dữ liệu: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
