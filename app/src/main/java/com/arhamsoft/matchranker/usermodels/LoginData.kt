package com.arhamsoft.matchranker.usermodels


data class LoginData (
    val token: String,
    val email:String,
    val userId: String,
    val fullname: String
)