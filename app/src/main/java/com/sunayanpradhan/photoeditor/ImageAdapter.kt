/*
package com.sunayanpradhan.photoeditor

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ImageAdapter(private val imageUris: List<Uri>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = imageUris[position]
        holder.bind(imageUri)
    }

    override fun getItemCount(): Int = imageUris.size

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(imageUri: Uri) {

            // Use Picasso or another image loading library to load the image into ImageView
            Picasso.get().load(imageUri).into(itemView.findViewById(R.id.imageView) as ImageView)
        }
    }
}*/

package com.example.conversion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter
import com.sunayanpradhan.photoeditor.EditActivity
import com.sunayanpradhan.photoeditor.R

class ImageAdapter(private val imageUris: ArrayList<Uri?>, private val context: Context) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = imageUris[position]
        Glide.with(context)
            .load(imageUri)
            .into(holder.imageView)
        holder.imageView.setOnClickListener { v: View? ->
            val intent = Intent(context, EditActivity::class.java)
            intent.putExtra("imageUri", imageUri.toString())
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return imageUris.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView

        init {
            imageView = itemView.findViewById<ImageView>(R.id.imageView)
        }
    }
}
