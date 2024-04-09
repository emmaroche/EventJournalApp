package ie.setu.eventJournal.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.setu.eventJournal.models.EventManager
import ie.setu.eventJournal.models.EventModel
import timber.log.Timber

class EventDetailViewModel : ViewModel() {
    private val event = MutableLiveData<EventModel>()

    var observableEvent: LiveData<EventModel>
        get() = event
        set(value) {event.value = value.value}

    fun getEvent(email:String, id: String) {
        try {
            EventManager.findById(email, id, event)
            Timber.i("Detail getEvent() Success : ${event.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Detail getEvent() Error : $e.message")
        }
    }

    fun updateEvent(email:String, id: String,event: EventModel) {
        try {
            EventManager.update(email, id, event)
            Timber.i("Detail update() Success : $event")
        }
        catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }
}