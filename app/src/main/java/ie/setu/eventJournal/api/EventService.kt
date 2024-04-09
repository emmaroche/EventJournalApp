package ie.setu.eventJournal.api

import ie.setu.eventJournal.models.EventModel
import retrofit2.Call
import retrofit2.http.*

interface EventService {
    @GET("/donations")
    fun findall(): Call<List<EventModel>>

    @GET("/donations/{email}")
    fun findall(@Path("email") email: String?)
            : Call<List<EventModel>>

    @GET("/donations/{email}/{id}")
    fun get(@Path("email") email: String?,
            @Path("id") id: String): Call<EventModel>

    @DELETE("/donations/{email}/{id}")
    fun delete(@Path("email") email: String?,
               @Path("id") id: String): Call<EventWrapper>

    @POST("/donations/{email}")
    fun post(@Path("email") email: String?,
             @Body donation: EventModel)
            : Call<EventWrapper>

    @PUT("/donations/{email}/{id}")
    fun put(@Path("email") email: String?,
            @Path("id") id: String,
            @Body donation: EventModel
    ): Call<EventWrapper>
}

