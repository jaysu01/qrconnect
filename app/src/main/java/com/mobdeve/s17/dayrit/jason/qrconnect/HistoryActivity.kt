package com.mobdeve.s17.dayrit.jason.qrconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext

data class HistoryItem(val id: Int, val type: String, val content: String)

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HistoryScreen()
        }
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val historyItems = listOf(
        HistoryItem(1, "Scan", "https://example.com"),
        HistoryItem(2, "Generate", "Sample QR text 1"),
        HistoryItem(3, "Scan", "https://anotherlink.com"),
        HistoryItem(4, "Generate", "Sample QR text 2")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Adjusted to position below camera
    ) {
        // Default "History" title (removed previous background and border styling)
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // LazyColumn to display the history items
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(historyItems) { item ->
                Text(
                    text = "${item.type} - ${item.content}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back to Home Button
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
fun HistoryPreview() {
    MaterialTheme {
        HistoryScreen()
    }
}