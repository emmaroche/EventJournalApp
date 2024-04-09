package ie.setu.eventJournal.models

import androidx.lifecycle.MutableLiveData

interface EventStore {
    fun findAll(eventsList:
                MutableLiveData<List<EventModel>>)
    fun findAll(email:String,
                eventsList: MutableLiveData<List<EventModel>>)
    fun findById(email:String, id: String, event: MutableLiveData<EventModel>)
    fun create(event: EventModel)
    fun delete(email:String,id: String)
    fun update(email: String,id: String, event: EventModel)
}

