package com.example.detaithuctap

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdapterFragment(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    // Danh sách các Fragment để hiển thị
    private val fragments = listOf(
        DonHangFragment(),
        CauhinhFragment(),
        HanhDongFragment(),
        HoTroFragment(),
        CaNhanFragment()
    )

    // Trả về tổng số Fragment trong danh sách
    override fun getItemCount(): Int {
        return fragments.size
    }

    // Tạo Fragment tương ứng với vị trí được chọn
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}