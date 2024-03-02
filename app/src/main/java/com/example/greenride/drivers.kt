package com.example.greenride

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class drivers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)

        val button1 = findViewById<Button>(R.id.button2)
        button1.setOnClickListener {
            launchMainActivity("Raju")
        }

        val button2 = findViewById<Button>(R.id.button3)
        button2.setOnClickListener {
            launchMainActivity("Ali")
        }

        val button3 = findViewById<Button>(R.id.button4)
        button3.setOnClickListener {
            launchMainActivity("Ranjit")
        }

        val button4 = findViewById<Button>(R.id.button5)
        button4.setOnClickListener {
            launchMainActivity("Khan")
        }
    }

    private fun launchMainActivity(driverName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("driverName", driverName)
        }
        startActivity(intent)
        Toast.makeText(this, "Your ride with $driverName is confirmed", Toast.LENGTH_SHORT).show()
    }
}
