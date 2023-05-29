package com.pam.todolist

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class AttachmentAdapter(
    private val context: Context,
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
        val attachmentUri: Uri = attachments[position]

        holder.attachmentView.text = attachmentUri.toString()

        holder.attachmentView.setOnLongClickListener {
            Log.i("LongClick", "Long Click Triggered")
            listener.onLongClick(attachmentUri, position)
            true
        }

        holder.attachmentView.setOnClickListener {
            Log.i("LongClick", "Click Triggered")
            listener.onClick(attachmentUri)
        }
    }

    interface OnClickListener {
        fun onClick(attachmentUri: Uri)
        fun onLongClick(uri : Uri, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAttachments(newAttachments: List<Uri>) {
        attachments = newAttachments
        this.notifyDataSetChanged()
    }
}
