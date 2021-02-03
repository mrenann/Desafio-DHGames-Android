package com.mrenann.desafio_dhgames.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mrenann.desafio_dhgames.R
import com.mrenann.desafio_dhgames.databinding.ActivityLoginBinding
import com.mrenann.desafio_dhgames.databinding.ActivitySplashBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val auth by lazy{
        Firebase.auth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btLoginEntrar.setOnClickListener {
            val userEmail = binding.tietEmail.text.toString()
            val userPass = binding.tietSenha.text.toString()

            auth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@LoginActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                }

        }

        binding.btLoginRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onResume() {
        super.onResume()

        auth.currentUser?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}