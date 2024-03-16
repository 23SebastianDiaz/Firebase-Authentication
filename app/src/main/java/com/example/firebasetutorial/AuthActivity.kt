package com.example.firebasetutorial

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.appcompat.app.AlertDialog
import com.example.firebasetutorial.databinding.ActivityAuthBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    companion object {
        const val EMAIL_KEY = "email"
        const val PROVIDER_KEY = "provider"

    }

    private val GOOGLE_SIGN_IN = 100
    private val callbackManager = CallbackManager.Factory.create()

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen() //inicializa la instalacion del SplashScreen
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        screenSplash.setKeepOnScreenCondition { false }
        initUi()
    }

    private fun initUi() {

        Analitycs()
        initListeners()
        session()

    }

    //Integrar Analytics
    private fun Analitycs() {
        val analytics =
            FirebaseAnalytics.getInstance(this) //Instancia el Analytics de FireBase para integrarla
        val bundle = Bundle()
        bundle.putString("message", "Integracion de Firebase completa")
        analytics.logEvent("InitScreen", bundle)
    }

    //funcion para establecer la autenticacion por medio de lo button
    private fun initListeners() {
        //SIGNUP
        binding.btnSignUp.setOnClickListener {
            //condicion para que no pueda introducir textos vacios
            if (binding.etEmail.text.isNotEmpty() && binding.etPassword.text.isNotEmpty()) {
                //se crea el email y la contraseña
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.etEmail.text.toString().lowercase(),
                    binding.etPassword.text.toString()
                ).addOnCompleteListener { signUp ->
                    //se notifique si la notificacion de registro a sido satisfactorio
                    if (signUp.isSuccessful) {
                        //envia lo parametros registrados al activity main, como email y un provider type
                        showMain(signUp.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlert()
                    }
                }
            }
        }
        //LOGIN
        binding.btnLogin.setOnClickListener {
            //condicion para que no pueda introducir textos vacios
            if (binding.etEmail.text.isNotEmpty() && binding.etPassword.text.isNotEmpty()) {
                //se crea el email y la contraseña
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                ).addOnCompleteListener { signUp ->
                    //se notifique si la notificacion de registro a sido satisfactorio
                    if (signUp.isSuccessful) {
                        //envia lo parametros registrados al activity main, como email y un provider type
                        showMain(signUp.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlert()
                    }
                }
            }
        }
        //GOOGLE
        binding.btnGoogle.setOnClickListener {
            //Configuracion Autenticacion del cliente con Google
            //Login por defecto
            val googleConf =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            //mostrar cliente de autenticacion con google
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut() //se realice logOut de la cuenta asociada en el momento
            //mostrar pantalla de autenticacion
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        binding.btnFacebook.setOnClickListener {
            //Llamar a vista de sesion de Facebook
            //Que permisos queremos leer del usuario
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
            //funcion que se llama a modo de callback cuando finalice la auth con facebook
            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    //controlar los posibles sucesos
                    override fun onCancel() {
                        TODO("Not yet implemented")
                    }

                    override fun onError(error: FacebookException) {
                        showAlert()
                    }

                    override fun onSuccess(result: LoginResult) {
                        // en caso de que no sea nulo
                        result?.let {
                            val token = it.accessToken
                            val credential = FacebookAuthProvider.getCredential(token.token)
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        showMain(
                                            it.result?.user?.email ?: "",
                                            ProviderType.FACEBOOK
                                        )
                                    } else {
                                        showAlert()
                                    }
                                }
                        }
                    }
                })
        }
    }

    //Funcion para iniciar activity main
    private fun showMain(email: String, provider: ProviderType) {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            putExtra(EMAIL_KEY, email)
            putExtra(PROVIDER_KEY, provider.name)
        }
        startActivity(mainIntent)
    }

    //Recibe los datos de la activity de autenticacion de google y facebook
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(
            requestCode,
            resultCode,
            data
        )// LLamada a una de las posibles sucesos de Facebook
        super.onActivityResult(requestCode, resultCode, data)

        //Verifica si la respuesta de la activity corrende con la autenticacion de google
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) //envia la data
            try {
                //recuperar la cuenta autenticada
                val account = task.getResult(ApiException::class.java)
                //Autentica la cuenta de google con Firebase
                if (account != null) {
                    //recupera credencial
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            //se notifique si la notificacion de registro a sido satisfactorio
                            if (it.isSuccessful) {
                                //envia lo parametros registrados al activity main, como email y un provider type
                                showMain(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert()
                            }
                        }
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }
    }

    //Recuperar si tiene un email y password guardado
    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString(EMAIL_KEY, null) //obtiene el email por medio de la key
        val provider = prefs.getString(PROVIDER_KEY, null) //obtiene el provider por medio de la key

        if (email != null && provider != null) {
            showMain(email, ProviderType.valueOf(provider))
        }

    }

    //Funcion para alerta en caso de error
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha produciodo un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }
}