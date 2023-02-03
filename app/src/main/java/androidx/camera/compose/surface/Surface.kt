package androidx.camera.compose.surface

import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.compose.surface.SurfaceHolderEvent.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

private const val TAG = "Surface"

@Composable
fun Surface(
    onSurfaceHolderEvent: (SurfaceHolderEvent) -> Unit = { _ -> }
) {
    Log.d(TAG, "Surface")

    AndroidView(factory = {context ->
        SurfaceView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            holder.addCallback(
                object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        onSurfaceHolderEvent(SurfaceCreated(holder))
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        onSurfaceHolderEvent(SurfaceChanged(holder, width, height))
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        onSurfaceHolderEvent(SurfaceDestroyed(holder))
                    }
                }
            )
        }
    })
}


sealed interface SurfaceHolderEvent {
    data class SurfaceCreated(
        val holder: SurfaceHolder
    ) : SurfaceHolderEvent

    data class SurfaceChanged(
        val holder: SurfaceHolder,
        val width: Int,
        val height: Int
    ) : SurfaceHolderEvent

    data class SurfaceDestroyed(
        val holder: SurfaceHolder
    ) : SurfaceHolderEvent
}
