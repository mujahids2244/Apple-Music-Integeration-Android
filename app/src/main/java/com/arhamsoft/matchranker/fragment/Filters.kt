package com.arhamsoft.matchranker.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.*




class Filters : Fragment(R.layout.fragment_filters) {

    lateinit var binding: FragmentFiltersBinding



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = FragmentFiltersBinding.inflate(LayoutInflater.from(context))


        return binding.root
    }

}