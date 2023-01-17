package androidx.camera.compose.view

import android.util.Log
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.SurfaceRequest
import androidx.camera.view.PreviewView.ImplementationMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier

private const val TAG = "Preview"

@Composable
fun CameraPreview(
    modifier: Modifier,
    implementationMode: ImplementationMode = ImplementationMode.COMPATIBLE,
    onSurfaceProviderReady: (SurfaceProvider) -> Unit = {},
) {

    Log.d(TAG, "CameraPreview")

    val surfaceRequest by produceState<SurfaceRequest?>(initialValue = null, producer = {
        onSurfaceProviderReady(SurfaceProvider { request -> value = request })
    })

    when(implementationMode) {
        ImplementationMode.PERFORMANCE -> TODO()
        ImplementationMode.COMPATIBLE -> PreviewTexture(surfaceRequest = surfaceRequest)
    }
}