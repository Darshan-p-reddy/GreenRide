package com.example.greenride

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.greenride.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth

    //override fun onStart() {
      //  super.onStart()
        //check if user already logged in
        //val currentUser : FirebaseUser?=auth.currentUser
        //if (currentUser != null){
          //  startActivity(Intent(this,MainActivity::class.java))
            //finish()
        //}
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Initialize Firebase auth
        auth= FirebaseAuth.getInstance()

        binding.signupbutton.setOnClickListener {
            val email = binding.Email.text.toString()
            val password = binding.Password.text.toString()
            
            if (email.isEmpty()||password.isEmpty()){
                Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email ,password)
                    .addOnCompleteListener{task ->
                        if(task.isSuccessful){
                            Toast.makeText(this, "Log-In Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this,MainActivity::class.java))
                        }
                        else{
                            Toast.makeText(this, "Sign-In Failed:${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }

                    }
            }

        }
        val signUpText = findViewById<TextView>(R.id.signUpText)
        signUpText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
