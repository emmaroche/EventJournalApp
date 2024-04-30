package ie.setu.eventJournal.models

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
@IgnoreExtraProperties
@Parcelize
data class EventModel(
    var uid: String = "N/A",
    var name: String = "N/A",
    var description: String = "n/a",
    var type: String = "n/a",
    var date: String = "n/a",
    var time: String = "n/a",
    var amount: Int = 0,
    var profilepic: String = "",
    var image: String = "",
    val email: String = "joe@bloggs.com",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var isFavourite: Boolean = false)
    : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "description" to description,
            "type" to type,
            "date" to date,
            "time" to time,
            "amount" to amount,
            "profilepic" to profilepic,
            "image" to image,
            "email" to email,
            "latitude" to latitude,
            "longitude" to longitude,
            "isFavourite" to isFavourite
        )
    }
}


