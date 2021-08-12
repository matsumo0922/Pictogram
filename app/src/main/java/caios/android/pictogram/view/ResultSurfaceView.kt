package caios.android.pictogram.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PixelFormat
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import caios.android.pictogram.R
import caios.android.pictogram.data.KeyPoint
import caios.android.pictogram.data.PostureData

@SuppressLint("ViewConstructor")
abstract class ResultSurfaceView(surfaceView: SurfaceView) : SurfaceView(surfaceView.context), SurfaceHolder.Callback {

    protected val surfaceHolder: SurfaceHolder = surfaceView.holder
    protected val paint = Paint()

    init {
        surfaceHolder.addCallback(this)
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
        surfaceView.setZOrderOnTop(true)
        setPaint()
    }

    abstract fun drawPosture(postureData: PostureData, bitmap: Bitmap, viewSize: Size, time: Long): List<KeyPoint>

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

    private fun setPaint() {
        paint.color = ContextCompat.getColor(context, R.color.colorAccentGreen)
        paint.strokeWidth = 12f
        paint.textSize = 34f
    }

}