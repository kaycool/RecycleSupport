package kai.wang.recycle.support

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kai.wang.recycle.support.lib.GridCenterManager
import kotlinx.android.synthetic.main.activity_recycle.*
import java.util.*

/**
 * @author kai.w
 * @des  $des
 */
class GridCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle)

        recycleView.layoutManager = GridCenterManager(this, 3)
//        recycleView.layoutManager = GridLayoutManager(this, 3)
        recycleView.adapter = FlowLayoutAdapter(this@GridCenterActivity)
        recycleView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.left = 10
                outRect.top = 10
            }
        })
    }


    companion object {

        class FlowLayoutAdapter(val context: Context) : RecyclerView.Adapter<FlowLayoutHolder>() {
            private val colors = mutableListOf(
                Color.RED, Color.CYAN
                , Color.BLUE, Color.GREEN, Color.YELLOW, Color.LTGRAY
            )
            private val texts = mutableListOf(
                "onCreateViewHolder"
                , "FlowLayoutAdapter"
                , "getItemCount"
                , "onBindViewHolder"
                , "FlowLayoutHolder"
                , "测试文本"
                , "假发"
                , "百度与google"
                , "生活与禽兽"
                , "爱情与生活"
                , "make more more money"
            )

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlowLayoutHolder {
                val textView = TextView(context)
                textView.setPadding(
                    context.resources.getDimensionPixelSize(R.dimen.dp_10)
                    , context.resources.getDimensionPixelSize(R.dimen.dp_5)
                    , context.resources.getDimensionPixelSize(R.dimen.dp_10)
                    , context.resources.getDimensionPixelSize(R.dimen.dp_5)
                )
                textView.setTextColor(Color.WHITE)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.sp_12))
                textView.background = context.createGradientDrawable(10f, colors[Random().nextInt(6)])
                textView.gravity = Gravity.CENTER
                return FlowLayoutHolder(textView)
            }

            override fun getItemCount(): Int {
                return 101
            }

            override fun onBindViewHolder(holder: FlowLayoutHolder, position: Int) {
                (holder.itemView as? TextView)?.apply {
                    this.text = texts[Random().nextInt(10)]
                }
            }

        }


        class FlowLayoutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        }

        fun Context.createGradientDrawable(radiusPx: Float, solidColor: Int) = GradientDrawable().apply {
            cornerRadius = radiusPx
            setColor(solidColor)
        }
    }

}