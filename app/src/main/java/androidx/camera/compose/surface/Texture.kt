package androidx.camera.compose.surface

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView

private const val TAG = "Texture"

@Composable
fun Texture(
    onSurfaceTextureEvent: (SurfaceTextureEvent) -> Boolean = { _ -> true },
    onRequestBitmapReady: (() -> Bitmap?) -> Unit
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
            onRequestBitmapReady { -> it.bitmap }
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