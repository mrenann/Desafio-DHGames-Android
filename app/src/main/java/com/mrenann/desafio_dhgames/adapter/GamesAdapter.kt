package com.mrenann.desafio_dhgames.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mrenann.desafio_dhgames.R
import com.mrenann.desafio_dhgames.databinding.GameCardBinding
import java.util.HashMap


class GamesAdapter(
        var list: MutableList<DocumentSnapshot> = mutableListOf(),
        private val onClicked: (DocumentSnapshot?) -> Unit
): RecyclerView.Adapter<GamesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = GameCardBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], onClicked)
    }

    fun updateList(newList: MutableList<DocumentSnapshot>){
        list=newList
        notifyDataSetChanged()
    }

    class ViewHolder(
            private val binding: GameCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(game: DocumentSnapshot, onClick: (DocumentSnapshot) -> Unit) = with(binding) {

            val firebaseAuth by lazy { Firebase.auth }
            val db by lazy { Firebase.firestore }

            val user = firebaseAuth.currentUser
            var idFriend:String = ""

            binding.tvtitleGame.text = game["title"].toString()+"-"+game["released"].toString()
                Glide.with(itemView.context)
                        .load(game["photo"])
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_logo)
                        .into(binding.rIvPoster)

            itemView.setOnClickListener {
                onClick(game)
            }
        }
    }
}
