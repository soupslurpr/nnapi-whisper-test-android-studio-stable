package com.example.nnapiwhispertestandroidstudiostable.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File

class NnApiViewModel : ViewModel() {

    var options = Interpreter.Options()
    var nnApiDelegate: NnApiDelegate? = null
    var tfLite: Interpreter? = null
    var output by mutableStateOf("")
    var elapsed by mutableStateOf(0L)

    // Initialize interpreter with NNAPI delegate for Android Pie or above
    fun initialize(applicationContext: Context) {
        nnApiDelegate = NnApiDelegate()
        options.useNNAPI = true
        NnApiDelegate.Options().useNnapiCpu = false
        options.addDelegate(nnApiDelegate)

        val model = applicationContext.assets.open("models/whisper-tiny.tflite")
        val file = model.readBytes()

        val fileName = "whisper-tiny.tflite"
        applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(file)
        }

        val modelFile = File(applicationContext.filesDir,"whisper-tiny.tflite")

        // Initialize TFLite interpreter
        try {
            tfLite = Interpreter(modelFile, options)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun runInference() {
        try {
            val outputShape = tfLite?.getOutputTensor(0)
            val input = TensorBuffer.createFixedSize(intArrayOf(1, 80, 3000), DataType.FLOAT32)
            val output = TensorBuffer.createFixedSize(outputShape?.shape(), DataType.FLOAT32)

            val start = System.currentTimeMillis()
            tfLite?.run(input.buffer, output.buffer)
            elapsed = System.currentTimeMillis() - start
        } catch (e: RuntimeException) {
            throw RuntimeException(e)
        }
    }

    fun unload() {
        tfLite?.close()
        nnApiDelegate?.close()
    }
}