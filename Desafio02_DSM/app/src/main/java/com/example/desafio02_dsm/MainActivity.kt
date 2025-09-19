package com.example.desafio02_dsm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.OAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistrarse: Button
    private lateinit var btnFacebook: Button
    private lateinit var btnGitHub: Button
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // inicializa Facebook SDK
            FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
            FacebookSdk.sdkInitialize(applicationContext)

            // inicializa FirebaseAuth
            auth = FirebaseAuth.getInstance()

            // cierra cualquier sesión existente al iniciar la app
            auth.signOut()
            LoginManager.getInstance().logOut()

            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            callbackManager = CallbackManager.Factory.create()

            // referencias UI
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            btnRegistrarse = findViewById(R.id.btnRegistrarse)
            btnFacebook = findViewById(R.id.btnFacebook)
            btnGitHub = findViewById(R.id.btnGitHub)
        } catch (e: Exception) {
            // si hay algún error en la inicialización, reiniciar la actividad
            Toast.makeText(this, "Error al iniciar la aplicación", Toast.LENGTH_SHORT).show()
            recreate()
            return
        }

        // --- bloque de login con correo y contraseña ---
        // acción de login con email/password
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa los campos", Toast.LENGTH_SHORT).show()
            } else {
                loginEmpleado(email, password)
            }
        }

        // --- bloque de registro ---
        // ir a registrar nuevo empleado
        btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistrarEmpleadoActivity::class.java))
        }

        // --- bloque de login con Facebook ---
        // login con Facebook
        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logOut()

            // solicitar permisos necesarios
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }

        // configurar callback de Facebook
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@MainActivity,
                        "Login cancelado",
                        Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@MainActivity,
                        "Error en login: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })

        // --- bloque de login con GitHub ---
        // login con GitHub
        btnGitHub.setOnClickListener {
            signInWithGitHub()
        }

        // --- bloque de configuración de la vista ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- funciones de login (fuera del onCreate) ---

    // función para login con correo y contraseña
    private fun loginEmpleado(email: String, password: String) {
        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        // nos lleva a la página una vez logeados
                        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Dashboard::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        auth.signOut()
                        Toast.makeText(this, "Error al abrir Dashboard: ${e.message}", Toast.LENGTH_LONG).show()
                        btnLogin.isEnabled = true
                    }
                } else {
                    // error de login
                    auth.signOut()
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    btnLogin.isEnabled = true
                }
            }
    }

    // Función para manejar el token de Facebook
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenido via Facebook", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Dashboard::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error de autenticación: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Funciones para login con GitHub
    private fun signInWithGitHub() {
        val provider = OAuthProvider.newBuilder("github.com")
        auth.pendingAuthResult?.addOnSuccessListener { authResult ->
            // si existe una sesión pendiente, manejarla
            Toast.makeText(this, "Bienvenido via GitHub", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }?.addOnFailureListener { e ->
            // no hay sesión pendiente, iniciar nuevo flujo
            startGitHubLogin(provider)
        } ?: startGitHubLogin(provider)
    }

    private fun startGitHubLogin(provider: OAuthProvider.Builder) {
        try {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { authResult ->
                    // login exitoso
                    val user = authResult.user
                    if (user != null) {
                        Toast.makeText(this, "Bienvenido ${user.displayName}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Dashboard::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    // error en el login
                    when {
                        e.message?.contains("cancelled") == true -> {
                            Toast.makeText(this, "Login cancelado", Toast.LENGTH_SHORT).show()
                        }
                        e.message?.contains("network") == true -> {
                            Toast.makeText(this, "Error de red. Verifica tu conexión", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "Error en login de GitHub: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar login: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- Funciones del ciclo de vida de la actividad (fuera del onCreate) ---

    // verificar si ya hay usuario logueado
    override fun onStart() {
        super.onStart()
        auth.signOut()
    }

    // procesar resultado del login de Facebook
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}