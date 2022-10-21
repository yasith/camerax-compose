package androidx.camera.compose.viewmodel

import android.app.Application
import android.util.Log
import androidx.camera.compose.model.CameraState
import androidx.camera.compose.model.CameraUiState
import androidx.camera.compose.model.CaptureState
import androidx.camera.core.*
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

private const val TAG = "CameraComposeViewModel"

class CameraComposeViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera : Camera

    private val imageCapture = ImageCapture.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()

    private val preview = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()

    private val _cameraUiState: MutableStateFlow<CameraUiState> = MutableStateFlow(CameraUiState())
    val cameraUiState: StateFlow<CameraUiState> = _cameraUiState

    private val _captureUiState: MutableStateFlow<CaptureState> =
        MutableStateFlow(CaptureState.CaptureNotReady)
    val captureUiState: StateFlow<CaptureState> = _captureUiState

    private var zoomScale = 1.0f

    fun initializeCamera() {
        Log.d(TAG, "initializing camera")
        viewModelScope.launch {
            val currentCameraUiState = _cameraUiState.value

            // wait for the camera provider instance and extensions manager instance
            cameraProvider = ProcessCameraProvider.getInstance(getApplication()).await()

            val availableCameraLens =
                listOf(
                    CameraSelector.LENS_FACING_BACK,
                    CameraSelector.LENS_FACING_FRONT
                ).filter { lensFacing ->
                    cameraProvider.hasCamera(cameraLensToSelector(lensFacing))
                }


            // prepare the new camera UI state which is now in the READY state and contains the list
            // of available extensions, available lens faces.
            val newCameraUiState = currentCameraUiState.copy(
                cameraState = CameraState.READY,
                availableCameraLens = availableCameraLens,
            )
            _cameraUiState.emit(newCameraUiState)
        }
    }

    fun startPreview(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: SurfaceProvider
    ) {
        Log.d(TAG, "starting preview")
        val currentCameraUiState = _cameraUiState.value

        val cameraSelector = cameraLensToSelector(currentCameraUiState.cameraLens)

        preview.setSurfaceProvider(surfaceProvider)

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e(TAG, "Usecase binding failed", e)
        }

        viewModelScope.launch {
            _cameraUiState.emit(_cameraUiState.value.copy(cameraState = CameraState.READY))
            _captureUiState.emit(CaptureState.CaptureReady)
        }
    }


    private fun cameraLensToSelector(@CameraSelector.LensFacing lensFacing: Int): CameraSelector =
        when (lensFacing) {
            CameraSelector.LENS_FACING_FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraSelector.LENS_FACING_BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            else -> throw IllegalArgumentException("Invalid lens facing type: $lensFacing")
        }

    fun flipCamera() {

        val newLensFacing = when(val currentLensFacing = _cameraUiState.value.cameraLens) {
            CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK
            CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT
            else -> throw java.lang.IllegalArgumentException("Invalid lens facing type: $currentLensFacing")
        }

        viewModelScope.launch {
            _cameraUiState.emit(_cameraUiState.value.copy(cameraLens = newLensFacing))
        }
    }

    fun capture() {
        TODO("Not yet implemented")
    }

    fun openGallery() {
        TODO("Not yet implemented")
    }

    fun onTapToFocus(focusMeteringAction: FocusMeteringAction) {
        Log.d(TAG, "onTapToFocus $focusMeteringAction")
        camera.cameraControl.startFocusAndMetering(focusMeteringAction)
    }

    fun onZoom(zoomMultiplier: Float) {
        val zoomState = camera.cameraInfo.zoomState.value
        zoomState?.let {
            val minZoomRatio = zoomState.minZoomRatio
            val maxZoomRatio = zoomState.maxZoomRatio

            val zoomRatio = zoomState.zoomRatio * zoomMultiplier

            Log.d(TAG, "onZoom $zoomRatio")
            camera.cameraControl.setZoomRatio(clamp(zoomRatio, minZoomRatio, maxZoomRatio))
        }
    }
}