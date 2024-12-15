package com.example.detaithuctap

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.detaithuctap.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo firebase
        auth = FirebaseAuth.getInstance()

        // Xử lý sự kiện khi ấn vào icon hiển thị mật khẩu
        binding.imgHienThiMatKhau.setOnClickListener {
            togglePasswordVisibility()
        }

        // Xử lý sự kiên khi ấn vào nút đăng ký
        binding.tvDangKy.setOnClickListener {
            val intent = Intent(this, DangKyActivity::class.java)
            startActivity(intent)
        }

        // Xử lý sự kiện khi ấn vào nút đăng nhập
        binding.btnDangNhap.setOnClickListener {
            val taiKhoan = binding.edtTaiKhoan.text.toString().trim()
            val matKhau = binding.edtMatKhau.text.toString().trim()

            if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (taiKhoan.matches(Regex("^\\+?[0-9]{10,13}$"))) {
                loginWithPhoneAndPassword(taiKhoan, matKhau)
            } else {
                Toast.makeText(this, "Tài khoản không chính xác", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Hàm xử lý hiển thị mật khẩu và ẩn mật khẩu
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.edtMatKhau.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.imgHienThiMatKhau.setImageResource(R.drawable.baseline_visibility_off_24)
        } else {
            binding.edtMatKhau.inputType = InputType.TYPE_CLASS_TEXT
            binding.imgHienThiMatKhau.setImageResource(R.drawable.baseline_visibility_24)
        }
        binding.edtMatKhau.setSelection(binding.edtMatKhau.text.length) // Giữ con trỏ ở cuối
        isPasswordVisible = !isPasswordVisible
    }

    // Hàm xác thực số điện thoại khi đăng nhập
    private fun loginWithPhoneAndPassword(phoneNumber: String, password: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").orderByChild("soDienThoai").equalTo(phoneNumber)
            .get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.children.first()
                    val storedPassword = user.child("matKhau").value.toString()

                    if (storedPassword == password) {
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, TrangChu::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Số điện thoại không tồn tại", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Lỗi đăng nhập: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}