package data

data class Badge(

    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "", // Optional if you want badge icons
    val achieved: Boolean = false,
    val dateAchieved: Long? = null // timestamp


)
