package com.arhamsoft.matchranker.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.arhamsoft.matchranker.R

class LoadingDialog(val mActivity: Activity) {
    private var isdialog: AlertDialog? = null


    fun startLoading(title:String){
        val infalter = mActivity.layoutInflater
        val dialogView = infalter.inflate(R.layout.loading_bar,null)
            dialogView.findViewById<TextView>(R.id.progressTitle).text = title
        val bulider = AlertDialog.Builder(mActivity)
        bulider.setView(50)
        bulider.setView(dialogView)
        bulider.setCancelable(false)
        isdialog = bulider.create()
        isdialog?.show()
    }
    fun isDismiss(){
        isdialog?.dismiss()
    }
}


