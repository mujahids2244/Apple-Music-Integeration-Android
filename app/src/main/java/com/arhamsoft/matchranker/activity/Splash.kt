package com.arhamsoft.matchranker.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.arhamsoft.matchranker.databinding.ActivitySplashBinding
import com.arhamsoft.matchranker.util.CustomSharedPreference

class Splash : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    lateinit var sharedPreference: CustomSharedPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreference = CustomSharedPreference(this)



        Handler(Looper.getMainLooper()).postDelayed({
            if(sharedPreference.isLogin("LOGIN")){
                val intent = Intent(this, MainScreen::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this, Onboarding::class.java)
                startActivity(intent)
                finish()
            }

        }, 700)
    }
}