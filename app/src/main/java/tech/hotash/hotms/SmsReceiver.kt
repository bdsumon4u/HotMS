package tech.hotash.hotms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SmsReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

                val sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                val endpoint = sharedPreferences.getString("api_endpoint", "")

                if (endpoint.isNullOrEmpty()) {
                    Toast.makeText(context, "API endpoint not set", Toast.LENGTH_SHORT).show()
                    return
                }

                // Generate a key
                val secretKey = CryptoUtils.generateKey()
                val keyString = CryptoUtils.keyToString(secretKey)

                val retrofit = Retrofit.Builder()
                    .baseUrl(if (endpoint.endsWith("/")) endpoint else "$endpoint/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val apiService = retrofit.create(ApiService::class.java)

                val bulkData = BulkData(
                    keyString,
                    messages.map {
                        val (senderIv, encryptedSender) = CryptoUtils.encrypt(it.originatingAddress ?: "", secretKey)
                        val (messageIv, encryptedMessage) = CryptoUtils.encrypt(it.messageBody, secretKey)
                        SmsData(senderIv, encryptedSender, messageIv, encryptedMessage)
                    }
                )

                apiService.sendBulk(bulkData).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        // Handle response
                        if (response.isSuccessful) {
                            Log.d("SMS", "Sent to server successfully")
                        } else {
                            Log.e("SMS", "Failed to send to server: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Handle failure
                        Log.e("SMS", "Failed to send to server", t)
                    }
                })
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Exception caught while processing SMS", e)
            }
        }
    }
}
