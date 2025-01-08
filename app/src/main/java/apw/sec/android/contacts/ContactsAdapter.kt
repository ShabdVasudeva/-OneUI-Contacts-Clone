package apw.sec.android.contacts

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import android.widget.SectionIndexer
import java.util.*
import kotlin.collections.ArrayList

class ContactsAdapter(
    private var contactList: List<Contact>,
    private var context: Context
) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>(), SectionIndexer {

    private var filteredList: MutableList<Contact> = ArrayList(contactList)
    private var sectionPositions: ArrayList<Int> = ArrayList()
    private var sections: Array<String> = arrayOf()

    init {
        updateSections()
    }

    class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.image)
        private val text: TextView = view.findViewById(R.id.name)
        private val number: TextView = view.findViewById(R.id.number)
        private val context: Context = view.context

        fun bind(contact: Contact) {
            text.text = contact.name
            number.text = contact.phoneNumber
            if (contact.photoUri != null) {
                Glide.with(context)
                    .load(contact.photoUri)
                    .apply(RequestOptions().transform(RoundedCorners(60)))
                    .placeholder(R.drawable.adapter_item)
                    .into(image)
            } else {
                image.setImageResource(R.drawable.adapter_item)
            }
        }
    }

    fun updateContacts(newContacts: List<Contact>) {
        contactList = newContacts
        filteredList = ArrayList(contactList)
        updateSections()
        notifyDataSetChanged()
    }

    fun filter(query: String?) {
        filteredList.clear()
        if (query.isNullOrEmpty()) {
            filteredList.addAll(contactList)
        } else {
            val filePattern: String = query.lowercase().trim()
            for (contact in contactList) {
                if (contact.name.lowercase().contains(filePattern) || contact.phoneNumber?.contains(filePattern) == true) {
                    filteredList.add(contact)
                }
            }
        }
        updateSections()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.contacts_adapter, parent, false)
        return ContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val contact: Contact = filteredList[position]
        holder.bind(contact)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, InfoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("contact", contact)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    override fun getSections(): Array<String> = sections

    override fun getSectionForPosition(position: Int): Int {
        for (i in sectionPositions.indices) {
            if (position < sectionPositions[i]) {
                return i - 1
            }
        }
        return sectionPositions.size - 1
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return if (sectionIndex >= 0 && sectionIndex < sectionPositions.size) {
            sectionPositions[sectionIndex]
        } else filteredList.size - 1
    }

    private fun updateSections() {
        val sectionSet = TreeSet<String>()
        sectionPositions.clear()
        for (i in filteredList.indices) {
            val name = filteredList[i].name
            if (name.isNotEmpty()) {
                val firstChar = name[0].uppercaseChar().toString()
                if (!sectionSet.contains(firstChar)) {
                    sectionSet.add(firstChar)
                    sectionPositions.add(i)
                }
            }
        }
        sections = sectionSet.toTypedArray()
    }
}