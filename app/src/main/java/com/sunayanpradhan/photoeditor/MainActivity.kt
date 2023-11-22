package com.sunayanpradhan.photoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.conversion.ImageAdapter
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream

/*
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
}*/

 */

class MainActivity : AppCompatActivity() {
    private val selectedImages = ArrayList<Uri?>()
    private var recyclerView: RecyclerView? = null
    private var imageAdapter: ImageAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        imageAdapter = ImageAdapter(selectedImages, this)
        recyclerView?.setAdapter(imageAdapter)
        val pickImagesButton = findViewById<Button>(R.id.pickImagesButton)
        val pdfbutton = findViewById<Button>(R.id.button)
        pdfbutton.setOnClickListener { view: View? -> createPdfFromImages() }
        pickImagesButton.setOnClickListener { view: View? -> pickImages() }
    }

    private fun pickImages() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    selectedImages.add(imageUri) // Add selected image to the list
                    startCropActivity(
                        imageUri,
                        selectedImages.size - 1
                    ) // Start crop for the added image
                }
            } else if (data.data != null) {
                val imageUri = data.data
                selectedImages.add(imageUri) // Add selected image to the list
                startCropActivity(
                    imageUri,
                    selectedImages.size - 1
                ) // Start crop for the added image
            }
        } else if (requestCode >= UCROP_REQUEST_BASE && requestCode < UCROP_REQUEST_BASE + selectedImages.size) {
            val index = requestCode - UCROP_REQUEST_BASE
            val croppedUri = UCrop.getOutput(data!!)
            if (croppedUri != null) {
                selectedImages[index] = croppedUri // Replace with cropped image
                imageAdapter!!.notifyItemChanged(index)
            }
        } else if (requestCode == UCROP_REQUEST_BASE + selectedImages.size - 1 && resultCode == RESULT_OK && data != null) {
            val croppedUri = UCrop.getOutput(data)
            if (croppedUri != null) {
                selectedImages.add(croppedUri) // Add cropped image to the list
                imageAdapter!!.notifyItemInserted(selectedImages.size - 1)
            }
        }
    }

    private fun startCropActivity(sourceUri: Uri?, index: Int) {
        val options = UCrop.Options()
        options.setCompressionQuality(70)
        val destinationUri = Uri.fromFile(
            File(
                cacheDir,
                "cropped_image$index"
            )
        )
        UCrop.of(sourceUri!!, destinationUri)
            .withOptions(options)
            .start(this, UCROP_REQUEST_BASE + index)
    }

    private fun createPdfFromImages() {
        try {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(downloadsDir, "images.pdf")
            pdfFile.createNewFile()
            val pdfDocument = PdfDocument(PdfWriter(FileOutputStream(pdfFile)))
            val document = Document(pdfDocument)
            for (imageUri in selectedImages) {
                val imageData = ImageDataFactory.create(imageUri.toString())
                val image = Image(imageData)

                // Set a fixed size for all images (adjust width and height as needed)
                image.setWidth(500f)
                image.setHeight(700f) // Set height in points
                document.add(image)
                document.add(Paragraph("\n")) // Add some space between images
            }
            document.close()
            openGeneratedPdf(pdfFile) // Open the generated PDF
            Log.d("PDFCreation", "PDF created successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PDFCreation", "Error creating PDF: " + e.message)
        }
    }

    private fun openGeneratedPdf(pdfFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName + ".provider",
            pdfFile
        )
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    companion object {
        private const val PICK_IMAGES_REQUEST = 1
        private const val UCROP_REQUEST = 2
        private const val UCROP_REQUEST_BASE = 1000
    }
}