package com.example.greenride
// DriverAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.greenride.R

class DriverAdapter(private val drivers: List<Driver>) :
    RecyclerView.Adapter<DriverAdapter.DriverViewHolder>() {

    inner class DriverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views from item_driver.xml here
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false)
        return DriverViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        val currentDriver = drivers[position]
        // Bind driver data to views in item_driver.xml
    }

    override fun getItemCount() = drivers.size
}
