package ie.setu.eventJournal.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.setu.eventJournal.firebase.FirebaseDBManager
import ie.setu.eventJournal.models.EventModel
import timber.log.Timber
import java.lang.Exception

class ReportViewModel : ViewModel() {

    private val eventsList =
        MutableLiveData<List<EventModel>>()

    val observableEventsList: LiveData<List<EventModel>>
        get() = eventsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    fun load() {
        try {
            FirebaseDBManager.findAll(liveFirebaseUser.value?.uid!!, eventsList)
            Timber.i("Report Load Success : ${eventsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : ${e.toString()}")
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
}

