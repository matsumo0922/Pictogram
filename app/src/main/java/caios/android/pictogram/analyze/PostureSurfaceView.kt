package caios.android.pictogram.analyze

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.view.SurfaceHolder
import android.view.SurfaceView

@SuppressLint("ViewConstructor")
class PostureSurfaceView(surfaceView: SurfaceView): SurfaceView(surfaceView.context), SurfaceHolder.Callback {

    private var surfaceHolder = surfaceView.holder

    init {
        surfaceHolder.addCallback(this)
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
        surfaceView.setZOrderOnTop(true)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

    fun drawPosture(postureData: PostureData, bitmap: Bitmap) {
        val canvas: Canvas? = surfaceHolder.lockCanvas()

        canvas?.drawColor(0, PorterDuff.Mode.CLEAR)
        canvas?.drawBitmap(bitmap, 0f, 0f, null)

        surfaceHolder.unlockCanvasAndPost(canvas ?: return)
    }
}