package ie.setu.eventJournal.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import ie.setu.eventJournal.R
import ie.setu.eventJournal.databinding.LoginBinding
import ie.setu.eventJournal.ui.home.Home
import timber.log.Timber
import androidx.lifecycle.Observer
import com.google.android.gms.common.SignInButton

class Login : AppCompatActivity() {

    // Google authentication reference: https://www.youtube.com/watch?v=-tCIsI7aZGk
    // Note: I added google auth before lab steps came out which is why I have continued with this alternative approach

    private lateinit var loginRegisterViewModel: LoginRegisterViewModel
    private lateinit var loginBinding: LoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 120
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        loginBinding = LoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        firebaseAuth = FirebaseAuth.getInstance()

        loginBinding.emailSignInButton.setOnClickListener {
            signIn(
                loginBinding.fieldEmail.text.toString(),
                loginBinding.fieldPassword.text.toString()
            )
        }

        loginBinding.emailCreateAccountButton.setOnClickListener {
            createAccount(
                loginBinding.fieldEmail.text.toString(),
                loginBinding.fieldPassword.text.toString()
            )
        }

        loginBinding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        loginBinding.googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        loginBinding.googleSignInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        loginBinding.googleSignInButton.setBackgroundColor(Color.WHITE);

    }

    //Required to exit app from Login Screen - must investigate this further
    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(this, "Click again to Close App...", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Email and pass: Check if user is signed in (non-null) and update UI accordingly.
        loginRegisterViewModel = ViewModelProvider(this).get(LoginRegisterViewModel::class.java)
        loginRegisterViewModel.liveFirebaseUser.observe(this, Observer
        { firebaseUser ->
            if (firebaseUser != null)
                startActivity(Intent(this, Home::class.java))
        })

        loginRegisterViewModel.firebaseAuthManager.errorStatus.observe(this, Observer
        { status -> checkStatus(status) })

        // Google auth: Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
    }

    private fun createAccount(email: String, password: String) {
        Timber.d("createAccount:$email")
        if (!validateForm()) return

        loginRegisterViewModel.register(email, password)
    }

    private fun signIn(email: String, password: String) {
        Timber.d("signIn:$email")
        if (!validateForm()) return

        loginRegisterViewModel.login(email, password)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun checkStatus(error: Boolean) {
        if (error)
            Toast.makeText(
                this,
                getString(R.string.auth_failed),
                Toast.LENGTH_LONG
            ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Timber.w("Google sign in failed: ${e.statusCode}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Timber.d("firebaseAuthWithGoogle:${acct.id}")
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    startActivity(Intent(this, Home::class.java))
                    finish()
                } else {
                    Timber.w("signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = loginBinding.fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            loginBinding.fieldEmail.error = "Required."
            valid = false
        } else {
            loginBinding.fieldEmail.error = null
        }

        val password = loginBinding.fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            loginBinding.fieldPassword.error = "Required."
            valid = false
        } else {
            loginBinding.fieldPassword.error = null
        }
        return valid
    }
}
