package com.example.sub

import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class contactListAdapter(contactList: List<Contact>, onClickListener: ListItemClickListener) :
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
        viewHolder: contactListAdapter.ViewHolder,
        position: Int
    ) {
       // ((viewHolder..setText(localCommentList!![position].getText())
       // viewHolder.getPostCreator()
            //  .setText("Commenter: " + localCommentList!![position].getUser_name())
    }

    override fun getItemCount(): Int {
        return if (contactList == null) {
            0
        } else contactList!!.size
    }

    companion object {
        private var mOnClickListener: ListItemClickListener? = null
        private var contactList: List<Contact>? = null
    }

    init {
        mOnClickListener = onClickListener
        contactList =
    }
}

