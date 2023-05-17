package com.pam.todolist

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso


class AttachmentAdapter(
    private var attachments: List<Uri>,
    private val listener: OnClickListener
) : RecyclerView.Adapter<AttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.attachment_view, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val imageUri: Uri = attachments[position]

//        Glide.with(holder.itemView.context)
//            .load(imageUri)
//            .into(holder.attachmentImageView)
        Picasso.get()
            .load(imageUri)
            .into(holder.attachmentImageView)

        holder.itemView.setOnLongClickListener {
            listener.onLongClick(imageUri, position)
            true
        }
    }

    interface OnClickListener {
        fun onLongClick(uri : Uri, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAttachments(newAttachments: List<Uri>) {
        attachments = newAttachments
        this.notifyDataSetChanged()
    }
}
