package com.example.myapplicationdc.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplicationdc.Activity.DetailActivity
import com.example.myapplicationdc.Domain.DoctorModel
import com.example.myapplicationdc.R
import com.example.myapplicationdc.databinding.ViewholderTopDoctorBinding

class TopDoctorAdapter(private val context: Context?, private var items: MutableList<DoctorModel>) :
    RecyclerView.Adapter<TopDoctorAdapter.Viewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderTopDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val doctor = items[position]
        holder.binding.apply {
            nameTxt.text = doctor.Name
            specialTxt.text = doctor.Special
            scoreTxt.text = doctor.Rating.toString()
            yearTxt.text = "${doctor.Experience} Years"

            // Load image using Glide
            Glide.with(holder.itemView.context)
                .load(doctor.Picture)
                .apply(RequestOptions().centerCrop())
                .into(img)

            // Set favorite button color to red
            favBtn.setColorFilter(context?.resources?.getColor(R.color.RED) ?: 0, PorterDuff.Mode.SRC_IN)

            // Get favorite state from SharedPreferences
            val preferences = context?.getSharedPreferences("doctor_favorites", Context.MODE_PRIVATE)
            val isFavorite = preferences?.getBoolean(doctor.Id.toString(), false) ?: false
            favBtn.setImageResource(if (isFavorite) R.drawable.love else R.drawable.cardiogram)

            // Toggle favorite state on click
            favBtn.setOnClickListener {
                val updatedState = !isFavorite
                preferences?.edit()?.putBoolean(doctor.Id.toString(), updatedState)?.apply()

                // Update the favorite button image
                favBtn.setImageResource(if (updatedState) R.drawable.love else R.drawable.cardiogram)

                // Update the state in the items list if needed
                items[position] = doctor.copy(isFavorite = updatedState) // Ensure your DoctorModel has an isFavorite property

                // Notify the adapter that the item has changed
                notifyItemChanged(position)
            }

            // Navigate to doctor details
            holder.itemView.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("object", doctor)
                }
                context?.startActivity(intent)
            }
        }
    }

    // Add this function to update the list of doctors
    fun updateDoctors(newDoctors: List<DoctorModel>) {
        items.clear() // Clear the current list
        items.addAll(newDoctors) // Add the new doctors
        notifyDataSetChanged() // Notify the adapter to refresh the view
    }

    class Viewholder(val binding: ViewholderTopDoctorBinding) : RecyclerView.ViewHolder(binding.root)
}
