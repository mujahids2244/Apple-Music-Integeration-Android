package com.arhamsoft.matchranker.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.databinding.FragmentEditProfileBinding
import com.arhamsoft.matchranker.models.EditProfilePost
import com.arhamsoft.matchranker.models.UserProfileData
import com.arhamsoft.matchranker.models.UserProfileModel
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.User
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.arhamsoft.matchranker.util.Imgconvertors
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Runnable


class EditProfile : Fragment(R.layout.fragment_edit_profile) {

    lateinit var binding: FragmentEditProfileBinding
    lateinit var mActivity: FragmentActivity
    lateinit var user:User
    var bitmap: Bitmap?= null
    var  arr : ByteArray? =  null
    lateinit var database: UserDatabase
    var u_id:String?= null
    var encodedImage:String? = null
    var updatedEncodedImage:String? = null
    lateinit var loading: LoadingDialog
    var pic: String? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = FragmentEditProfileBinding.inflate(LayoutInflater.from(context))


        database = UserDatabase.getDatabase(requireContext())
        loading = LoadingDialog(requireActivity() as Activity)
        val th = Thread(Runnable{
            user = database.userDao().getUser()
            u_id = user.userId
            binding.etname.setText(user.fullname)
            binding.mail.text = user.email

            if(user.profilePhoto != null) {

                val byteArray: ByteArray? = user.profilePhoto
                bitmap = byteArray?.let { Imgconvertors.toBitmap(it) }!!
                binding.uploadpic.setImageBitmap(bitmap)
            }


        })
        th.start()
        th.join()





        val setimg =registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            val data: Intent? = result.data

            if (data != null) {
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, data.data)
                binding.uploadpic.setImageBitmap(bitmap)


                val imageUri: Uri? = data.data
                val imageStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri!!)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                 encodedImage = encodeImage(selectedImage)

                if (encodedImage!!.contains("/9j") == true){
                    updatedEncodedImage = encodedImage!!.replace("/9j","data:image/jpeg;base64,/9j")

                }

            }

        }

        binding.clickforUpload.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            setimg.launch(gallery)


        }


        binding.backtoaccount.setOnClickListener {
            (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer,Account()).remove(this).addToBackStack(null).commit();
            replaceFragment()

        }


        binding.updateupbtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                userProfileUpdateApi(binding.etname.text.toString())
                    withContext(Dispatchers.Main){
                        loading.startLoading("Please Wait")
                    }
            }

            Toast.makeText(requireContext(),"Updated",Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }





//bitmap to base 64
    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun updateData(res:UserProfileData) {
        if(res!=null) {
            if(res.profileImage != null) {
                if (res.profileImage.contains("data:image/jpeg;base64,") == true) {

                    pic = res.profileImage.replace("data:image/jpeg;base64,", "")
                    val imageBytes: ByteArray = Base64.decode(pic, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    arr = Imgconvertors.imgtobytearray(decodedImage)
                }
            }
            //base64 to bytearray to bitmap


            user.fullname = res.fullName

            user.profilePhoto = arr
        }
        val th = Thread(Runnable {
            val isInserted = database.userDao().updateUser(user)

            Log.e("user profile Updated", isInserted.toString())

        })
        th.start()
        th.join()
    }

    private fun userProfileUpdateApi(name:String){


        val checked = EditProfilePost (
            u_id!!,
            name,
            updatedEncodedImage,
            true
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                 response as UserProfileModel
                URLs.userProfileData = response.data
                updateData(response.data)

            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()

                Toast.makeText(requireContext(),"Api Syncing Failed User Profile Data"
                    ,Toast.LENGTH_SHORT).show()


            }

        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).userProfileUpdateResponse(gson))
    }


    private fun replaceFragment() {

        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer,Account()).addToBackStack(null).commit();

    }
}