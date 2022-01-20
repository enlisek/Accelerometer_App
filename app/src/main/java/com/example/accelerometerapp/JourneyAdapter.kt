package com.example.accelerometerapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class JourneyAdapter(private val dataArray: ArrayList<JourneyRow>): RecyclerView.Adapter<JourneyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MyViewHolder{
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.one_row, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cl: Calendar = Calendar.getInstance()
        cl.timeInMillis = dataArray[holder.adapterPosition].startRoute.toLong()

        holder.dateTextView.text = "" + cl.get(Calendar.DAY_OF_MONTH).toString() + "." + (cl.get(Calendar.MONTH)+1).toString() + "." + cl.get(Calendar.YEAR)
        holder.nameTextView.text = dataArray[holder.adapterPosition].name
        holder.additionalCommentsTextView.text = dataArray[holder.adapterPosition].description
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val dateTextView = view.findViewById(R.id.textViewDate) as TextView
        val nameTextView = view.findViewById(R.id.textViewName) as TextView
        val additionalCommentsTextView = view.findViewById(R.id.textViewAdditionalComments) as TextView
    }


}