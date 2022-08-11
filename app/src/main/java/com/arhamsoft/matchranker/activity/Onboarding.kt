package com.arhamsoft.matchranker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.adapter.ViewPagerAdapter
import com.arhamsoft.matchranker.databinding.ActivityOnboardingBinding


class Onboarding : AppCompatActivity() {

    var titleList = mutableListOf<String>()
    var detailList = mutableListOf<String>()
    var imgList = mutableListOf<Int>()
    lateinit var binding: ActivityOnboardingBinding
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        postToList()

        val adapter =  ViewPagerAdapter(titleList,detailList,imgList)
        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val indicator = binding.indicators
        indicator.setViewPager(binding.viewPager)

        binding.btnSignup.setOnClickListener {


            if(binding.btnSignup.isClickable){

                binding.btnSignup.background = (ContextCompat.getDrawable(applicationContext,R.drawable.edit_text_color))
                binding.btnLogin.background = (ContextCompat.getDrawable(applicationContext,R.drawable.edit_text_shape))

            }
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)

        }

           binding.btnLogin.setOnClickListener {


               Log.e("","login=")
               if(binding.btnLogin.isClickable){

                   binding.btnLogin.background = (ContextCompat.getDrawable(applicationContext,R.drawable.edit_text_color))
                   binding.btnSignup.background = (ContextCompat.getDrawable(applicationContext,R.drawable.edit_text_shape))

               }
               val intent = Intent(this, LogIn::class.java)
               startActivity(intent)

        }
    }


    fun addtoList(title: String, desc:String, img : Int){
        titleList.add(title)
        detailList.add(desc)
        imgList.add(img)
    }

    fun postToList(){
        for (i in 1..3){
            addtoList("Enjoy the best music \n with us?","dmcwpkcm mcdwpkmcw kwcmpwdklvmwp vmwkpfvmwp dmc mdcwpkvmkpt btpnebnkep",
                R.drawable.plogo)
        }
    }
}