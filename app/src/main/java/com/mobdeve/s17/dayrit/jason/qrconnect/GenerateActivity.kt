package com.mobdeve.s17.dayrit.jason.qrconnect

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GenerateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            GenerateScreen()
        }
    }

    // function to copy text to clipboard
    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code Content", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    // function to save qr code to gallery
    private fun saveQRCodeToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val filename = "QRCode_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // for android 10 and above
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
                true
            } else {
                // for android 9 and below
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(imagesDir, filename)
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                // add to gallery
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(imageFile)
                context.sendBroadcast(mediaScanIntent)
                true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // function to share qr code
    private fun shareQRCode(context: Context, bitmap: Bitmap) {
        try {
            // create a temporary file in cache directory
            val cachePath = File(context.cacheDir, "images")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }

            val file = File(cachePath, "qr_code_${System.currentTimeMillis()}.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // get URI for the file using fileprovider
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // create share intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // start the chooser
            val chooser = Intent.createChooser(shareIntent, "Share QR Code")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing QR code: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    @Composable
    fun GenerateScreen(modifier: Modifier = Modifier) {
        var inputText by remember { mutableStateOf("https://example.com") }
        var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var isGenerating by remember { mutableStateOf(false) }
        var showSuccess by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val scrollState = rememberScrollState()

        // function to generate qr code
        fun generateQRCode(text: String) {
            if (text.isBlank()) return

            isGenerating = true
            showSuccess = false

            val barcodeEncoder = BarcodeEncoder()
            qrBitmap = try {
                barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
            } catch (e: Exception) {
                null
            }

            // save to database using coroutines
            val timestamp = System.currentTimeMillis().toString()
            val dbHelper = QRDatabaseHelper(context)

            if (context is ComponentActivity) {
                context.lifecycleScope.launch {
                    try {
                        dbHelper.insertHistoryAsync("Generate", text, timestamp)
                    } catch (e: Exception) {
                        // fallback to synchronous method
                        dbHelper.insertHistory("Generate", text, timestamp)
                    }
                    isGenerating = false
                    showSuccess = qrBitmap != null
                }
            } else {
                dbHelper.insertHistory("Generate", text, timestamp)
                isGenerating = false
                showSuccess = qrBitmap != null
            }
        }

        // generate initial qr code
        LaunchedEffect(Unit) {
            generateQRCode(inputText)
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding() // add padding for status bar
                .navigationBarsPadding() // add padding for nav bar
                .verticalScroll(scrollState) // add scrolling
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // title
            Text(
                text = "⚡ Generate QR Code",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Enter text or URL to create a QR code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // input card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Content:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter text or URL...") },
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            // generate button
            Button(
                onClick = { generateQRCode(inputText) },
                enabled = inputText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = if (isGenerating) "Generating..." else "Generate QR Code",
                    modifier = Modifier.padding(8.dp)
                )
            }

            // success message
            if (showSuccess) {
                Text(
                    text = "✅ QR Code Generated Successfully!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // qr code display
            qrBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier.padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your QR Code:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        )

                        Text(
                            text = "Scan this code with any QR scanner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // action buttons for qr code
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // copy to clipboard button
                    Button(
                        onClick = {
                            (context as GenerateActivity).copyToClipboard(context, inputText)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📋 Copy to Clipboard")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // save to gallery button
                        Button(
                            onClick = {
                                if ((context as GenerateActivity).saveQRCodeToGallery(context, bitmap)) {
                                    Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💾 Save")
                        }

                        // share button
                        Button(
                            onClick = {
                                (context as GenerateActivity).shareQRCode(context, bitmap)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📤 Share")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // add some space before navigation buttons
            Spacer(modifier = Modifier.height(24.dp))

            // navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, HistoryActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📋 History")
                }

                Button(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🏠 Home")
                }
            }

            // add bottom padding to ensure content is scrollable
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GeneratePreview() {
    MaterialTheme {
        GenerateActivity().GenerateScreen()
    }
}