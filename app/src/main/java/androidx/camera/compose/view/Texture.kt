package androidx.camera.compose.view

import android.graphics.SurfaceTexture
import android.util.Log
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

private const val TAG = "Texture"

@Composable
fun PreviewTexture(
    surfaceRequest : SurfaceRequest?,
) {
    Log.d(TAG, "PreviewTexture")
    var resolution : Size by remember(surfaceRequest) {
        mutableStateOf(surfaceRequest?.resolution ?: Size(0, 0))
    }

    var currentSurfaceRequest : SurfaceRequest? by remember { mutableStateOf(null) }
    var surfaceTexture : SurfaceTexture? by remember { mutableStateOf(null) }

    val onSurfaceTextureEvent : (SurfaceTextureEvent) -> Boolean = {
            when(it) {
                is SurfaceTextureEvent.SurfaceTextureAvailable -> {
                    surfaceTexture = it.surface
                }
                is SurfaceTextureEvent.SurfaceTextureDestroyed -> {
                    surfaceTexture = null
                }
            }
        true
    }

    LaunchedEffect(key1 = surfaceRequest, block = {
        currentSurfaceRequest?.willNotProvideSurface()
        currentSurfaceRequest = surfaceRequest
    })

    surfaceTexture?.let {
        it.setDefaultBufferSize(resolution.width, resolution.height)
        val surface = android.view.Surface(surfaceTexture)
        surfaceRequest?.provideSurface(
            surface, Dispatchers.Main.asExecutor()
        ) {}
    }

    Texture(onSurfaceTextureEvent)
}

@Composable
fun Texture(
    onSurfaceTextureEvent: (SurfaceTextureEvent) -> Boolean = { _ -> true },
) {
    Log.d(TAG, "Texture")

    var textureView : TextureView? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { context ->
            TextureView(context).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        onSurfaceTextureEvent(SurfaceTextureEvent.SurfaceTextureAvailable(surface, width, height))
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        onSurfaceTextureEvent(SurfaceTextureEvent.SurfaceTextureSizeChanged(surface, width, height))
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        return onSurfaceTextureEvent(SurfaceTextureEvent.SurfaceTextureDestroyed(surface))
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                        onSurfaceTextureEvent(SurfaceTextureEvent.SurfaceTextureUpdated(surface))
                    }
                }
            }
        }, update = {
            textureView = it
        }
    )
}

sealed interface SurfaceTextureEvent {
    data class SurfaceTextureAvailable(
        val surface : SurfaceTexture,
        val width : Int,
        val height : Int
    ) : SurfaceTextureEvent

    data class SurfaceTextureSizeChanged(
        val surface : SurfaceTexture,
        val width : Int,
        val height : Int
    ) : SurfaceTextureEvent

    data class SurfaceTextureDestroyed(
        val surface: SurfaceTexture
    ) : SurfaceTextureEvent

    data class SurfaceTextureUpdated(
        val surface: SurfaceTexture
    ) : SurfaceTextureEvent
}