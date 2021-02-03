package com.mrenann.desafio_dhgames.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mrenann.desafio_dhgames.databinding.ActivityRegisterBinding
import com.mrenann.desafio_dhgames.utils.Constants.ConstantsDB.FIREBASE_COLLECTION_USERS


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private val auth by lazy{
        Firebase.auth
    }

    private val db by lazy {
        Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signUp()
        binding.ibRegisterBack.setOnClickListener {
            finish()
        }

    }

    private fun signUp() {

        binding.btCardRegister.setOnClickListener {
            val userName = binding.tieNome.text.toString()
            val userEmail = binding.tieEmail.text.toString()
            val userPass = binding.tieSenha.text.toString()
            val userConfirmPass = binding.tieConfSenha.text.toString()

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnSuccessListener {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(userName).build()
                        it.user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { task->
                                    it.user?.let{ user->
                                        registerDB(user)
                                    }

                                }
                    }.addOnFailureListener {
                        Toast.makeText(this@RegisterActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
        }
    }

    private fun registerDB(user: FirebaseUser){
        val userData = hashMapOf(
            "uid" to (user.uid),
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: "")
        )

        db.collection(FIREBASE_COLLECTION_USERS)
            .document(user.uid ?: "")
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuário Cadastrado com Sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
    }
}