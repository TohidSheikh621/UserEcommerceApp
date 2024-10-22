package com.example.userkabirstore.activity

import android.content.Intent
import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity

import com.example.userkabirstore.R
import com.example.userkabirstore.databinding.ActivityOrderPlacedBinding

class OrderPlacedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderPlacedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderPlacedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }

        binding.successLottie.playAnimation()

        binding.doneBtn.setOnClickListener {
            var intent  = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent )
            finish()
        }


    }
}