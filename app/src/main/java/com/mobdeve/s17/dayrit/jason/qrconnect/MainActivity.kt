package com.mobdeve.s17.dayrit.jason.qrconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.mobdeve.s17.dayrit.jason.qrconnect.ui.theme.MOBICOMQRConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MOBICOMQRConnectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to QRConnect!", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Button to navigate to the Scan QR Activity
        Button(
            onClick = {
                // Navigate to ScanActivity
                val intent = Intent(context, ScanActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(text = "Scan QR Code")
        }

        // Button to navigate to the Generate QR Activity
        Button(
            onClick = {
                // Navigate to GenerateActivity
                val intent = Intent(context, GenerateActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(text = "Generate QR Code")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Button to view history
        Button(
            onClick = {
                // Navigate to History Activity
                val intent = Intent(context, HistoryActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(text = "View History")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MOBICOMQRConnectTheme {
        MainScreen()
    }
}