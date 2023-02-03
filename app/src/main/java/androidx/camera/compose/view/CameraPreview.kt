package androidx.camera.compose.view

import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import androidx.camera.compose.surface.CombinedSurface
import androidx.camera.compose.surface.CombinedSurfaceEvent
import androidx.camera.compose.surface.SurfaceType
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.SurfaceRequest
import androidx.camera.view.PreviewView.ImplementationMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.mapNotNull

private const val TAG = "Preview"

@Composable
fun CameraPreview(
    modifier: Modifier,
    implementationMode: ImplementationMode = ImplementationMode.COMPATIBLE,
    onSurfaceProviderReady: (SurfaceProvider) -> Unit = {},
    onRequestBitmapReady: (() -> Bitmap?) -> Unit
) {
    Log.d(TAG, "CameraPreview")

    val surfaceRequest by produceState<SurfaceRequest?>(initialValue = null) {
        onSurfaceProviderReady(SurfaceProvider { request ->
            value?.willNotProvideSurface()
            value = request
        })
    }

    PreviewSurface(
        surfaceRequest = surfaceRequest,
    )

}

@Composable
fun PreviewSurface(
    surfaceRequest : SurfaceRequest?,
//    onRequestBitmapReady: (() -> Bitmap?) -> Unit,
    type: SurfaceType = SurfaceType.TEXTURE_VIEW,
    implementationMode: ImplementationMode = ImplementationMode.COMPATIBLE,
) {
    Log.d(TAG, "PreviewSurface")

    var surface: Surface? by remember { mutableStateOf(null) }

    LaunchedEffect(surfaceRequest, surface) {
        Log.d(TAG, "LaunchedEffect")
        snapshotFlow {
            if (surfaceRequest == null || surface == null) null
            else Pair(surfaceRequest, surface)
        }.mapNotNull { it }
            .collect { (request, surface) ->
                Log.d(TAG, "Collect: Providing surface")

                request.provideSurface(surface!!, Dispatchers.Main.asExecutor()) {}
            }
    }



    when(implementationMode) {
        ImplementationMode.PERFORMANCE -> TODO()
        ImplementationMode.COMPATIBLE -> CombinedSurface(
            onSurfaceEvent = { event ->
                surface = when (event)     {
                    is CombinedSurfaceEvent.SurfaceAvailable -> {
                        event.surface
                    }

                    is CombinedSurfaceEvent.SurfaceDestroyed ->  {
                        null
                    }
                }
            }
        )
    }
}