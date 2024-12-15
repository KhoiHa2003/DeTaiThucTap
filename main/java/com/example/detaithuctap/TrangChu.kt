package com.example.detaithuctap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.detaithuctap.databinding.ActivityTrangchuBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class TrangChu : AppCompatActivity() {

    private lateinit var binding: ActivityTrangchuBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrangchuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewpager
        bottomNavigationView = binding.navigationView

        val adapter = AdapterFragment(this)
        viewPager.adapter = adapter

        // Đăng ký callback khi trang viewpager thay đổi
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Cập nhật bottomNavication khi chuyển trang
                when (position) {
                    0 -> bottomNavigationView.menu.findItem(R.id.DonHang).isChecked = true
                    1 -> bottomNavigationView.menu.findItem(R.id.CauHinh).isChecked = true
                    2 -> bottomNavigationView.menu.findItem(R.id.Them).isChecked = true
                    3 -> bottomNavigationView.menu.findItem(R.id.HoTro).isChecked = true
                    4 -> bottomNavigationView.menu.findItem(R.id.CaNhan).isChecked = true
                }
            }
        })

        // Xử lý sự kiện khi chọn mục trong bottomNavication
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.DonHang -> viewPager.currentItem = 0
                R.id.CauHinh -> viewPager.currentItem = 1
                R.id.Them -> viewPager.currentItem = 2
                R.id.HoTro -> viewPager.currentItem = 3
                R.id.CaNhan -> viewPager.currentItem = 4
            }
            true
        }
    }
}
