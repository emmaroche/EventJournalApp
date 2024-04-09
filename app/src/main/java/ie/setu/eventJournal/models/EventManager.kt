package ie.setu.eventJournal.models

import androidx.lifecycle.MutableLiveData
import ie.setu.eventJournal.api.EventClient
import ie.setu.eventJournal.api.EventWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

object EventManager : EventStore {

    private var events = ArrayList<EventModel>()

    override fun findAll(eventsList: MutableLiveData<List<EventModel>>) {

        val call = EventClient.getApi().findall()

        call.enqueue(object : Callback<List<EventModel>> {
            override fun onResponse(call: Call<List<EventModel>>,
                                    response: Response<List<EventModel>>
            ) {
                eventsList.value = response.body() as ArrayList<EventModel>
                Timber.i("Retrofit findAll() = ${response.body()}")
            }

            override fun onFailure(call: Call<List<EventModel>>, t: Throwable) {
                Timber.i("Retrofit findAll() Error : $t.message")
            }
        })
    }

    override fun findAll(email: String, eventsList: MutableLiveData<List<EventModel>>) {

        val call = EventClient.getApi().findall(email)

        call.enqueue(object : Callback<List<EventModel>> {
            override fun onResponse(call: Call<List<EventModel>>,
                                    response: Response<List<EventModel>>
            ) {
                eventsList.value = response.body() as ArrayList<EventModel>
                Timber.i("Retrofit findAll() = ${response.body()}")
            }

            override fun onFailure(call: Call<List<EventModel>>, t: Throwable) {
                Timber.i("Retrofit findAll() Error : $t.message")
            }
        })
    }

    override fun findById(email: String, id: String, event: MutableLiveData<EventModel>)   {

        val call = EventClient.getApi().get(email,id)

        call.enqueue(object : Callback<EventModel> {
            override fun onResponse(call: Call<EventModel>, response: Response<EventModel>) {
                event.value = response.body() as EventModel
                Timber.i("Retrofit findById() = ${response.body()}")
            }

            override fun onFailure(call: Call<EventModel>, t: Throwable) {
                Timber.i("Retrofit findById() Error : $t.message")
            }
        })
    }

    override fun create( event: EventModel) {

        val call = EventClient.getApi().post(event.email,event)

        call.enqueue(object : Callback<EventWrapper> {
            override fun onResponse(call: Call<EventWrapper>,
                                    response: Response<EventWrapper>
            ) {
                val eventWrapper = response.body()
                if (eventWrapper != null) {
                    Timber.i("Retrofit ${eventWrapper.message}")
                    Timber.i("Retrofit ${eventWrapper.data.toString()}")
                }
            }

            override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                Timber.i("Retrofit Error : $t.message")
            }
        })
    }

    override fun delete(email: String,id: String) {

        val call = EventClient.getApi().delete(email,id)

        call.enqueue(object : Callback<EventWrapper> {
            override fun onResponse(call: Call<EventWrapper>,
                                    response: Response<EventWrapper>
            ) {
                val eventWrapper = response.body()
                if (eventWrapper != null) {
                    Timber.i("Retrofit Delete ${eventWrapper.message}")
                    Timber.i("Retrofit Delete ${eventWrapper.data.toString()}")
                }
            }

            override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                Timber.i("Retrofit Delete Error : $t.message")
            }
        })
    }

    override fun update(email: String,id: String, event: EventModel) {

        val call = EventClient.getApi().put(email,id,event)

        call.enqueue(object : Callback<EventWrapper> {
            override fun onResponse(call: Call<EventWrapper>,
                                    response: Response<EventWrapper>
            ) {
                val eventWrapper = response.body()
                if (eventWrapper != null) {
                    Timber.i("Retrofit Update ${eventWrapper.message}")
                    Timber.i("Retrofit Update ${eventWrapper.data.toString()}")
                }
            }

            override fun onFailure(call: Call<EventWrapper>, t: Throwable) {
                Timber.i("Retrofit Update Error : $t.message")
            }
        })
    }
}