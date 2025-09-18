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
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 🔹 Inicializar FirebaseAuth y Facebook CallbackManager
        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        // 🔹 Referencias UI
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        btnFacebook = findViewById(R.id.btnFacebook)
        btnGitHub = findViewById(R.id.btnGitHub)

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
            startActivity(Intent(this, RegistrarEmpleadoActivity::class.java))
        }

        // 🔹 Login con Facebook
        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
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
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ✅ Login correcto → Ir al Dashboard
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Dashboard::class.java))
                    finish()
                } else {
                    // ❌ Error de login
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // 🔹 Verificar si ya hay usuario logueado
    override fun onStart() {
        super.onStart()
        val usuario = auth.currentUser
        if (usuario != null) {
            // Si ya había sesión, ir directo al Dashboard
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }
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
            addCustomParameter("scope", "user:email")
        }

        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                // Login exitoso
                Toast.makeText(this, "Bienvenido via GitHub", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // Error en el login
                Toast.makeText(this, "Error en login de GitHub: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}
