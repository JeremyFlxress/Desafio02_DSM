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
            // 🔹 Inicializar Facebook SDK
            FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
            FacebookSdk.sdkInitialize(applicationContext)
            
            // 🔹 Inicializar FirebaseAuth y Facebook CallbackManager
            auth = FirebaseAuth.getInstance()
            
            // Cerrar cualquier sesión existente al iniciar la app
            auth.signOut()
            LoginManager.getInstance().logOut()
            
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)
            
            callbackManager = CallbackManager.Factory.create()

            // 🔹 Referencias UI
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            btnRegistrarse = findViewById(R.id.btnRegistrarse)
            btnFacebook = findViewById(R.id.btnFacebook)
            btnGitHub = findViewById(R.id.btnGitHub)
        } catch (e: Exception) {
            // Si hay algún error en la inicialización, reiniciar la actividad
            Toast.makeText(this, "Error al iniciar la aplicación", Toast.LENGTH_SHORT).show()
            recreate()
            return
        }

        // 🔹 Acción de login con email/password
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa los campos", Toast.LENGTH_SHORT).show()
            } else {
                loginEmpleado(email, password)
            }
        }

        // 🔹 Ir a registrar nuevo empleado
        btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistrarEmpleado::class.java))
        }

        // 🔹 Login con Facebook
        btnFacebook.setOnClickListener {
            // Asegurarse de cerrar cualquier sesión previa
            LoginManager.getInstance().logOut()
            
            // Solicitar permisos necesarios
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }

        // Configurar callback de Facebook
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

        // 🔹 Login con GitHub
        btnGitHub.setOnClickListener {
            signInWithGitHub()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginEmpleado(email: String, password: String) {
        // Desactivar el botón de login para evitar múltiples clicks
        btnLogin.isEnabled = false
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        // ✅ Login correcto → Ir al Dashboard
                        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Dashboard::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        // Error al abrir Dashboard
                        auth.signOut() // Asegurarse de cerrar la sesión si hay error
                        Toast.makeText(this, "Error al abrir Dashboard: ${e.message}", Toast.LENGTH_LONG).show()
                        btnLogin.isEnabled = true
                    }
                } else {
                    // ❌ Error de login
                    auth.signOut() // Asegurarse de cerrar la sesión si hay error
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    btnLogin.isEnabled = true
                }
            }
    }

    // 🔹 Verificar si ya hay usuario logueado
    override fun onStart() {
        super.onStart()
        // Al iniciar, asegurarnos de que estamos en la pantalla de login
        auth.signOut()
    }

    // 🔹 Procesar resultado del login de Facebook
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // 🔹 Manejar token de acceso de Facebook
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso
                    Toast.makeText(this, "Bienvenido via Facebook", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Dashboard::class.java))
                    finish()
                } else {
                    // Error en el login
                    Toast.makeText(this, "Error de autenticación: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 🔹 Login con GitHub
    private fun signInWithGitHub() {
        val provider = OAuthProvider.newBuilder("github.com").apply {
            addCustomParameter("scope", "user:email read:user")
            addCustomParameter("client_id", getString(R.string.github_client_id))
        }

        // Primero, verificar si hay una sesión pendiente
        auth.pendingAuthResult?.addOnSuccessListener { authResult ->
            // Existe una sesión pendiente, manejarla
            Toast.makeText(this, "Bienvenido via GitHub", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }?.addOnFailureListener { e ->
            // No hay sesión pendiente, iniciar nuevo flujo
            startGitHubLogin(provider)
        } ?: startGitHubLogin(provider) // Si no hay pending result, iniciar nuevo flujo
    }

    private fun startGitHubLogin(provider: OAuthProvider.Builder) {
        try {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { authResult ->
                    // Login exitoso
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
                    // Error en el login
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
}
