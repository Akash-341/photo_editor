package com.sunayanpradhan.photoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage

import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.sunayanpradhan.photoeditor.databinding.ActivityEditBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log


class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private var selectedImageUris: MutableList<Uri> = mutableListOf()
    private lateinit var imageAdapter: ImageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_edit)

        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(selectedImageUris)
        binding.recyclerView.adapter = imageAdapter
        val imageUris = intent.getStringArrayListExtra("imageUris")
        if (imageUris != null) {
            // If multiple image URIs are present, pass them to DsPhotoEditorActivity
            val dsPhotoEditorIntent = Intent(this, DsPhotoEditorActivity::class.java)
            dsPhotoEditorIntent.putStringArrayListExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, imageUris)
//            dsPhotoEditorIntent.putStringArrayListExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_INITIAL_PHOTO, imageUris)
            startActivityForResult(dsPhotoEditorIntent, 100)
        } else {
            // If a single image URI is present, pass it to DsPhotoEditorActivity
            val imageUri = intent.getStringExtra("imageUri").toString()
            val dsPhotoEditorIntent = Intent(this, DsPhotoEditorActivity::class.java)
            dsPhotoEditorIntent.data = imageUri.toUri()
            dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Edited Image")
            startActivityForResult(dsPhotoEditorIntent, 100)
        }
//        startActivityForResult(dsPhotoEditorIntent, 100)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

     /*   if (resultCode == RESULT_OK) {

            if(requestCode==100) {

                    val outputUri: Uri? = data!!.data


                Log.e( "onActivityResult: ",outputUri.toString())
                if (outputUri != null) {
                    createPdfFromImageUris(outputUri)
                    Log.e( "pdf ","PDFCLICKED" )
                }
            }
        }*/
       /* if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                val outputUri: Uri? = data!!.data

                // Add the selected image URI to the list
                outputUri?.let {
                    selectedImageUris.add(it)
                    imageAdapter.notifyDataSetChanged() // Notify the adapter that the data set has changed
                }

                // Handle PDF creation here if needed
                // createPdfFromImageUris(selectedImageUris)
            }
        }*/

        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                val outputUri: Uri? = data!!.data

                // Add the selected image URI to the list
                outputUri?.let {
                    selectedImageUris.add(it)
                    imageAdapter.notifyDataSetChanged() // Notify the adapter that the data set has changed
                }
                createPdfFromImageUris(selectedImageUris)
            }
        }
    }
    private fun createPdfFromImageUris(imageUris: List<Uri>) {
        val pdfDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "PDF_$timeStamp"
        val pdfFile = File(pdfDirectory, "$imageFileName.pdf")

        try {
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            pdfDocument.writer.compressionLevel = 5
            val document = Document(pdfDocument)

            for (imageUri in imageUris) {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val byteArrayOutputStream = ByteArrayOutputStream()
                inputStream?.use { input ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                    }
                }

                val imageData: ImageData = ImageDataFactory.create(byteArrayOutputStream.toByteArray())
                val image = Image(imageData)
                document.add(image)
            }

            document.close()
            pdfDocument.close()

            showToast("Images converted to PDF and saved as $imageFileName")
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle any errors
            showToast("Error creating PDF")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}