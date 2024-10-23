package com.nfs.tec_rfid.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nfs.tec_rfid.R
import com.nfs.tec_rfid.models.ItemRegistryResponse

class ItemRegistryAdapter(
    private var registries: List<ItemRegistryResponse>,
    private val onItemClick: (ItemRegistryResponse) -> Unit
) : RecyclerView.Adapter<ItemRegistryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemNameTextView: TextView = view.findViewById(R.id.item_name)
        val departmentRoomTextView: TextView = view.findViewById(R.id.department_room)
        val registryDateTextView: TextView = view.findViewById(R.id.registry_date)

        fun bind(registry: ItemRegistryResponse, onItemClick: (ItemRegistryResponse) -> Unit) {
            itemNameTextView.text = registry.item_name
            departmentRoomTextView.text = "${registry.department_name}, ${registry.room_name}"
            registryDateTextView.text = registry.registry_date.toString()

            itemView.setOnClickListener { onItemClick(registry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registry_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registry = registries[position]
        holder.bind(registry, onItemClick)
    }

    override fun getItemCount() = registries.size

    fun updateData(newRegistries: List<ItemRegistryResponse>) {
        registries = newRegistries
        notifyDataSetChanged()
    }
}
