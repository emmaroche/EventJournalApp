package ie.setu.eventJournal.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

//@Parcelize
//data class EventModel(
//    val _id: String = "N/A",
//    @SerializedName("paymenttype")
//    val paymentmethod: String = "N/A",
//    var message: String = "n/a",
//    var amount: Int = 0,
//    var upvotes: Int = 0,
//    val email: String = "joe@bloggs.com") : Parcelable

@Parcelize
data class EventModel(
    var uid: String = "N/A",
    var name: String = "N/A",
    var description: String = "n/a",
    var type: String = "n/a",
    var date: String = "n/a",
    var time: String = "n/a",
    var amount: Int = 0,
    val email: String = "joe@bloggs.com")
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
            "email" to email
        )
    }
}


