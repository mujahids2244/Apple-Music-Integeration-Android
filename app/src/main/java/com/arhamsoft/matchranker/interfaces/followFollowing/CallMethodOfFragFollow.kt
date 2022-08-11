package com.arhamsoft.matchranker.interfaces.followFollowing

interface CallMethodOfFragFollow {

    fun callFunction(p_id:String, off: Int, isLoadMore: Boolean)
    fun callFunctionForAdap(commentId:Long,action:Long)
}