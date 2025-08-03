package com.mobdeve.s17.dayrit.jason.qrconnect

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.coroutines.launch

class ScanActivity : ComponentActivity() {
    private var barcodeView: BarcodeView? = null
    private var hasScanned = false
    private var hasCameraPermission by mutableStateOf(false)

    // permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            // permission granted, start camera
            barcodeView?.resume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check for camera permission
        hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        // enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            ScannerScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission) {
            barcodeView?.resume()
        }
        hasScanned = false
    }

    override fun onPause() {
        super.onPause()
        barcodeView?.pause()
    }

    // function to copy text to clipboard
    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Scanned QR Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    @Composable
    private fun ScannerScreen() {
        var scanResult by remember { mutableStateOf<String?>(null) }
        var isScanning by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            if (!hasCameraPermission) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (!hasCameraPermission) {
                // permission not granted screen
                PermissionScreen(
                    onRequestPermission = {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onGoBack = {
                        val intent = Intent(this@ScanActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            } else if (isScanning) {
                // Camera Scanner View - Full screen
                AndroidView(
                    factory = { context ->
                        BarcodeView(context).apply {
                            barcodeView = this
                            resume()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { view ->
                    if (!hasScanned) {
                        val callback = object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult) {
                                if (!hasScanned) {
                                    hasScanned = true
                                    scanResult = result.text
                                    isScanning = false
                                    view.pause()

                                    // save to database using coroutines
                                    val timestamp = System.currentTimeMillis().toString()
                                    val dbHelper = QRDatabaseHelper(applicationContext)

                                    lifecycleScope.launch {
                                        try {
                                            dbHelper.insertHistoryAsync("Scan", result.text, timestamp)
                                        } catch (e: Exception) {
                                            // fallback to synchronous method
                                            dbHelper.insertHistory("Scan", result.text, timestamp)
                                        }
                                    }
                                }
                            }

                            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {
                                // handle possible result points if needed
                            }
                        }
                        view.decodeContinuous(callback)
                    }
                }

                // overlay UI for scanning
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding() // add padding for status bar
                        .navigationBarsPadding() // add padding for nav bar
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // top instruction card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = "📱 Point your camera at a QR code to scan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // bottom navigation
                    Button(
                        onClick = {
                            val intent = Intent(this@ScanActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                // result screen
                scanResult?.let { result ->
                    ScanResultScreen(
                        result = result,
                        onScanAgain = {
                            isScanning = true
                            hasScanned = false
                            scanResult = null
                            barcodeView?.resume()
                        },
                        onGoHome = {
                            val intent = Intent(this@ScanActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onOpenUrl = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            startActivity(intent)
                        },
                        onCopyToClipboard = { text ->
                            copyToClipboard(this@ScanActivity, text)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun PermissionScreen(
        onRequestPermission: () -> Unit,
        onGoBack: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📷 Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "This app needs camera permission to scan QR codes. Please grant camera access to continue.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Grant Camera Permission")
            }

            Button(
                onClick = onGoBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go Back")
            }
        }
    }

    @Composable
    private fun ScanResultScreen(
        result: String,
        onScanAgain: () -> Unit,
        onGoHome: () -> Unit,
        onOpenUrl: (String) -> Unit,
        onCopyToClipboard: (String) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding() // add padding for status bar
                .navigationBarsPadding() // add padding for nav bar
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // success message
            Text(
                text = "✅ Scan Successful!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // result card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Scanned Content:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }
            }

            // action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // copy to clipboard button
                Button(
                    onClick = { onCopyToClipboard(result) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📋 Copy to Clipboard")
                }

                // open URL button (only for URLs)
                if (result.startsWith("http://") || result.startsWith("https://")) {
                    Button(
                        onClick = { onOpenUrl(result) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🌐 Open URL")
                    }
                }

                Button(
                    onClick = onScanAgain,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📱 Scan Another")
                }

                Button(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🏠 Back to Home")
                }
            }
        }
    }
}