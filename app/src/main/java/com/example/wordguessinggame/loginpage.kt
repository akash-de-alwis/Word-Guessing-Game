package com.example.wordguessinggame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class loginpage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loginpage)

        val btnNavigate: Button = findViewById(R.id.button2)
        btnNavigate.setOnClickListener {
            val intent = Intent(this, logdetails::class.java)
            startActivity(intent)
        }

        val btnNavigatek: Button = findViewById(R.id.button3)
        btnNavigatek.setOnClickListener {
            val intent = Intent(this, logdetails::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}