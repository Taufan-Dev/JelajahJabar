package com.taufan.projectakhir

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

object GlideHelper {
    /**
     * Loads an image URL using Glide, dynamically adding the ngrok skip warning header
     * if the URL belongs to our backend server. Falls back to a local drawable if loading fails or URL is empty.
     */
    fun loadImage(imageView: ImageView, url: String?, placeholderDrawable: Int) {
        if (url.isNullOrEmpty()) {
            imageView.setImageResource(placeholderDrawable)
            return
        }

        // Check if the URL targets the ngrok domain to attach the skip warning header
        val glideUrl = if (url.contains("ngrok-free.dev") || url.contains("ngrok.io")) {
            GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build()
            )
        } else {
            url
        }

        Glide.with(imageView.context)
            .load(glideUrl)
            .placeholder(placeholderDrawable)
            .error(placeholderDrawable)
            .into(imageView)
    }
}
