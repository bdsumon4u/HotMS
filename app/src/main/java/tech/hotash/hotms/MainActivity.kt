package tech.hotash.hotms

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CommitPrefEdits")
    @Composable
    fun MyApp(sharedPreferences: SharedPreferences) {
        var endpoint by remember { mutableStateOf(sharedPreferences.getString("api_endpoint", "") ?: "") }
        var endpointError by remember { mutableStateOf<String?>(null) }
        val isServiceEnabled = sharedPreferences.getBoolean("service_enabled", false)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HotMS", color = MaterialTheme.colorScheme.primary) },
                    actions = {
                        var isEnabled by remember { mutableStateOf(isServiceEnabled) }
                        ToggleButton(
                            isEnabled = isEnabled,
                            onToggleChange = {
                                isEnabled = it
                                sharedPreferences.edit().putBoolean("service_enabled", isEnabled)

                                if (isEnabled) {
                                    ContextCompat.startForegroundService(this@MainActivity, Intent(this@MainActivity, SmsService::class.java))
                                } else {
                                    stopService(Intent(this@MainActivity, SmsService::class.java))
                                }
                            }
                        )
                    },
                )
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

    @Composable
    fun ToggleButton(
        isEnabled: Boolean,
        onToggleChange: (Boolean) -> Unit
    ) {
        val buttonColor = if (isEnabled) Color.Green else Color.Gray
        val buttonText = if (isEnabled) "ON" else "OFF"

        Button(
            onClick = { onToggleChange(!isEnabled) },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .height(36.dp)
                .width(80.dp)
        ) {
            Text(
                text = buttonText,
                color = Color.White
            )
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
