package com.arhamsoft.matchranker.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apple.android.sdk.authentication.AuthenticationFactory
import com.apple.android.sdk.authentication.AuthenticationManager
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.ActivityLogInBinding
import com.arhamsoft.matchranker.models.RefreshTokenModel
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.room.User
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.LoginToAppleModel
import com.arhamsoft.matchranker.usermodels.RegisterResponse
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.arhamsoft.matchranker.util.AppPreferences
import com.arhamsoft.matchranker.util.CustomSharedPreference
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LogIn : AppCompatActivity() {
    private var showPass = false
    lateinit var binding: ActivityLogInBinding
    lateinit var authenticationManager : AuthenticationManager
    lateinit var database: UserDatabase
    lateinit var sharedPreference: CustomSharedPreference
    private val REQUESTCODE_APPLEMUSIC_AUTH = 3456
    lateinit var loading:LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticationManager = AuthenticationFactory.createAuthenticationManager(this)
        val pm: PackageManager = this.packageManager
        database = UserDatabase.getDatabase(this)
        sharedPreference = CustomSharedPreference(this)
         loading = LoadingDialog(this)

        CoroutineScope(Dispatchers.IO).launch {
            getdeveloperToken()
        }



        binding.backtomain.setOnClickListener {

            onBackPressed()
            //this.finish()
        }

        binding.gotoSignup.setOnClickListener {

            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }
        binding.tvforgot.setOnClickListener {

            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)

        }

        binding.loginbtn.setOnClickListener {




            val userToken: String? = AppPreferences.getInstance(this).getAppleMusicUserToken()
            val devToken: String? = AppPreferences.getInstance(this).getDeveloperToken()
            if (binding.etemail.text.isEmpty() && binding.etpassword.text.toString().isEmpty()) {
                Toast.makeText(this, "please fill all fields", Toast.LENGTH_SHORT).show()
            }
            else if (binding.etemail.text.isEmpty() || binding.etpassword.text.toString().isEmpty()){
                Toast.makeText(this, "Enter the missing field", Toast.LENGTH_SHORT).show()

            } else {

                 if (!isPackageInstalled("com.apple.android.music", pm)) {
                    Toast.makeText(
                        this,
                        "Apple Music App is not installed.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    if (userToken != null && !userToken.isEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                loading.startLoading("Please Wait")
                            }
                            getdeveloperToken()
                            callUserLoginApi()
                        }

                    } else {
                        appleMusicAuth()

                    }

            }


            }





        }
        binding.ivtogglepass.setOnClickListener {
            showPass = !showPass
            showPassword(showPass)
        }

        showPassword(showPass)
    }


    private fun showPassword(isShow: Boolean) {
        if (isShow) {
            // To show the password
            binding.etpassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.ivtogglepass.setImageResource(R.drawable.ic_baseline_visibility_off_24)
        } else {
            // To hide the password
            binding.etpassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.ivtogglepass.setImageResource(R.drawable.ic_baseline_visibility_24)
        }
    }

    private fun getdeveloperToken() {

        var dev_token:String? = null
//        val user = User()
//        val checked = TokenPost(
//            u_token!!,
//        )


//        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {

                response as RefreshTokenModel
                dev_token = response.data.token

                AppPreferences.getInstance(this@LogIn).setDeveloperToken(dev_token)
                Toast.makeText(
                    this@LogIn,
                    "new developer token fetched",
                    Toast.LENGTH_LONG
                ).show()


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    this@LogIn,
                    "Api Syncing Failed developer token ..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, RetrofitClientUser(this).getRetrofitClientUser(false).getDeveloperToken())
    }



    //function for authentication (connect apple music with app)
    fun appleMusicAuth()
    {


        val intent: Intent = authenticationManager.createIntentBuilder(getString(R.string.developer_token))
                .setHideStartScreen(true)
                .setContextId("1100742453") // invoke build to generate the intent, make sure to use startActivityForResult if you care about the music-user-token being returned.
                .build()


        startActivityForResult(intent,REQUESTCODE_APPLEMUSIC_AUTH)



    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        var userTok: String? = ""
        val devToken: String? = AppPreferences.getInstance(this).getDeveloperToken()

        if (requestCode == REQUESTCODE_APPLEMUSIC_AUTH) {
            val tokenResult = authenticationManager.handleTokenResult(data)
            if (!tokenResult.isError) {
                val musicUserToken = tokenResult.musicUserToken
                Toast.makeText(this,"UT:${musicUserToken} ",Toast.LENGTH_SHORT).show()
                //userTok = musicUserToken
                //AppPreferences().with(this)?.setAppleMusicUserToken(musicUserToken)
                AppPreferences.getInstance(this).setAppleMusicUserToken(musicUserToken)
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        loading.startLoading("Please Wait")
                    }
                    getdeveloperToken()
                    callUserLoginApi()

                }

            } else {
                val error = tokenResult.error
                userTok = "Error getting token: $error"
                Toast.makeText(this,"${userTok} ",Toast.LENGTH_SHORT).show()
            }



        } else {
            super.onActivityResult(requestCode, resultCode, data)

        }
    }


    private fun callUserLoginApi() {
        val user = User()

        val login = LoginToAppleModel(
            binding.etemail.text.toString(),
            binding.etpassword.text.toString()
        )



        //val jsonParser =  JsonParser()
        val gson: JsonObject = JsonParser.parseString(Gson().toJson(login)).asJsonObject

        //User login APi
        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {

                runOnUiThread {
                    loading.isDismiss()
                }
                val loginModel = response as RegisterResponse

                user.email = loginModel.data.email
                user.token = loginModel.data.token
                user.userId = loginModel.data.userId
                user.fullname = loginModel.data.fullname
                insertData(user)

                Toast.makeText(this@LogIn,"Data saved in db",Toast.LENGTH_SHORT).show()

                Log.e("login", "onSuccess: ${response} ")

                sharedPreference.saveLogin("LOGIN",true)

                val intent = Intent(this@LogIn,MainScreen::class.java)
                startActivity(intent)
                finish()


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()

                Toast.makeText(this@LogIn,"login onFailure: ${t.message}",Toast.LENGTH_SHORT).show()


            }
        }, RetrofitClientUser(this).getRetrofitClientUser(true).loginUser(gson))
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun insertData(user: User) {

        val th = Thread(Runnable {
            val isInserted = database.userDao().insertUser(user)

            Log.e("Data inserted", isInserted.toString())

        })
        th.start()
        th.join()
    }


}