package com.example.sub

import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class contactListAdapter(contactList: ArrayList<Contact>, onClickListener: profileFragment) :
    RecyclerView.Adapter<contactListAdapter.ViewHolder>() {
    interface ListItemClickListener {
        fun onListItemClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        val contactName: TextView
        val contactNumber: TextView

        override fun onClick(v: View) {
            val position = layoutPosition
            mOnClickListener!!.onListItemClick(position)
        }

        init {
            contactName = view.findViewById<View>(R.id.contactListName) as TextView
            contactNumber = view.findViewById<View>(R.id.contactListNumber) as TextView
            view.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_contact_list, viewGroup, false)
        return ViewHolder(view)
    }



    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        viewHolder.contactName.setText(contactList_!![position].number)
        viewHolder.contactNumber
              .setText(contactList_!![position].name)
    }

    override fun getItemCount(): Int {
        return if (contactList_ == null) {
            0
        } else contactList_!!.size
    }

    companion object {
        private var mOnClickListener: ListItemClickListener? = null
        private var contactList_: ArrayList<Contact>? = null
    }

    init {
        mOnClickListener = onClickListener
        contactList_ = contactList
    }
}

