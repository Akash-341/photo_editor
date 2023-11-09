package com.sunayanpradhan.photoeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.github.dhaval2404.imagepicker.ImagePicker
import com.sunayanpradhan.photoeditor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var imageUri: Uri? =null
    companion object {
        const val PICK_IMAGES_REQUEST_CODE = 123
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.selectImageCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGES_REQUEST_CODE)

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                if (data?.clipData != null) {
                    // Multiple images selected
                    val imageUris = mutableListOf<String>()
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        imageUris.add(uri.toString())
                    }
                    // Pass the list of image URIs to the next activity
                    val intent = Intent(this, EditActivity::class.java)
                    intent.putStringArrayListExtra("imageUris", ArrayList(imageUris))
                    startActivity(intent)
                } else if (data?.data != null) {
                    // Single image selected
                    val imageUri = data.data!!
                    val intent = Intent(this, EditActivity::class.java)
                    intent.putExtra("imageUri", imageUri.toString())
                    startActivity(intent)
                }
            } else {
                // Handle other cases or errors
            }
        } catch (e: Exception) {
            // Handle exceptions
        }
    }
}