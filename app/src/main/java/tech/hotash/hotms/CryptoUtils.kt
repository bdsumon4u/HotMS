package tech.hotash.hotms

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

object CryptoUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256

    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(message: String, secretKey: SecretKey): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Generate a random IV
        val iv = ByteArray(cipher.blockSize)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)

        // Initialize the cipher with the secret key and IV
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams)

        val encryptedBytes = cipher.doFinal(message.toByteArray())

        // Encode the IV and encrypted message to Base64
        val ivString = Base64.getEncoder().encodeToString(iv)
        val encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes)

        return Pair(ivString, encryptedMessage)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun keyToString(secretKey: SecretKey): String {
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stringToKey(keyString: String): SecretKey {
        val decodedKey = Base64.getDecoder().decode(keyString)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }
}
