package ie.setu.eventJournal.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.setu.eventJournal.firebase.FirebaseDBManager
import ie.setu.eventJournal.models.EventModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel : ViewModel() {

    private val eventsList =
        MutableLiveData<List<EventModel>>()

    // Reference used to help with implementing the fav button: https://www.geeksforgeeks.org/togglebutton-in-kotlin/ & https://stackoverflow.com/questions/61429820/android-check-uncheck-switch-button-through-databinding-with-livedata
    private val _eventChanged = MutableLiveData<EventModel>()
    val eventChanged: LiveData<EventModel> = _eventChanged

    val observableEventsList: LiveData<List<EventModel>>
        get() = eventsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    var readOnly = MutableLiveData(false)

    init { load() }

    fun load() {
        try {
            readOnly.value = false
            FirebaseDBManager.findAll(liveFirebaseUser.value?.uid!!, eventsList)
            Timber.i("Report Load Success : ${eventsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : ${e.toString()}")
        }
    }

    fun loadAll() {
        try {
            readOnly.value = true
            FirebaseDBManager.findAllFavourites(liveFirebaseUser.value?.uid!!, eventsList)
            Timber.i("Report LoadAll Success : ${eventsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }

    fun toggleFavorite(event: EventModel) {
        event.isFavourite = !event.isFavourite
        FirebaseDBManager.update(liveFirebaseUser.value?.uid!!, event.uid, event)
        _eventChanged.value = event
    }


    fun filterPastEvents() {
        val allEvents = eventsList.value
        if (allEvents != null) {
            val format = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val pastEvents = allEvents.filter {
                val eventDate = format.parse(it.date)
                eventDate != null && eventDate.before(Date())
            }
            eventsList.value = pastEvents
        } else {
            Timber.e("Events list is null. Cannot filter past events.")
        }
    }

    fun delete(userid: String, id: String) {
        try {
            FirebaseDBManager.delete(userid,id)
            Timber.i("Report Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Report Delete Error : $e.message")
        }
    }

    fun deleteAllEvents() {
        try {
            FirebaseDBManager.deleteAllEvents(liveFirebaseUser.value?.uid!!)
            Timber.i("Report Delete All Events Success")
        }
        catch (e: Exception) {
            Timber.i("Report Delete All Events Error : $e.message")
        }
    }
}
