package caios.android.pictogram.analyze

import android.content.Context
import android.graphics.Bitmap
import caios.android.pictogram.data.Device
import caios.android.pictogram.data.Model
import caios.android.pictogram.data.PostureData
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp

abstract class Interpriter(
    private val context: Context,
    private val model: Model,
    private val device: Device
) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    abstract fun estimatePosture(bitmap: Bitmap): PostureData

    protected fun getInterpreter(): Interpreter {
        interpreter?.let { return it } ?: kotlin.run {
            interpreter = Interpreter(loadModel(), setUpOption())
            return interpreter!!
        }
    }

    private fun loadModel(): MappedByteBuffer {
        val modelName = when(model) {
            Model.MOVENET_LIGHTNING -> "movenet_lightning_v3.tflite"
            Model.MOVENET_THUNDER   -> "movenet_thunder_v3.tflite"
            Model.POSENET           -> "posenet.tflite"
        }

        val fileDescriptor = context.resources.assets.openFd(modelName)
        val inputStream = fileDescriptor.createInputStream()
        return inputStream.channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    private fun setUpOption(): Interpreter.Options {
        return Interpreter.Options().apply {
            setNumThreads(4)
            when (device) {
                Device.CPU   -> Unit
                Device.GPU   -> {
                    gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                }
                Device.NNAPI -> {
                    setUseNNAPI(true)
                }
            }
        }
    }

    //シグモイド関数
    protected fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    companion object {
        const val CPU_THREADS = 4
        const val IMAGE_MEAN = 64.0f
        const val IMAGE_STD = 64.0f

        fun getInstance(context: Context, model: Model, device: Device): Interpriter {
            return when (model) {
                Model.MOVENET_LIGHTNING -> MoveNetInterpriter(context, model, device)
                Model.MOVENET_THUNDER   -> MoveNetInterpriter(context, model, device)
                Model.POSENET           -> PoseNetInterpriter(context, model, device)
            }
        }
    }
}