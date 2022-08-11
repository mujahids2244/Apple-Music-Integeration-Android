package com.arhamsoft.matchranker.interfaces.followFollowing

object CallMethodFragObject {

    private lateinit var callMethodOfFragFollow: CallMethodOfFragFollow
    private lateinit var notifyFrag: NotifyRecentActivity

    fun setListener(callMethodOfFragFollow: CallMethodOfFragFollow) {
        this.callMethodOfFragFollow = callMethodOfFragFollow
    }

    fun passingData(p_id:String, off: Int, isLoadMore: Boolean) {
        callMethodOfFragFollow.callFunction(p_id, off, isLoadMore)
    }

    fun passingDataAdap(commentId:Long,action:Long){
        callMethodOfFragFollow.callFunctionForAdap(commentId,action)
    }


    fun setListner(notifyRecentActivity: NotifyRecentActivity){
        this.notifyFrag = notifyRecentActivity
    }

    fun passNotify(){
        notifyFrag.notifyFrag()
    }
}