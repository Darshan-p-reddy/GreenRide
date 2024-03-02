package com.example.greenride

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class my_rides : AppCompatActivity() {

    private lateinit var listViewRideHistory: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_rides)

        // Initialize ListView
        listViewRideHistory = findViewById(R.id.list_ride_history)

        // Dummy ride history data
        val rideHistory = listOf(
            "Date: April 18, 2024 | Time: 09:00 AM",
            "Pickup: 123 Main St | Drop: 456 Park Ave",
            "Fare: Rs150.00",
            "-----------------------------",
            "Date: April 17, 2024 | Time: 11:30 AM",
            "Pickup: 789 Elm St | Drop: 101 Oak St",
            "Fare: Rs200.00",
            "-----------------------------",
            "Date: April 16, 2024 | Time: 11:30 AM",
            "Pickup: 789 Elm St | Drop: 101 Oak St",
            "Fare: Rs250.00"
        )

        // Create and set adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rideHistory)
        listViewRideHistory.adapter = adapter
    }
}
