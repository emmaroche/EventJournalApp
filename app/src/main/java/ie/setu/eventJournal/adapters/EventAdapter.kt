package ie.setu.eventJournal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.eventJournal.R
import ie.setu.eventJournal.databinding.CardEventBinding
import ie.setu.eventJournal.models.EventModel
import androidx.core.net.toUri
import ie.setu.eventJournal.utils.customTransformation


interface EventClickListener {
    fun onEventClick(event: EventModel)
}

class EventAdapter constructor(
    private var events: ArrayList<EventModel>,
    private val listener: EventClickListener,
    private val readOnly: Boolean
) : RecyclerView.Adapter<EventAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding, readOnly)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val event = events[position]
        holder.bind(event, listener)
    }

    fun removeAt(position: Int) {
        events.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = events.size

    inner class MainHolder(val binding: CardEventBinding, private val readOnly: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        val readOnlyRow = readOnly

        fun bind(event: EventModel, listener: EventClickListener) {
            binding.root.tag = event
            binding.event = event

            // Load the image URI into imageIcon ImageView using Picasso
            Picasso.get()
                .load(event.image.toUri())
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .into(binding.imageIcon)

            binding.root.setOnClickListener { listener.onEventClick(event) }
            binding.executePendingBindings()
        }
    }
}