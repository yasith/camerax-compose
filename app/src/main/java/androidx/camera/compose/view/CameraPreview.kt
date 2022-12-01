package androidx.camera.compose.view

import android.util.Log
import android.view.ViewTreeObserver
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.ViewPort
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred

private const val TAG = "Preview"

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    state: CameraPreviewState? = null,
    onTap: (x: Float, y: Float, meteringPointFactory: MeteringPointFactory) -> Unit = {_,_,_ -> },
    onZoom: (Float) -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val localContext = LocalContext.current
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    val transformableState = rememberTransformableState(onTransformation = { zoomChange, _, _ ->
        onZoom(zoomChange)
    })

    DisposableEffect(lifecycleOwner) {
        onDispose {
            state?.clear()
        }
    }

    LaunchedEffect(previewView) {
        previewView?.viewPort?.let {
            state?.viewPortDeferred?.complete(it)
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
                keepScreenOn = true
                scaleType = PreviewView.ScaleType.FILL_CENTER
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewPort?.let {
                            viewTreeObserver.removeOnGlobalLayoutListener(this)
                            state?.viewPortDeferred?.complete(it)
                        }
                    }
                })
            }
        },
        update = { view ->
            state?.lifecycleOwnerDeferred?.complete(lifecycleOwner)
            state?.previewViewDeferred?.complete(view)
            previewView = view
        }
    )
}

class CameraPreviewState {

    internal var previewViewDeferred = CompletableDeferred<PreviewView>()
    internal var viewPortDeferred = CompletableDeferred<ViewPort>()
    internal var lifecycleOwnerDeferred = CompletableDeferred<LifecycleOwner>()

    internal fun clear() {
        previewViewDeferred.cancel()
        viewPortDeferred.cancel()
        lifecycleOwnerDeferred.cancel()

        previewViewDeferred = CompletableDeferred()
        viewPortDeferred = CompletableDeferred()
        lifecycleOwnerDeferred = CompletableDeferred()
    }

    suspend fun getBitmap() = previewViewDeferred.await().bitmap

    suspend fun getMeteringPointFactory() = previewViewDeferred.await().meteringPointFactory

    @androidx.camera.view.TransformExperimental
    suspend fun getOutputTransform() = previewViewDeferred.await().outputTransform

    suspend fun getPreviewStreamState() = previewViewDeferred.await().previewStreamState

    suspend fun getSurfaceProvider() = previewViewDeferred.await().surfaceProvider

    suspend fun getViewPort() = viewPortDeferred.await()

    suspend fun getLifecycleOwner() = lifecycleOwnerDeferred.await()

    suspend fun getController() = previewViewDeferred.await().controller
    suspend fun setController(controller: CameraController) {
        previewViewDeferred.await().controller = controller
    }

    suspend fun getImplementationMode() = previewViewDeferred.await().implementationMode

    suspend fun setImplementationMode(implementationMode: PreviewView.ImplementationMode) {
        previewViewDeferred.await().implementationMode = implementationMode
    }

    suspend fun getScaleType() = previewViewDeferred.await().scaleType

    suspend fun setScaleType(scaleType: ScaleType) {
        previewViewDeferred.await().scaleType = scaleType
    }
}