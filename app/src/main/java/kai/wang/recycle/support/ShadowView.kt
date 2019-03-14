package kai.wang.recycle.support

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * @author kai.w
 * @des  $des
 */
class ShadowView : FrameLayout {
    val innerPaint = Paint()
    val outPaint = Paint()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setWillNotDraw(false)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2
        val radius = width.toFloat() / 2
        val radialGradient = RadialGradient(
            centerX,
            centerY + 50,
            radius,
            Color.parseColor("#0000E4BD"),
            Color.parseColor("#0F00E4BD"),
            Shader.TileMode.CLAMP
        )

        outPaint.shader = radialGradient
        canvas.drawCircle(centerX, centerY, radius, outPaint)

        innerPaint.color = Color.TRANSPARENT
        innerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        canvas.drawCircle(centerX, centerY, radius / 2, innerPaint)
    }


}