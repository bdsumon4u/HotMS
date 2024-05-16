package tech.hotash.hotms

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("bulk")
    fun sendBulk(@Body bulkData: BulkData): Call<ResponseBody>
}

data class SmsData(
    val originID: String,
    val senderAddress: String,
    val contentID: String,
    val messageBody: String,
)

data class BulkData(
    val bulkID: String,
    val messages: List<SmsData>
)
