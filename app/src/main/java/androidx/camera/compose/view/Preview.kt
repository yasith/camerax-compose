package androidx.camera.compose.view

import android.util.Log
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner

private const val TAG = "Preview"

@Composable
fun Preview(
    modifier: Modifier = Modifier,
    state: PreviewState? = null,
    onPreviewReady: (LifecycleOwner, Preview.SurfaceProvider) -> Unit,
    onPreviewTapped: (x: Float, y: Float) -> Unit,
    onZoom: (Float) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val localContext = LocalContext.current
    val previewView = remember { PreviewView(localContext) }

    val transformableState = rememberTransformableState(onTransformation = { zoomChange, _, _ ->
        onZoom(zoomChange)
    })

    DisposableEffect(lifecycleOwner) {
        onDispose {
            Log.d(TAG, "onDispose")
            state?.clear()
        }
    }

    AndroidView(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        Log.d(TAG, "onTap $offset")
                        onPreviewTapped(offset.x, offset.y)
                    }
                )
            }
            .transformable(state = transformableState),
        factory = { previewView },
        update = { view ->
            state?.let { state ->
                state.previewView = view
            }
            onPreviewReady(lifecycleOwner, view.surfaceProvider)
        }
    )
}

class PreviewState {

    private var _previewView : PreviewView? = null
    internal var previewView : PreviewView
        get() = _previewView ?: throw UninitializedPropertyAccessException("Called before PreviewView was initialized")
        set(view) { _previewView = view }

    init {
        Log.d("PreviewState", "Initializing PreviewState")
    }

    internal fun clear() { _previewView = null }

    val bitmap
        get() = previewView.bitmap

    val meteringPointFactory
        get() = previewView.meteringPointFactory

    val outputTransform
        @androidx.camera.view.TransformExperimental
        get() = previewView.outputTransform

    val previewStreamState
        get() = previewView.previewStreamState

    val surfaceProvider
        get() = previewView.surfaceProvider

    val viewPort
        get() = previewView.viewPort

    var controller
        set(value) { previewView.controller = value }
        get() = previewView.controller

    var implementationMode
        set(value) { previewView.implementationMode = value}
        get() = previewView.implementationMode

    var scaleType
        set(value) {previewView.scaleType = value}
        get() = previewView.scaleType
}