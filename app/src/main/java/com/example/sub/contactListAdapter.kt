package com.example.sub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class contactListAdapter(userList: MutableList<User>, onClickListener: profileFragment) :
    RecyclerView.Adapter<contactListAdapter.ViewHolder>() {
    interface ListItemClickListener {
        fun onListItemClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        val contactFirstName: TextView
        val contactLastName: TextView
        val contactNumber: TextView

        override fun onClick(v: View) {
            val position = layoutPosition
            mOnClickListener!!.onListItemClick(position)
        }

        init {
            contactFirstName = view.findViewById<View>(R.id.contactListFirstName) as TextView
            contactLastName = view.findViewById<View>(R.id.contactListLastName) as TextView
            contactNumber = view.findViewById<View>(R.id.contactListPhoneNumber) as TextView
            view.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_contact_list2, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        viewHolder.contactFirstName.setText(userList_!![position].firstName)
        viewHolder.contactLastName.setText(userList_!![position].lastName)
        viewHolder.contactNumber
              .setText(userList_!![position].number)
    }

    override fun getItemCount(): Int {
        return if (userList_ == null) {
            0
        } else userList_!!.size
    }

    fun changeDataSet(strings: List<User>?) {
        userList_ = strings as MutableList<User>?
    }

    companion object {
        private var mOnClickListener: ListItemClickListener? = null
        private var userList_: MutableList<User>? = null
    }

    init {
        mOnClickListener = onClickListener
        userList_ = userList
    }
}

