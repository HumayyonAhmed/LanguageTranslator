package com.languagetranslator.translate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        configureGoogleSignIn()

        val prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val username = prefs.getString("username", null)
        if (email != null && username != null) {
            // User is already logged in, start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // User is not logged in, show the login page
            setContentView(R.layout.activity_login)
            // Set up the sign-in button and configure GoogleSignInClient as before
        }


        val signInButton = findViewById<SignInButton>(R.id.login)
        signInButton.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            signInIntent.putExtra("overrideAccount", null as Parcelable?)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email
                val username = account?.givenName
                Log.i("Email", email.toString());
                Log.i("Password", username.toString());
                val prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                with(prefs.edit()) {
                    putString("email", email)
                    putString("username", username)
                    apply()
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: ApiException) {
                // Handle sign-in failure
            }
        }
    }
}
