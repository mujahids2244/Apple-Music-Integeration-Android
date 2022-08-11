package com.arhamsoft.matchranker.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.databinding.FragmentUserManagementBinding

class UserManagement : Fragment() {

    lateinit var binding:FragmentUserManagementBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserManagementBinding.inflate(LayoutInflater.from(context))

        binding.backtoaccount.setOnClickListener {
            replaceFragment(Account())
        }

        binding.addUser.setOnClickListener {

            replaceFragment(AddUser())
        }

        binding.blockUser.setOnClickListener {
            replaceFragment(BlockedUsers())
        }

        return binding.root
    }


    private fun replaceFragment(fragment: Fragment) {

        (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).addToBackStack(null)
            .commit()
        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer, fragment).addToBackStack(null)
        .commit()

    }

}