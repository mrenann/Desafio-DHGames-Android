package com.mrenann.desafio_dhgames.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mrenann.desafio_dhgames.R
import com.mrenann.desafio_dhgames.databinding.ActivityGameBinding
import com.mrenann.desafio_dhgames.databinding.ActivitySplashBinding
import com.mrenann.desafio_dhgames.utils.Constants
import com.mrenann.desafio_dhgames.utils.Constants.ConstantsGAME.TO_GAME_INTENT

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding

    private var gameId: String? = null
    val firebaseAuth by lazy { Firebase.auth }
    val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ibRegisterBack.setOnClickListener {
            finish()
        }

    }


    override fun onResume() {
        super.onResume()

        val myUserId = firebaseAuth.currentUser?.uid
        gameId = intent.getStringExtra(TO_GAME_INTENT)

        val documentReference = myUserId?.let {userId->
            gameId?.let { gameId->
                db.collection(Constants.ConstantsDB.FIREBASE_COLLECTION_USERS)
                    .document(userId)
                    .collection(Constants.ConstantsDB.FIREBASE_COLLECTION_GAMES)
                    .document(gameId)
            }
        }

        documentReference?.get()
            ?.addOnSuccessListener {
                Glide.with(this)
                    .load(it["photo"])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_logo)
                    .into(binding.poster)

                Glide.with(this)
                    .load(it["photo"])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_logo)
                    .into(binding.ivBackDropGames)

                binding.tvTitulo.text = it["title"].toString()
                binding.tvLancamento.text = "Lançamento: " + it["released"].toString()
                binding.tvDescricao.text = it["description"].toString()

                binding.fab.setOnClickListener {view->
                    val intent = Intent(this, AddEditGamesActivity::class.java)
                    intent.putExtra(Constants.ConstantsGAME.HAS_GAME_TO_EDIT_INTENT, it.id)
                    startActivity(intent)
                }

            }?.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }

    }

}