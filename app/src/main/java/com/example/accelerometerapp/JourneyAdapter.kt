package com.example.accelerometerapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myflicks.MainViewModel

class JourneyAdapter(private val dataArray: ArrayList<JourneyRow>, private val mainViewModel: MainViewModel): RecyclerView.Adapter<JourneyAdapter.MyViewHolderMovie>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MyViewHolderMovie{
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.one_row,parent,false)
        return MyViewHolderMovie(view)
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolderMovie, position: Int) {
        holder.dateTextView.text = dataArray[holder.adapterPosition].date
        holder.nameTextView.text = dataArray[holder.adapterPosition].name
        holder.additionalCommentsTextView.text = dataArray[holder.adapterPosition].additionalComments
    }

    inner class MyViewHolderMovie(view: View) : RecyclerView.ViewHolder(view){
        val dateTextView = view.findViewById(R.id.textViewDate) as TextView
        val nameTextView = view.findViewById(R.id.textViewName) as TextView
        val additionalCommentsTextView = view.findViewById(R.id.textViewAdditionalComments) as TextView
    }


}