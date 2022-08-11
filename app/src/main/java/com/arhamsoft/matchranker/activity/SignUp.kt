package com.arhamsoft.matchranker.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.arhamsoft.matchranker.databinding.ActivitySignUpBinding
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import java.util.regex.Matcher
import java.util.regex.Pattern
import android.content.pm.PackageManager
import com.arhamsoft.matchranker.usermodels.RegisterToAppleModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject


class SignUp : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    lateinit var loading:LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = LoadingDialog(this)

        val pm: PackageManager = this.packageManager


        binding.backtomain.setOnClickListener {

            onBackPressed()
            //this.finish()
        }

        binding.gotoLogin.setOnClickListener {

            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }


        binding.signupbtn.setOnClickListener {

            if (binding.etFullName.text.isEmpty() && binding.etEmail.text.isEmpty() && binding.etPassword.text.isEmpty()
                && binding.etVerifyPass.text.isEmpty()
            ) {
                Toast.makeText(this, "please fill all fields", Toast.LENGTH_SHORT).show()

            } else {

                if (binding.etEmail.text.isEmpty()) {
                    Toast.makeText(this, "Email field is empty", Toast.LENGTH_SHORT).show()
                } else if (binding.etFullName.text.isEmpty()) {
                    Toast.makeText(this, "full name field is empty", Toast.LENGTH_SHORT)
                        .show()
                } else if (binding.etPassword.text.isEmpty()) {
                    Toast.makeText(this, "password field is empty", Toast.LENGTH_SHORT)
                        .show()
                } else if (binding.etVerifyPass.text.isEmpty()) {
                    Toast.makeText(this, "Re-password field is empty", Toast.LENGTH_SHORT)
                        .show()
                } else if (binding.etPassword.text.toString() != binding.etVerifyPass.text.toString()) {
                    Toast.makeText(this, "you have entered wrong Re-password", Toast.LENGTH_SHORT)
                        .show()
                } else if (isValidPassword(binding.etPassword.text.toString())) {
                    Toast.makeText(
                        this,
                        "Atleast one uppercase, one lowercase, one special character and a digit is required",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else if (binding.etPassword.text.toString() == binding.etVerifyPass.text.toString()) {

                    if (!isPackageInstalled("com.apple.android.music", pm)) {
                        Toast.makeText(
                            this,
                            "Apple Music App is not installed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        loading.startLoading("Please Wait")
                        callUserRegisterApi()
                    }

                }
            }
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val password_pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$)$"
        pattern = Pattern.compile(password_pattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }



    private fun callUserRegisterApi() {

        val register = RegisterToAppleModel(
            binding.etEmail.text.toString(),
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString(),
            binding.etFullName.text.toString(),
            true
        )

//        val jsonObject = JSONObject()
//        jsonObject.put("UserName", register.UserName)
//        jsonObject.put("email", register.email)
//        jsonObject.put("fullName", register.fullName)
//        jsonObject.put("isAppleMusicSubscribed", register.isAppleMusicSubscribed)
//        jsonObject.put("password", register.password)


        //val jsonParser =  JsonParser()
        val gson: JsonObject = JsonParser.parseString(Gson().toJson(register)).asJsonObject

        //User register APi
        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                Log.e("register", "onSuccess: ${response} ")
                Toast.makeText(this@SignUp,"success",Toast.LENGTH_SHORT).show()

                val intent = Intent(this@SignUp, LogIn::class.java)
                startActivity(intent)
                finish()


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()

                Toast.makeText(this@SignUp,"${t.message}",Toast.LENGTH_SHORT).show()

                Log.e("test", "onFailure: ${t.message}")

            }
        }, RetrofitClientUser(this).getRetrofitClientUser(true).createUser2(gson))
    }
}
