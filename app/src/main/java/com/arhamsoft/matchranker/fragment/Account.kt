package com.arhamsoft.matchranker.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.LogIn
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.databinding.FragmentAccountBinding
import com.arhamsoft.matchranker.models.RefreshTokenModel
import com.arhamsoft.matchranker.models.TokenPost
import com.arhamsoft.matchranker.models.UserProfileData
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.User
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.arhamsoft.matchranker.util.CustomSharedPreference
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser


class Account : Fragment(R.layout.fragment_account) {

    lateinit var binding: FragmentAccountBinding
    var bitmap: Bitmap?= null
    lateinit var database: UserDatabase
    lateinit var user : User
    var u_token: String? = null
    var userProfileData: UserProfileData? = null
    var pic: String?= null
    lateinit var loading: LoadingDialog
    lateinit var sharedPreference: CustomSharedPreference



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = FragmentAccountBinding.inflate(LayoutInflater.from(context))
        userProfileData = URLs.userProfileData
        database = UserDatabase.getDatabase(requireContext())
        loading = LoadingDialog(requireContext() as Activity)
        sharedPreference = CustomSharedPreference(requireContext())

        val th = Thread(Runnable {
            user = database.userDao().getUser()
            u_token = user.token
            binding.name.text = userProfileData?.fullName
            binding.email.text = userProfileData?.email
            binding.follower.text = userProfileData?.numberofFollowers.toString()
            binding.following.text = userProfileData?.numberOfFollowings.toString()

            if(URLs.follStatusCheck==1){
//                binding.follower.text = URLs.noOfFollowing.toString()
                binding.following.text = URLs.noOfFollowing.toString()
            }
            else if(URLs.follStatusCheck ==2){
                binding.follower.text = URLs.noOfFollowing.toString()
                binding.following.text = URLs.noOfFollow.toString()
            }
            else if (URLs.follStatusCheck ==3){
                binding.follower.text = URLs.noOfFollow.toString()
            }
            else if (URLs.follStatusCheck ==4){
                binding.following.text = URLs.noOfFollowing.toString()
            }


            if(userProfileData?.profileImage != null) {

                if (userProfileData?.profileImage!!.contains("data:image/jpeg;base64," ) == true){

                    pic = userProfileData?.profileImage!!.replace("data:image/jpeg;base64,","")
                }

                val imageBytes:ByteArray = Base64.decode(pic, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.uploadpic.setImageBitmap(decodedImage)

            }


        })
        th.start()
        th.join()

        binding.gotoprofile.setOnClickListener {

            replaceFragment(EditProfile())
        }

        binding.gotoUseruni.setOnClickListener {

            replaceFragment(UserUniverse())

        }

        binding.gotoAppleSearch.setOnClickListener {

            replaceFragment(SearchAppleMusic())
        }

        binding.logout.setOnClickListener {

            userLogoutApi()
        }
        binding.activity.setOnClickListener {

            replaceFragment(UserActivity())
        }
        binding.userManage.setOnClickListener {
            replaceFragment(UserManagement())
        }

        binding.gotofollow.setOnClickListener {
            URLs.watchUserActivityList.clear()
            replaceFragment(Followers())
        }

        binding.gotofollowing.setOnClickListener {
            URLs.watchUserActivityList.clear()
            replaceFragment(Following())
        }

        return binding.root
    }



    private fun userLogoutApi() {


        val checked = TokenPost(
            u_token!!,
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as RefreshTokenModel
                deleteData()
                sharedPreference.clearSharedPreference()
                activity?.let{
                    val intent = Intent (it, LogIn::class.java)
                    it.startActivity(intent)
                }
                requireActivity().finishAffinity()

                Toast.makeText(requireContext(),"You've been logged out.", Toast.LENGTH_LONG)

            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed logout ..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(true).logoutUser(gson))
    }

    private fun deleteData() {
        val th = Thread(Runnable {
            database.userDao().deleteUser()
        })
        th.start()
        th.join()
    }

    private fun replaceFragment(fragment: Fragment) {
        (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).addToBackStack(null).commit()
        val fragmentTransaction = (activity as MainScreen).supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragContainer, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }

}