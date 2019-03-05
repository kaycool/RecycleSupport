package kai.wang.recycle.support.lib

import androidx.recyclerview.widget.RecyclerView

/**
 * @author kai.w
 * @des  $des
 */
class FlowLayoutManager: RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

}