package androidx.camera.compose.surface

import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import androidx.camera.compose.surface.SurfaceType.*
import androidx.compose.runtime.Composable

private const val TAG = "CombinedSurface"

@Composable
fun CombinedSurface(
    onSurfaceEvent: (CombinedSurfaceEvent) -> Unit,
    onRequestBitmapReady: (() -> Bitmap?) -> Unit = {},
    type: SurfaceType = SurfaceType.TEXTURE_VIEW,
) {
    Log.d(TAG, "PreviewTexture")

    when (type) {
        SurfaceType.SURFACE_VIEW -> Surface {
            when (it) {
                is SurfaceHolderEvent.SurfaceCreated -> {
                    onSurfaceEvent(CombinedSurfaceEvent.SurfaceAvailable(it.holder.surface))
                }

                is SurfaceHolderEvent.SurfaceDestroyed -> {
                    onSurfaceEvent(CombinedSurfaceEvent.SurfaceDestroyed)
                }
            }
        }

        SurfaceType.TEXTURE_VIEW -> Texture(
            {
                when (it) {
                    is SurfaceTextureEvent.SurfaceTextureAvailable -> {
                        onSurfaceEvent(CombinedSurfaceEvent.SurfaceAvailable(Surface(it.surface)))
                    }

                    is SurfaceTextureEvent.SurfaceTextureDestroyed -> {
                        onSurfaceEvent(CombinedSurfaceEvent.SurfaceDestroyed)
                    }
                }
                true
            },
            onRequestBitmapReady
        )
    }
}

sealed interface CombinedSurfaceEvent {
    data class SurfaceAvailable(
        val surface: Surface
    ) : CombinedSurfaceEvent

    object SurfaceDestroyed : CombinedSurfaceEvent
}

enum class SurfaceType {
    SURFACE_VIEW, TEXTURE_VIEW
}

