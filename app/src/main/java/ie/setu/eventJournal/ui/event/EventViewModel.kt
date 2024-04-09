package ie.setu.eventJournal.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.setu.eventJournal.models.EventManager
import ie.setu.eventJournal.models.EventModel

class EventViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addEvent(event: EventModel) {
        status.value = try {
            EventManager.create(event)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}