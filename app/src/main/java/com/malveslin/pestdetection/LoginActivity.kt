package com.malveslin.pestdetection

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.io.Serializable


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

        login_btn.setOnClickListener {
            onLogin()
        }
    }


    private fun onLogin() {

        usuario.forEachIndexed { index, user ->
            var intent: Intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuario", user)
            startActivity(intent)

        }

    }
}
