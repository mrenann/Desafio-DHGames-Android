package com.mrenann.desafio_dhgames.view.activity

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mrenann.desafio_dhgames.adapter.GamesAdapter
import com.mrenann.desafio_dhgames.databinding.ActivityMainBinding
import com.mrenann.desafio_dhgames.utils.Constants
import com.mrenann.desafio_dhgames.utils.Constants.ConstantsDB.FIREBASE_COLLECTION_GAMES
import com.mrenann.desafio_dhgames.utils.Constants.ConstantsDB.FIREBASE_COLLECTION_USERS
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val firebaseAuth by lazy { Firebase.auth }
    val db by lazy { Firebase.firestore }
    var list:MutableList<DocumentSnapshot> = mutableListOf()

    private val gamesAdapter : GamesAdapter by lazy {
        GamesAdapter { game->
            val clicked = game
            clicked?.let{
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra(Constants.ConstantsGAME.TO_GAME_INTENT, game.id)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myUserId = firebaseAuth.currentUser?.uid
        myUserId?.let { userId-> getGames(userId) }
        myUserId?.let {userId-> setupRecyclerView(userId) }

        binding.fab.setOnClickListener {view->
            val intent = Intent(this, AddEditGamesActivity::class.java)
            //intent.putExtra(Constants.ConstantsGAME.HAS_GAME_TO_EDIT_INTENT, )
            startActivity(intent)
        }
        SetupSearchView(application)
    }


    private fun SetupSearchView(application: Application) {

        val searchView: SearchView? = binding?.searchView
        val myUserId = firebaseAuth.currentUser?.uid

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query:String):Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterList(newText)
                }
                return true
            }
        })
    }

    private fun filterList(query: String) {
        val tempList:MutableList<DocumentSnapshot> = mutableListOf()

        for(d in list){
            if (query.toLowerCase(Locale.ROOT) in d["title"].toString().toLowerCase(Locale.ROOT)) {
                tempList.add(d)
            }
        }
        gamesAdapter.updateList(tempList)
    }

    private fun getGames(uid: String){
        val documentReference = db.collection(FIREBASE_COLLECTION_USERS)
            .document(uid)
            .collection(FIREBASE_COLLECTION_GAMES)

        documentReference.get()
            .addOnSuccessListener {
                list = it.documents
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView(uid: String){
        binding?.gamesRecycler?.apply {
            layoutManager = GridLayoutManager(
                this@MainActivity,
               2
            )
            val documentReference = db.collection(FIREBASE_COLLECTION_USERS)
                .document(uid)
                .collection(FIREBASE_COLLECTION_GAMES)

            documentReference.addSnapshotListener { snapshot: QuerySnapshot?, e: FirebaseFirestoreException? ->
                if (e != null) {
                    Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_SHORT).show()
                }
                if (snapshot != null ) {
                    val games:MutableList<DocumentSnapshot> = mutableListOf()
                    snapshot.documents.forEach {docSnapshot->
                        games.add(docSnapshot)
                    }
                    gamesAdapter.list.clear()
                    gamesAdapter.list = games
                    adapter = gamesAdapter
                }

                }

        }
    }

}

