package tech.hotash.hotms

import android.os.Build
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

    fun encrypt(message: String, secretKey: SecretKey): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        // Generate a random IV
        val iv = ByteArray(cipher.blockSize)
        SecureRandom().nextBytes(iv)

        // Initialize the cipher with the secret key and IV
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams)

        val encryptedBytes = cipher.doFinal(message.toByteArray())

        return Pair(base64Encode(iv), base64Encode(encryptedBytes))
    }

    private fun base64Decode(input: String): ByteArray {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getDecoder().decode(input)
        }
        return android.util.Base64.decode(input, android.util.Base64.DEFAULT)
    }

    private fun base64Encode(input: ByteArray): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(input)
        }
        return android.util.Base64.encodeToString(input, android.util.Base64.DEFAULT)
    }

    fun keyToString(secretKey: SecretKey): String {
        return base64Encode(secretKey.encoded)
    }

    fun stringToKey(keyString: String): SecretKey {
        val decodedKey = base64Decode(keyString)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }
}
