package com.arhamsoft.matchranker.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.arhamsoft.matchranker.databinding.ActivityForgotPasswordBinding
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.usermodels.ForgotModel
import com.arhamsoft.matchranker.usermodels.LoginToAppleModel
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject

class ForgotPassword : AppCompatActivity() {
    lateinit var binding: ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.backtomain.setOnClickListener {

            onBackPressed()
        }

        binding.forgotbtn.setOnClickListener {
            callForgotPassApi()
        }


    }



    private fun callForgotPassApi() {

        val forgot = ForgotModel(binding.forgotemail.text.toString())

        val jsonObject = JSONObject()
        jsonObject.put("email", forgot.email)


            //val jsonParser =  JsonParser()
        val gson: JsonObject = JsonParser.parseString(jsonObject.toString()).asJsonObject

        // Forgot password api
        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                Toast.makeText(this@ForgotPassword,"Reset email link sent successfully..", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@ForgotPassword,LogIn::class.java)
                startActivity(intent)
                finish()


            }

            override fun onFailure(t: Throwable) {
                Toast.makeText(this@ForgotPassword,"error", Toast.LENGTH_SHORT).show()

                Log.e("test", "onFailure: ${t.message}")

            }
        }, RetrofitClientUser(this).getRetrofitClientUser(false).forgotUser(gson))
    }

}