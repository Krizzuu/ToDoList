package com.pam.todolist

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var attachmentView: TextView

    init {
        attachmentView = itemView.findViewById(R.id.task_attachment)
    }
}