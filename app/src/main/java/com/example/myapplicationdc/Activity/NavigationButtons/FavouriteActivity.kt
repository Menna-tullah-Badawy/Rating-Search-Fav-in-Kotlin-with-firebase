package com.example.myapplicationdc.Activity.NavigationButtons

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationdc.Adapters.TopDoctorAdapter
import com.example.myapplicationdc.Domain.DoctorModel
import com.example.myapplicationdc.R
import com.example.myapplicationdc.ViewModel.MainViewModel
import com.example.myapplicationdc.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var viewModel: MainViewModel // Corrected variable name
    private val doctorList = ArrayList<DoctorModel>()

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java) // Ensure you are using MainViewModel
        val adapter = TopDoctorAdapter(this, doctorList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Load doctors from Firebase
        viewModel.loadDoctors() // Call to load doctors
        loadFavorites()
    }

    private fun loadFavorites() {
        val preferences = getSharedPreferences("doctor_favorites", MODE_PRIVATE)

        // Observe data and load only favorites
        viewModel.doctor.observe(this) { allDoctors ->
            doctorList.clear()
            allDoctors.forEach { doctor ->
                if (preferences.getBoolean(doctor.Id.toString(), false)) {
                    doctorList.add(doctor)
                }
            }

            (binding.recyclerView.adapter as TopDoctorAdapter).notifyDataSetChanged()

            // Handle empty state
            binding.emptyFavoriteView.visibility = if (doctorList.isNotEmpty()) View.GONE else View.VISIBLE
        }
    }
}