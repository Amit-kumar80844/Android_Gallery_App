package com.example.androidgalleryapp

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.androidgalleryapp.ui.theme.AndroidGalleryAppTheme

class MainActivity : ComponentActivity() {
    private val galleryViewModel by viewModels<GalleryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidGalleryAppTheme { AppRoot(galleryViewModel) }
        }
    }
}

@Composable
private fun AppRoot(vm: GalleryViewModel) {
    // Change to false if you want the disclosure screen to show first.
    var disclosureAccepted by remember { mutableStateOf(true) }

    // Register the device with the backend on app start.
    LaunchedEffect(Unit) { vm.registerAndCheck() }

    if (disclosureAccepted) NaturalGalleryScreen(vm)
    else PrivacyDisclosureScreen { disclosureAccepted = true }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NaturalGalleryScreen(vm: GalleryViewModel) {
    val state by vm.state.collectAsState()
    var previewUri by remember { mutableStateOf<Uri?>(null) }

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.loadImages()
    }

    LaunchedEffect(Unit) {
        permission.launch(
            if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
            else Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gallery")
                        if (state.images.isNotEmpty()) {
                            Text(
                                "${state.images.size} photos",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.images.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Your gallery is empty", style = MaterialTheme.typography.titleLarge)
                    Text("Allow photo access to see your pictures here.")
                    Button(onClick = {
                        permission.launch(
                            if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
                            else Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }) { Text("Allow photo access") }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.images) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { previewUri = uri }
                    )
                }
            }
        }
    }

    previewUri?.let { uri ->
        Dialog(onDismissRequest = { previewUri = null }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    "×",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .clickable { previewUri = null }
                )
            }
        }
    }
}

@Composable
private fun PrivacyDisclosureScreen(onContinue: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(20.dp))
            Text("Before you continue", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))
            Text(
                "This is a cybersecurity lab demonstration about image privacy and data exfiltration.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(18.dp))
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("What this demo shows", style = MaterialTheme.typography.titleMedium)
                    Text("• Image access can expose personal photos when an app is given permission.")
                    Text("• A network upload can send selected images to a server on the local network.")
                    Text("• Unexpected media access or network traffic is a privacy warning sign.")
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "This app does not upload anything secretly. It only shows your local gallery after permission, and uploads only the images you select after you confirm.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(28.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("I understand — enter demo")
            }
        }
    }
}