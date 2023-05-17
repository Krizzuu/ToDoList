package com.pam.todolist

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var attachmentImageView: ImageView

    init {
        attachmentImageView = itemView.findViewById(R.id.task_attachment)
    }
}