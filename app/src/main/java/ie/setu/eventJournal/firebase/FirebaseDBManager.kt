package ie.setu.eventJournal.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ie.setu.eventJournal.models.EventModel
import ie.setu.eventJournal.models.EventStore
import timber.log.Timber

object FirebaseDBManager : EventStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAll(eventsList: MutableLiveData<List<EventModel>>) {
        database.child("user-events")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Event error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<EventModel>()
                    val children = snapshot.children
                    children.forEach {
                        val event = it.getValue(EventModel::class.java)
                        // Make sure the fav icon does not reset to false on refresh
                        event?.isFavourite = it.child("isFavourite").getValue(Boolean::class.java) ?: false
                        localList.add(event!!)
                    }
                    database.child("user-events")
                        .removeEventListener(this)

                    eventsList.value = localList
                }
            })
    }

    override fun findAllFavourites(userid: String, eventsList: MutableLiveData<List<EventModel>>) {

        database.child("user-events").child(userid.toString())
            .orderByChild("isFavourite").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Event error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<EventModel>()
                    val children = snapshot.children
                    children.forEach {
                        val event = it.getValue(EventModel::class.java)
                        // Make sure the fav icon does not reset to false on refresh
                        event?.isFavourite = it.child("isFavourite").getValue(Boolean::class.java) ?: false
                        localList.add(event!!)
                    }
                    database.child("user-events").child(userid.toString())
                        .removeEventListener(this)

                    eventsList.value = localList
                }
            })
    }

    override fun findAll(userid: String, eventsList: MutableLiveData<List<EventModel>>) {

        database.child("user-events").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Event error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<EventModel>()
                    val children = snapshot.children
                    children.forEach {
                        val event = it.getValue(EventModel::class.java)
                        // Make sure the fav icon does not reset to false on refresh
                        event?.isFavourite = it.child("isFavourite").getValue(Boolean::class.java) ?: false
                        localList.add(event!!)
                    }
                    database.child("user-events").child(userid)
                        .removeEventListener(this)

                    eventsList.value = localList
                }
            })
    }

    override fun findById(userid: String, eventid: String, event: MutableLiveData<EventModel>) {

        database.child("user-events").child(userid)
            .child(eventid).get().addOnSuccessListener {
                event.value = it.getValue(EventModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener {
                Timber.e("firebase Error getting data $it")
            }
    }

    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, event: EventModel) {
        Timber.i("Firebase DB Reference : $database")

        val uid = firebaseUser.value!!.uid
        val key = database.child("user-events").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        event.uid = key
        val eventValues = event.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/user-events/$uid/$key"] = eventValues

        database.updateChildren(childAdd)
    }

    override fun delete(userid: String, eventid: String) {

        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-events/$userid/$eventid"] = null

        database.updateChildren(childDelete)
    }

    override fun deleteAllEvents(userid: String) {

        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-events/$userid"] = null

        database.updateChildren(childDelete)
    }

    override fun update(userid: String, eventid: String, event: EventModel) {

        val eventValues = event.toMap()

        val childUpdate: MutableMap<String, Any?> = HashMap()
        childUpdate["user-events/$userid/$eventid"] = eventValues

        database.updateChildren(childUpdate)
    }

    fun updateProfilePicture(userid: String, imageUri: String) {
        val userEvents = database.child("user-events").child(userid)
        userEvents.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.ref.child("profilepic").setValue(imageUri)
                }
            }
        })
    }

    fun updateLocationImage(eventId: String, imageUri: String) {
        database.child("user-events").child(eventId).child("image").setValue(imageUri)
    }
}