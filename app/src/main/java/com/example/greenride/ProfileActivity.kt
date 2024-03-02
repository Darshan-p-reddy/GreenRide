package com.example.greenride

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.greenride.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var profileImageView: CircleImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var profilePhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        storageReference = FirebaseStorage.getInstance().getReference("Users/${auth.currentUser?.uid}")

        binding.savebutton.setOnClickListener {
            val nametext = binding.nametext.text.toString()
            val emailtext = binding.emailtext.text.toString()
            val phnotext = binding.phnotext.text.toString()

            if (isValidPhoneNumber(phnotext)) {
                val user = User(nametext, emailtext, phnotext)
                if (uid != null) {
                    databaseReference.child(uid).setValue(user).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            uploadProfilePic()
                        } else {
                            Toast.makeText(this@ProfileActivity, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@ProfileActivity, "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        nameEditText = findViewById(R.id.nametext)
        emailEditText = findViewById(R.id.emailtext)
        phoneEditText = findViewById(R.id.phnotext)
        profileImageView = findViewById(R.id.profileimage4)

        profileImageView.setOnClickListener {
            openGallery()
        }

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val phoneNo = intent.getStringExtra("phoneNo")

        nameEditText.setText(name)
        emailEditText.setText(email)
        phoneEditText.setText(phoneNo)

        val saveButton: Button = findViewById(R.id.savebutton)
        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString()
            val updatedEmail = emailEditText.text.toString()
            val updatedPhoneNo = phoneEditText.text.toString()

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfilePic() {
        profilePhotoUri?.let { uri ->
            val imageRef = storageReference.child("profile.jpg")
            imageRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
                // Get the download URL for the uploaded image
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save the download URL to the database along with the user data
                    saveUserData(downloadUri.toString())
                }.addOnFailureListener {
                    Toast.makeText(this@ProfileActivity, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this@ProfileActivity, "Failed to upload the image", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this@ProfileActivity, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData(imageUrl: String) {
        val uid = auth.currentUser?.uid
        val nametext = binding.nametext.text.toString()
        val emailtext = binding.emailtext.text.toString()
        val phnotext = binding.phnotext.text.toString()

        val user = User(nametext, emailtext, phnotext, imageUrl)
        if (uid != null) {
            databaseReference.child(uid).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile successfully updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, RC_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            profilePhotoUri = data.data
            profilePhotoUri?.let {
                Glide.with(this).load(it).into(profileImageView)
            }
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^\\+(?:[0-9] ?){6,14}[0-9]$"))
    }

    companion object {
        private const val RC_SELECT_IMAGE = 123
    }
}
