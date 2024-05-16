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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import tech.hotash.hotms.ui.theme.HotMSTheme

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.INTERNET,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC,
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (hasRequiredPermissions()) {
                startForegroundService(Intent(this, SmsService::class.java))
            } else {
                ActivityCompat.requestPermissions(this, requiredPermissions, 101)
            }
        } else {
            // ask for permissions
        }

        setContent {
            MyApp(sharedPreferences)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MyApp(sharedPreferences: SharedPreferences) {
        var endpoint by remember { mutableStateOf(sharedPreferences.getString("api_endpoint", "") ?: "") }
        var key by remember { mutableStateOf(sharedPreferences.getString("api_key", "") ?: "") }
        var secret by remember { mutableStateOf(sharedPreferences.getString("api_secret", "") ?: "") }

        var endpointError by remember { mutableStateOf<String?>(null) }
        var keyError by remember { mutableStateOf<String?>(null) }
        var secretError by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Settings") })
            }
        ) {innerPadding ->
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("HTTP Endpoint") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = endpointError != null,
                )
                if (endpointError != null) {
                    Text(endpointError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = keyError != null,
                )
                if (keyError != null) {
                    Text(keyError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("API Secret") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = secretError != null,
                )
                if (secretError != null) {
                    Text(secretError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Reset error messages
                    endpointError = null
                    keyError = null
                    secretError = null

                    var hasError = false

                    if (endpoint.isBlank()) {
                        endpointError = "HTTP Endpoint cannot be empty"
                        hasError = true
                    }

                    if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                        endpointError = "HTTP Endpoint must start with http:// or https://"
                        hasError = true
                    }

                    if (key.isBlank()) {
                        keyError = "API Key cannot be empty"
                        hasError = true
                    }
                    if (secret.isBlank()) {
                        secretError = "API Secret cannot be empty"
                        hasError = true
                    }

                    if (hasError) {
                        return@Button
                    }

                    sharedPreferences
                        .edit()
                        .putString("api_endpoint", endpoint)
                        .putString("api_key", key)
                        .putString("api_secret", secret)
                        .apply()

                    Toast.makeText(this@MainActivity, "Settings saved", Toast.LENGTH_LONG).show()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun hasRequiredPermissions(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
