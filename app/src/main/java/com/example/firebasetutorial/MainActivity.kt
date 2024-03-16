package com.example.firebasetutorial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.firebasetutorial.AuthActivity.Companion.EMAIL_KEY
import com.example.firebasetutorial.AuthActivity.Companion.PROVIDER_KEY
import com.example.firebasetutorial.databinding.ActivityMainBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType {
    BASIC,
    GOOGLE,
    FACEBOOK
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()


    }

    private fun initUI() {
        initIntent()
    }

    //funcion que inicia el intent con la importacion de datos
    private fun initIntent() {
        //Se importan los putExtras
        val bundle = intent.extras
        val email = bundle?.getString(EMAIL_KEY)
        val provider = bundle?.getString(PROVIDER_KEY)
        setUp(email ?: "", provider ?: "")

        //save Data
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString(EMAIL_KEY, email)
        prefs.putString(PROVIDER_KEY, provider)
        prefs.apply()

    }

    private fun setUp(email: String, provider: String) {
        binding.tvEmail.text = email
        binding.tvProvider.text = provider

        binding.btnLogOut.setOnClickListener {
            //Borrado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            //Cierre de sesion en Facebook
            if (provider == ProviderType.FACEBOOK.name){
                LoginManager.getInstance().logOut()
            }

            //cierre en firebase
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }
}