package com.malveslin.pestdetection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.io.Serializable



val RC_SIGN_IN: Int = 1
lateinit var mGoogleSignInClient: GoogleSignInClient
lateinit var mGoogleSignInOptions: GoogleSignInOptions
private lateinit var firebaseAuth: FirebaseAuth

data class Usuario(val nome: String, val user: String, val senha: String) : Serializable {
    override fun toString(): String {
        return this.nome
    }
}

class LoginActivity : AppCompatActivity() {

    val usuario = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usuario.add(Usuario("Marcelo", "malves", "123"))
        usuario.add(Usuario("Jesus", "jesus", "124"))

        configureGoogleSignIn()

        setupUI()

        firebaseAuth = FirebaseAuth.getInstance()

        login_btn.setOnClickListener {
            onLogin()
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null ){
                    firebaseAuthWithGoogle(account)
                    Toast.makeText(this, "Conta: ${account.displayName}", Toast.LENGTH_LONG).show()
//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.putExtra("user", account)
//                    startActivity(intent)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Erro: $e", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun onLogin() {

        usuario.forEachIndexed { index, user ->
            var intent: Intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuario", user)
            startActivity(intent)

        }

    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private fun setupUI() {
        google_btn.setOnClickListener{
            signIn()
        }
    }


    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
//        Toast.makeText(this, ""+ credential.toString(), Toast.LENGTH_LONG).show()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                Toast.makeText(this, it.result.toString(), Toast.LENGTH_LONG).show()

                startActivity(MainActivity.getLaunchIntent(this))
            } else {
                //Toast.makeText(this, "Google sign in failed:(" + it.result.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }





}
