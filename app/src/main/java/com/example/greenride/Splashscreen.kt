package com.example.greenride

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler

@Suppress("DEPRECATION")
class Splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)
Handler().postDelayed({
    val intent = Intent(this,LoginActivity::class.java)
    startActivity(intent)
    finish()
},3000)
    }
}