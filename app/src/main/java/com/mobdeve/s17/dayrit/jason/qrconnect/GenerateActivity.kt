package com.mobdeve.s17.dayrit.jason.qrconnect

import android.content.Intent
import android.os.Bundle
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.platform.LocalContext

class GenerateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GenerateScreen()
        }
    }
}

@Composable
fun GenerateScreen(modifier: Modifier = Modifier) {
    var inputText = "https://example.com"  // Set a default URL for testing
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Text field to enter QR text
        BasicTextField(
            value = TextFieldValue(inputText),
            onValueChange = { inputText = it.text },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .border(1.dp, Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to generate QR code
        Button(
            onClick = {
                // Simulate QR generation here (use dummy data)
                // You can replace this with actual QR generation logic later
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(text = "Generate QR Code")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ImageView to show the QR code (dummy for now)
        val qrCodeImage: Bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)

        Spacer(modifier = Modifier.height(24.dp))

        // Button to go back to Home
        Button(
            onClick = {
                // Navigate back to MainActivity
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(text = "Back to Home")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GeneratePreview() {
    MaterialTheme {
        GenerateScreen()
    }
}