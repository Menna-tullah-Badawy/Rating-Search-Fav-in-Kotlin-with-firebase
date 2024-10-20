package com.example.myapplicationdc.Activity

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplicationdc.Domain.DoctorModel
import com.example.myapplicationdc.R
import com.example.myapplicationdc.databinding.ActivityDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var doctor: DoctorModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doctor = intent.getParcelableExtra("object") ?: return

        // Display doctor details
        binding.apply {
            titleTxt.text = doctor.Name
            specialTxt.text = doctor.Special
            Glide.with(this@DetailActivity)
                .load(doctor.Picture)
                .apply(RequestOptions().centerCrop())
                .into(img)
        }

        // Set up favorite button
        binding.favBtn.setColorFilter(resources.getColor(R.color.RED), PorterDuff.Mode.SRC_IN)

        // Get favorite state from SharedPreferences
        val preferences = getSharedPreferences("doctor_favorites", Context.MODE_PRIVATE)
        val isFavorite = preferences.getBoolean(doctor.Id.toString(), false)
        binding.favBtn.setImageResource(if (isFavorite) R.drawable.love else R.drawable.cardiogram)

        // Toggle favorite state on click
        binding.favBtn.setOnClickListener {
            val updatedState = !isFavorite
            preferences.edit().putBoolean(doctor.Id.toString(), updatedState).apply()

            // Update the favorite button image
            binding.favBtn.setImageResource(if (updatedState) R.drawable.love else R.drawable.cardiogram)

            // Here you would need to update the data in your list and notify the adapter
            // You can pass the position of the doctor in the list through intent if needed
            // Example: items[position] = doctor.copy(isFavorite = updatedState)
            // adapter.notifyItemChanged(position) // Uncomment if you have access to the adapter
        }

        // Get existing rating from Firebase
        getDoctorRating(doctor.Id.toString())

        // Handle rating bar changes
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            saveDoctorRating(doctor.Id.toString(), rating)
        }
    }

    private fun saveDoctorRating(doctorId: String, rating: Float) {
        val database = FirebaseDatabase.getInstance().getReference("Doctors/$doctorId")
        database.child("Rating").setValue(rating).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Rating saved successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save rating: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDoctorRating(doctorId: String) {
        val database = FirebaseDatabase.getInstance().getReference("Doctors/$doctorId")
        database.child("Rating").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val rating = dataSnapshot.getValue(Float::class.java) ?: 0f
                    binding.ratingBar.rating = rating // Set the rating to the RatingBar
                } else {
                    binding.ratingBar.rating = 0f // Default rating if none exists
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Failed to retrieve rating: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
