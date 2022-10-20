package androidx.camera.compose

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.compose.model.CameraState
import androidx.camera.compose.model.CameraUiState
import androidx.camera.compose.ui.theme.CameraXComposeTheme
import androidx.camera.compose.view.Preview
import androidx.camera.compose.view.PreviewState
import androidx.camera.compose.viewmodel.CameraComposeViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview


private const val TAG = "MainActivity"

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    private lateinit var permissionStateFlow: MutableStateFlow<PermissionState>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CameraXComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
                    permissionStateFlow = MutableStateFlow(permissionState)

                    if (permissionState.status.isGranted)  {
                        ViewFinder()
                    } else {
                        CameraPermission(permissionState)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPermission(cameraPermissionState : PermissionState) {
    Column {
        val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
            "The camera is important for this app. Please grant the permission."
        } else {
            "Camera not available"
        }

        Text(textToShow)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
            Text("Request permission")
        }
    }
}

@Composable
private fun ViewFinder(viewModel: CameraComposeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val cameraUiState : CameraUiState by viewModel.cameraUiState.collectAsState()

    if (cameraUiState.cameraState == CameraState.NOT_READY) {
        viewModel.initializeCamera()
    } else if (cameraUiState.cameraState == CameraState.READY){
        Box() {

            val previewState = PreviewState()
            Preview(
                modifier = Modifier.fillMaxSize(),
                state = previewState,
                onPreviewReady = { lifecycleOwner, surfaceProvider ->
                    viewModel.startPreview(
                        lifecycleOwner,
                        surfaceProvider
                    )
                },
                onTapToFocus = { focusMeteringAction -> viewModel.onTapToFocus(focusMeteringAction) },
                onZoom = { zoomScale -> viewModel.onZoom(zoomScale) }
            )
        }
    }
}
