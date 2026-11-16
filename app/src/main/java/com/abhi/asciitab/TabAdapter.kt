package com.abhi.asciitab

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TabAdapter(
    private val tabs: List<Tab>,
    private val onSelect: (Tab) -> Unit,
    private val onClose: (Tab) -> Unit
) : RecyclerView.Adapter<TabAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val thumb: ImageView = v.findViewById(R.id.thumb)
        val title: TextView = v.findViewById(R.id.title)
        val close: Button = v.findViewById(R.id.closeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false))

    override fun getItemCount(): Int = tabs.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tab = tabs[position]
        holder.title.text = tab.title.ifBlank { "New Tab" }
        if (tab.thumbnail != null) holder.thumb.setImageBitmap(tab.thumbnail)
        else holder.thumb.setImageResource(android.R.color.black)
        holder.itemView.setOnClickListener { onSelect(tab) }
        holder.close.setOnClickListener { onClose(tab) }
    }
}
