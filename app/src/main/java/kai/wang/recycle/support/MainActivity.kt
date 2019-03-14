package kai.wang.recycle.support

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import kai.wang.recycle.support.lib.FlowLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycleView.layoutManager = FlowLayoutManager()
        recycleView.adapter = FlowLayoutAdapter(this@MainActivity)
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
                textView.textSize = context.resources.getDimension(R.dimen.sp_12)
                textView.background = context.createGradientDrawable(10f, colors[Random().nextInt(6)])
                return FlowLayoutHolder(textView)
            }

            override fun getItemCount(): Int {
                return 100
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
