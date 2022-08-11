package com.arhamsoft.matchranker.fragment

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.PagerAdapter
import com.arhamsoft.matchranker.databinding.FragmentUserProfileBinding
import com.arhamsoft.matchranker.models.*
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*

class UserProfile : Fragment() {

     lateinit var binding: FragmentUserProfileBinding
     var p_id:String? = null
     var u_id:String? = null
    lateinit var loading:LoadingDialog
    var pic:String? = null
    var response: WatchUsersModel? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserProfileBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)
        response = URLs.watchUserResponse
        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
        }
        p_id = URLs.playerId

        if (response !=null) {

            if (response!!.data.profileImage?.contains("data:image/jpeg;base64,") == true) {

                pic = response!!.data.profileImage.replace("data:image/jpeg;base64,", "")
            }
            if (pic != null) {
                val imageBytes: ByteArray = Base64.decode(pic!!, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.uploadpic.setImageBitmap(decodedImage)
            }

            binding.name.text = response!!.data.name
            binding.totalComments.text = response!!.data.totalNoOfComments.toString()
            binding.totalLikes.text = response!!.data.totalNoOfLikes.toString()
            binding.totalFollowing.text = response!!.data.followingCount.toString()
            binding.totalFollowers.text = response!!.data.followersCount.toString()

            if (response!!.data.isFollowing){
                binding.follow.visibility = View.GONE
                binding.following.visibility = View.VISIBLE
            }
            else{
                binding.follow.visibility = View.VISIBLE
                binding.following.visibility = View.GONE
            }





        }



        binding.follow.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                getuserStatus(p_id!!, 0)
                withContext(Dispatchers.Main) {
                    loading.startLoading("please Wait")
                }

                URLs.noOfFollowing += 1
                URLs.follStatusCheck = 1
            }

            binding.follow.visibility = View.GONE
            binding.following.visibility = View.VISIBLE
        }

        binding.following.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                getuserStatus(p_id!!, 1)
                withContext(Dispatchers.Main) {
                    loading.startLoading("please Wait")
                }

                URLs.noOfFollowing -= 1
                URLs.follStatusCheck = 1
            }

            binding.follow.visibility = View.VISIBLE
            binding.following.visibility = View.GONE
        }

        binding.backtoaccount.setOnClickListener {

            (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).commit()

            if (URLs.fragCheck==1){
                replaceFragment(Followers())
            }
            else if(URLs.fragCheck==2){
                replaceFragment(Following())
            }
            else if(URLs.fragCheck == 4){
                replaceFragment(UserActivity())
            }
            else if(URLs.fragCheck==3){
                replaceFragment(AddUser())
            }
            else{
                replaceFragment(Account())
            }

        }



        // adding on click listener for our button.
        binding.openBottomSheet.setOnClickListener {

            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.mini_bottomsheet_dialog, null)
            val btnClose = view.findViewById<Button>(R.id.cancelSheet)
            val blockUser = view.findViewById<Button>(R.id.blockUser)

            blockUser.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    getuserStatus(p_id!!, 2)
                    withContext(Dispatchers.Main) {
                        loading.startLoading("please Wait")
                    }

                    if(binding.follow.visibility == View.VISIBLE){
                        URLs.noOfFollow -=1
                        URLs.follStatusCheck =3
                    }
                    else if(URLs.fragCheck == 1 && binding.following.visibility == View.VISIBLE){

                        URLs.noOfFollowing -= 1
                        URLs.noOfFollow -=1
                        URLs.follStatusCheck = 2
                    }
                    else{
                        URLs.noOfFollowing -= 1
                        URLs.follStatusCheck =4
                    }



                }

                dialog.dismiss()

            }

            btnClose.setOnClickListener {

                dialog.dismiss()
            }


            dialog.setCancelable(true)


            dialog.setContentView(view)


            dialog.show()
        }



        binding.rankSongs.setOnClickListener {

            if(binding.rankSongs.isClickable){

                binding.rankSongs.background = (ContextCompat.getDrawable(requireContext(),R.drawable.edit_text_color))
                binding.recentActivity.background = (ContextCompat.getDrawable(requireContext(),R.drawable.edit_text_shape))

            }
            binding.viewPager.currentItem = 0

        }

        binding.recentActivity.setOnClickListener {


            if(binding.recentActivity.isClickable){

                binding.recentActivity.background = (ContextCompat.getDrawable(requireContext(),R.drawable.edit_text_color))
                binding.rankSongs.background = (ContextCompat.getDrawable(requireContext(),R.drawable.edit_text_shape))

            }

            binding.viewPager.currentItem = 1


        }


        val fragments: ArrayList<Fragment> = arrayListOf(
            RankSongsInUP(),
            RecentActivity()

        )
        val viewPager: ViewPager2 = binding.viewPager
        val adapter = PagerAdapter(fragments, activity as MainScreen)
        binding.viewPager.adapter = adapter
        viewPager.currentItem = 0




        return binding.root
    }

        //follow or unfollow or block watch user
    private fun getuserStatus(playerId:String, connectivityType:Long) {

        val checked = UserStatusModelPost(
            u_id!!,
            connectivityType,
            playerId
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as FollowModel
                if(connectivityType == 2L){
                    (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this@UserProfile).commit()

                    if (URLs.fragCheck==1){
                        replaceFragment(Followers())

                    }
                    else if(URLs.fragCheck==2){
                        replaceFragment(Following())
                    }
                    else if(URLs.fragCheck==4){
                        replaceFragment(UserActivity())
                    }
                    else if(URLs.fragCheck==3){
                        replaceFragment(AddUser())
                    }
                    else{
                        replaceFragment(Account())
                    }



                }


            }

            override fun onFailure(t: Throwable) {
                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed user status..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
//                CoroutineScope(Dispatchers)
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).userStatus(gson))
    }


    private fun replaceFragment(fragment: Fragment) {
        (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).addToBackStack(null).commit();

        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer,fragment).addToBackStack(null).commit();

    }

}