package com.mrenann.desafio_dhgames.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mrenann.desafio_dhgames.R
import com.mrenann.desafio_dhgames.databinding.ActivityLoginBinding
import com.mrenann.desafio_dhgames.databinding.ActivitySplashBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private var validationEmail = false
    private var validationPassword = false

    private val auth by lazy{
        Firebase.auth
    }
    private val firebaseAnalytics by lazy { Firebase.analytics }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textChangeDefault(binding.tietEmail, binding.tilEmail, R.string.string_email)
        textChangeDefault(binding.tietSenha, binding.tilSenha, R.string.string_password)

        binding.btLoginEntrar.setOnClickListener {
            if(validationEmail == true && validationPassword == true){
                val userEmail = binding.tietEmail.text.toString()
                val userPass = binding.tietSenha.text.toString()

                auth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnSuccessListener {
                        firebaseAnalytics.logEvent("login", null)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@LoginActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }else{
                Toast.makeText(this@LoginActivity, "Preencha todos os campos corretamente", Toast.LENGTH_LONG).show()
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

    private fun textChangeDefault(editText: EditText, textInputLayout: TextInputLayout, errorString: Int) {
        editText.doOnTextChanged { text, start, _, count ->
            if (text?.isBlank() == true) {
                textInputLayout.error = "Campo não pode ser vazio"
            } else {
                textInputLayout.isErrorEnabled = false
            }
            if (editText.tag == getString(R.string.string_email)) {
                validatingEmail(text.toString())
            }
            if (editText.tag == getString(R.string.string_password)) {
                passwordLength(start + 1)
            }

        }
    }

    private fun validatingEmail(email: String) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            validationEmail = true
            binding.tilEmail.isErrorEnabled = false
        } else {
            validationEmail = false
            binding.tilEmail.error = getString(R.string.validationEmail)
        }
    }
    private fun passwordLength(count: Int) {
        if (count >= 6) {
            validationPassword = true
            binding.tilSenha.isErrorEnabled = false
        } else {
            validationPassword = false
            binding.tilSenha.error = getString(R.string.validationPassword)

        }
    }
}