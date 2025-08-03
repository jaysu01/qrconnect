package com.mobdeve.s17.dayrit.jason.qrconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HistoryItem(val id: Int, val type: String, val content: String, val timestamp: String)

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            HistoryScreen { finish() }
        }
    }
}

@Composable
fun HistoryScreen(onBackPressed: () -> Unit = {}) {
    val context = LocalContext.current
    var historyItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // create database helper instance
    val dbHelper = remember { QRDatabaseHelper(context) }

    // function to load history items
    fun loadHistory() {
        if (context is ComponentActivity) {
            context.lifecycleScope.launch {
                try {
                    isLoading = true
                    val items = dbHelper.getAllHistoryAsync()
                    historyItems = items
                    isLoading = false
                } catch (e: Exception) {
                    isLoading = false
                    // fallback to synchronous method if async fails
                    historyItems = dbHelper.getAllHistory()
                }
            }
        } else {
            // fallback for non-componentactivity contexts
            historyItems = dbHelper.getAllHistory()
            isLoading = false
        }
    }

    // function to delete item
    fun deleteItem(id: Int) {
        if (context is ComponentActivity) {
            context.lifecycleScope.launch {
                try {
                    dbHelper.deleteHistoryAsync(id)
                    loadHistory() // refresh the list
                } catch (e: Exception) {
                    // fallback to synchronous method
                    dbHelper.deleteHistory(id)
                    loadHistory()
                }
            }
        } else {
            dbHelper.deleteHistory(id)
            loadHistory()
        }
    }

    // function to clear all history
    fun clearAllHistory() {
        if (context is ComponentActivity) {
            context.lifecycleScope.launch {
                try {
                    dbHelper.clearAllHistoryAsync()
                    loadHistory() // refresh the list
                } catch (e: Exception) {
                    // fallback to synchronous method
                    dbHelper.clearAllHistory()
                    loadHistory()
                }
            }
        } else {
            dbHelper.clearAllHistory()
            loadHistory()
        }
    }

    // load history when composable is first created
    LaunchedEffect(Unit) {
        loadHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // add padding for status bar
            .navigationBarsPadding() // add padding for nav bar
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // "history" title
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(24.dp)
            )
        } else if (historyItems.isEmpty()) {
            // show message when no history items exist
            Text(
                text = "No history items found",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            // display the history items
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.type,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.type == "Scan") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = item.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = formatTimestamp(item.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // delete button for each history item
                            Button(
                                onClick = { deleteItem(item.id) }
                            ) {
                                Text(text = "Delete")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // button to clear all history
        if (historyItems.isNotEmpty()) {
            Button(
                onClick = { clearAllHistory() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Clear All History")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // back to home button
        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                onBackPressed()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Back to Home")
        }
    }
}

// helper function to format timestamp
fun formatTimestamp(timestamp: String): String {
    return try {
        val date = Date(timestamp.toLong())
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        timestamp
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryPreview() {
    MaterialTheme {
        HistoryScreen()
    }
}