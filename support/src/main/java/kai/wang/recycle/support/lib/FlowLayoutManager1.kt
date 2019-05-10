package kai.wang.recycle.support.lib

import androidx.recyclerview.widget.RecyclerView

/**
 * @author kai.w
 * @des  $des
 */
class FlowLayoutManager1 : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        if (itemCount == 0) recycler?.also { detachAndScrapAttachedViews(it) }
        if (childCount == 0 && state?.isPreLayout == true) return
        recycler?.also { detachAndScrapAttachedViews(it) }






    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        return super.scrollVerticallyBy(dy, recycler, state)
    }


}