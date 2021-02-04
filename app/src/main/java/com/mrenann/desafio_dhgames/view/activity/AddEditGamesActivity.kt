package com.mrenann.desafio_dhgames.view.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mrenann.desafio_dhgames.R
import com.mrenann.desafio_dhgames.databinding.ActivityAddEditGamesBinding
import com.mrenann.desafio_dhgames.databinding.ActivityGameBinding
import com.mrenann.desafio_dhgames.utils.Constants
import com.mrenann.desafio_dhgames.utils.Constants.ConstantsDB.FIREBASE_COLLECTION_GAMES
import com.mrenann.desafio_dhgames.utils.Constants.ConstantsDB.FIREBASE_COLLECTION_USERS

class AddEditGamesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditGamesBinding
    private var gameId: String? = null
    val firebaseAuth by lazy { Firebase.auth }
    val db by lazy { Firebase.firestore }
    private val storageRef by lazy { Firebase.storage.reference }
    private val firebaseAnalytics by lazy { Firebase.analytics }
    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        gameId = intent.getStringExtra(Constants.ConstantsGAME.HAS_GAME_TO_EDIT_INTENT)

        binding.ibRegisterBack.setOnClickListener {
            finish()
        }

        gameId?.let {
            val myUserId = firebaseAuth.currentUser?.uid
            val documentReference = myUserId?.let {userId->
                gameId?.let { gameId->
                    db.collection(Constants.ConstantsDB.FIREBASE_COLLECTION_USERS)
                        .document(userId)
                        .collection(Constants.ConstantsDB.FIREBASE_COLLECTION_GAMES)
                        .document(gameId)
                }
            }
            documentReference?.get()
                ?.addOnSuccessListener { game->
                binding.tietTitulo.setText(game["title"].toString())
                binding.tietLancado.setText(game["released"].toString())
                binding.tietDesc.setText(game["description"].toString())

                Glide.with(this)
                    .load(game["photo"])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_logo)
                    .into(binding.poster)

                binding.btCardRegister.setOnClickListener {
                    updateGame(game.id)
                }

                }?.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }

        }?: run {

            binding.btCardRegister.setOnClickListener {
                saveGame()
            }

        }

        binding.poster.setOnClickListener {
            choosePicture()
        }

    }

    private fun choosePicture(){
        val intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = (Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent,1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1 && resultCode== RESULT_OK && data!=null && data.data!=null){
            data.data?.let{
                imageUri = it
                binding.poster.setImageURI(imageUri)
            }

        }
    }

    private fun uploadPicture(imgId:String) {

        imageUri?.let { imageUri ->

        val savedUri = imageUri
        val profilePhoto = storageRef.child(
            "${(firebaseAuth.currentUser?.uid ?: "")}/${imgId}"
        )

        profilePhoto.putFile(savedUri)
            .addOnSuccessListener {
                val downloadUri = it.storage.downloadUrl

                downloadUri.addOnSuccessListener { thisUri ->
                    val Photodata = hashMapOf<String, Any>(
                        "photo" to thisUri.toString()
                    )
                    db.collection(FIREBASE_COLLECTION_USERS)
                        .document(firebaseAuth.currentUser?.uid ?: "")
                        .collection(FIREBASE_COLLECTION_GAMES)
                        .document(imgId)
                        .update(Photodata)
                }

            }.addOnFailureListener {
                Toast.makeText(
                    this@AddEditGamesActivity,
                    it.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }

    } ?: run {}

    }


    private fun saveGame(){
        val gameData = hashMapOf(
            "title" to (binding.tietTitulo.text.toString() ?: ""),
            "description" to (binding.tietDesc.text.toString() ?: ""),
            "released" to (binding.tietLancado.text.toString() ?: ""),
            "photo" to ("")
        )

        db.collection(FIREBASE_COLLECTION_USERS)
            .document(firebaseAuth.currentUser?.uid ?: "")
            .collection(FIREBASE_COLLECTION_GAMES)
            .add(gameData)
            .addOnSuccessListener {
                uploadPicture(it.id)
                firebaseAnalytics.logEvent("save_game", null)
                Toast.makeText(this, "Dados Salvos com Sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateGame(gameId:String){
        val gamedata = hashMapOf<String, Any>(
            "title" to (binding.tietTitulo.text.toString() ?: ""),
            "description" to (binding.tietDesc.text.toString() ?: ""),
            "released" to (binding.tietLancado.text.toString() ?: "")
        )
        db.collection(FIREBASE_COLLECTION_USERS)
            .document(firebaseAuth.currentUser?.uid ?: "")
            .collection(FIREBASE_COLLECTION_GAMES)
            .document(gameId)
            .update(gamedata)

            .addOnSuccessListener {
                uploadPicture(gameId)
                firebaseAnalytics.logEvent("edit_game", null)
                Toast.makeText(this, "Dados Alterados com Sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}