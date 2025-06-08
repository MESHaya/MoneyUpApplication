package com.example.firebase_test_application

data class Badge(

    val badge_id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val achieved: Boolean = false,
    val dateAchieved: Long? = null // timestamp


)
