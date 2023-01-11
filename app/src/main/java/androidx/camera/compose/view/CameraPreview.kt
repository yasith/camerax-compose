package androidx.camera.compose.view

import android.util.Log
import android.view.ViewTreeObserver
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.ViewPort
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.ImplementationMode
import androidx.camera.view.PreviewView.ScaleType
import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

private const val TAG = "Preview"

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    state: CameraPreviewState? = null,
    onTap: (x: Float, y: Float, meteringPointFactory: MeteringPointFactory) -> Unit = {_,_,_ -> },
    onZoom: (Float) -> Unit = {},
    scaleType: ScaleType = ScaleType.FILL_CENTER,
    implementationMode: ImplementationMode = ImplementationMode.PERFORMANCE,
    onViewPortChanged: (ViewPort) -> Unit = {},
    onSurfaceProviderReady: (SurfaceProvider) -> Unit = {},
) {

    var previewView: PreviewView? by remember { mutableStateOf(null) }

    val transformableState = rememberTransformableState(onTransformation = { zoomChange, _, _ ->
        onZoom(zoomChange)
    })

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(previewView) {
        previewView?.viewPort?.let {
            onViewPortChanged(it)
        }

        previewView?.let {
            it.previewStreamState.observe(lifecycleOwner, Observer { streamState ->
                state?._previewStreamStateFlow?.value = streamState
            })
        }
    }

    AndroidView(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        Log.d(TAG, "onTap $offset")
                        onTap(offset.x, offset.y, previewView!!.meteringPointFactory)
                    }
                )
            }
            .transformable(state = transformableState)
            .then(modifier),
        factory = { context ->
            PreviewView(context).apply {
                this.scaleType = scaleType
                this.implementationMode = implementationMode
                keepScreenOn = true
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewPort?.let {
                            viewTreeObserver.removeOnGlobalLayoutListener(this)
                            onViewPortChanged(it)
                        }
                    }
                })
            }
        },
        update = { view ->
            onSurfaceProviderReady(view.surfaceProvider)
            previewView = view
        }
    )
}

class CameraPreviewState {

    internal val _previewStreamStateFlow = MutableStateFlow(PreviewView.StreamState.IDLE)

//    TODO
//    fun getBitmap() : ImageBitmap {
//    }

//    Temporarily removed
//    @androidx.camera.view.TransformExperimental
//    suspend fun getOutputTransform() = previewViewDeferred.await().outputTransform

    fun previewStreamStateFlow() : Flow<PreviewView.StreamState> = _previewStreamStateFlow.asStateFlow()
}

