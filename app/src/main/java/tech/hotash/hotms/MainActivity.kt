package tech.hotash.hotms

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.INTERNET,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
    )

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requiredPermissions.plus(Manifest.permission.FOREGROUND_SERVICE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.plus(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 101)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            startForegroundService(Intent(this, SmsService::class.java))
        } else {
            ContextCompat.startForegroundService(this, Intent(this, SmsService::class.java))
        }

        setContent {
            MyApp(sharedPreferences)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    // Handle permission denial
                    // You might show a message explaining why the permission is necessary
                    // and prompt the user to grant it again
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                    return
                }
            }
            // All permissions granted, proceed with app functionality
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CommitPrefEdits")
    @Composable
    fun MyApp(sharedPreferences: SharedPreferences) {
        var endpoint by remember { mutableStateOf(sharedPreferences.getString("api_endpoint", "") ?: "") }
        var endpointError by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HotMS", color = MaterialTheme.colorScheme.primary) },
                )
            }
        ) {innerPadding ->
            Column(modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)) {
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("API Endpoint") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = endpointError != null,
                )
                if (endpointError != null) {
                    Text(endpointError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Reset error messages
                    endpointError = null

                    var hasError = false

                    if (endpoint.isBlank()) {
                        endpointError = "HTTP Endpoint cannot be empty"
                        hasError = true
                    }

                    if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                        endpointError = "HTTP Endpoint must start with http:// or https://"
                        hasError = true
                    }

                    if (hasError) {
                        return@Button
                    }

                    sharedPreferences
                        .edit()
                        .putString("api_endpoint", endpoint)
                        .apply()

                    Toast.makeText(this@MainActivity, "Settings saved", Toast.LENGTH_LONG).show()
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Text("Save")
                }
            }
        }
    }

    private fun hasRequiredPermissions() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}
