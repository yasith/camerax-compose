package androidx.camera.compose

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.compose.model.CameraState
import androidx.camera.compose.model.CameraUiState
import androidx.camera.compose.ui.theme.CameraXComposeTheme
import androidx.camera.compose.view.CameraPreview
import androidx.camera.compose.view.CameraPreviewState
import androidx.camera.compose.viewmodel.CameraComposeViewModel
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview.SurfaceProvider
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*
import kotlinx.coroutines.flow.MutableStateFlow


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
private fun ViewFinder(viewModel: CameraComposeViewModel = viewModel()) {

    val cameraUiState : CameraUiState by viewModel.cameraUiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    val onSurfaceProviderReady : (SurfaceProvider) -> Unit = {
        viewModel.startPreview(lifecycleOwner, it)
    }

    if (cameraUiState.cameraState == CameraState.NOT_READY) {
        viewModel.initializeCamera()
    } else if (cameraUiState.cameraState == CameraState.READY){
        Box() {
            CameraPreview(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                Log.d(TAG, "onDoubleTap $offset")
                                viewModel.flipCamera()
                            }
                        )
                    },
                onTap = { x, y, meteringPointFactory ->
                    Log.d(TAG, "onTap: $x, $y")
                    val meteringPoint = meteringPointFactory.createPoint(x, y)
                    val focusMeteringAction = FocusMeteringAction.Builder(meteringPoint).build()
                    viewModel.onTapToFocus(focusMeteringAction) },
                onZoom = { zoomScale -> viewModel.onZoom(zoomScale) },
                onSurfaceProviderReady = onSurfaceProviderReady
            )
        }
    }
}
