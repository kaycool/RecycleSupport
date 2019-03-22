package kai.wang.recycle.support.lib

import android.content.Context
import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author kai.w
 * @des  $des
 */
class GridCenterManager(val context: Context, val spanCount: Int = 0) : RecyclerView.LayoutManager() {
    private val mItemRects: SparseArray<Rect> = SparseArray()//key 是View的position，保存View的bounds 和 显示标志，
    private var mFirstVisiRow: Int = 0//屏幕可见的第一个View的Position
    private var mLastVisiRow: Int = 0//屏幕可见的最后一个View的Position
    private var mParentWidth = 0
    private val mDectorRect = Rect()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)

        if (itemCount == 0) {
            return
        }

        if (childCount == 0 && state.isPreLayout) {
            return
        }

        detachAndScrapAttachedViews(recycler)
        mFirstVisiRow = 0
        mLastVisiRow = itemCount

        //初始化时调用 填充childView
        fill(recycler, state)
    }


    /**
     * 初始化时调用 填充childView
     *
     * @param recycler
     * @param state
     */
    private fun fill(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        fill(recycler, state, 0)
    }

    /**
     * 填充childView的核心方法,应该先填充，再移动。
     * 在填充时，预先计算dy的在内，如果View越界，回收掉。
     * 一般情况是返回dy，如果出现View数量不足，则返回修正后的dy.
     *
     * @param recycler
     * @param state
     * @param dy       RecyclerView给我们的位移量,+,显示底端， -，显示头部
     * @return 修正以后真正的dy（可能剩余空间不够移动那么多了 所以return <|dy|）
     */
    private fun fill(recycler: RecyclerView.Recycler?, state: RecyclerView.State?, dy: Int): Int {
        var dy = dy

        var topOffset = paddingTop

        //回收越界子view
        with(childCount > 0) {
            if (this) {
                for (i in childCount - 1 downTo 0 step spanCount) {
                    val child = getChildAt(i)

                    if (dy > 0) {//需要回收当前屏幕，上越界的View
                        if (getDecoratedBottom(child!!) - dy < topOffset) {
                            for (index in i downTo i - spanCount) {
                                val removeChild = getChildAt(index)
                                removeAndRecycleView(removeChild!!, recycler!!)
                            }
                            mFirstVisiRow++
                            continue
                        }
                    } else if (dy < 0) {//回收当前屏幕，下越界的View
                        if (getDecoratedTop(child!!) - dy > height - paddingBottom) {
                            for (index in i downTo i - spanCount) {
                                val removeChild = getChildAt(index)
                                removeAndRecycleView(removeChild!!, recycler!!)
                            }
                            mLastVisiRow--
                            continue
                        }
                    }
                }
            }
        }

        var leftOffset = paddingLeft
        var lineHeight = 0

        //布局子View阶段
        with(dy >= 0) {
            if (this) {
                var minPos = mFirstVisiRow * spanCount
                val mLastVisiPos = itemCount - 1
                if (childCount > 0) {
                    val lastView = getChildAt(childCount - 1)
                    minPos = getPosition(lastView!!) + 1//从最后一个View+1开始吧
                    topOffset = getDecoratedTop(lastView)
                    leftOffset = getDecoratedRight(lastView)
                    lineHeight = Math.max(lineHeight, getDecoratedMeasurementVertical(lastView))
                }

                for (i in minPos..mLastVisiPos) {
                    val child = recycler!!.getViewForPosition(i)
                    addView(child)
                    calculateItemDecorationsForChild(child, mDectorRect)
                    //测量子view阶段
                    measureChildWithMargins(child, 0, 0)

                    val spanIndex = i % spanCount + 1


                    if (spanIndex == 1) {//换行
                        topOffset += lineHeight
                        leftOffset = 0
                        lineHeight = 0

                        if (mLastVisiPos - i < spanCount) {//最后一行居中显示
                            val spaceWidth = (mParentWidth / spanCount) * (spanCount - (mLastVisiPos - i) - 1)
                            leftOffset += spaceWidth / 2
                        }
                    }

                    layoutDecoratedWithMargins(
                        child,
                        leftOffset,
                        topOffset,
                        leftOffset + getDecoratedMeasurementHorizontal(child),
                        topOffset + getDecoratedMeasurementVertical(child)
                    )

                    //保存Rect供逆序layout用
                    val rect = Rect(
                        leftOffset,
                        topOffset,
                        leftOffset + getDecoratedMeasurementHorizontal(child),
                        topOffset + getDecoratedMeasurementVertical(child)
                    )
                    mItemRects.put(i, rect)
                    leftOffset += getDecoratedMeasurementHorizontal(child)
                    lineHeight = Math.max(lineHeight, getDecoratedMeasurementVertical(child))
                }


            } else {

            }

        }

        return dy
    }


    private fun measureChild(view: View, maxHeight: Int) {
        val spanLimitSize = width.toFloat() / spanCount
        val lp = view.layoutParams as RecyclerView.LayoutParams
        val verticalInsets = mDectorRect.top + mDectorRect.bottom + lp.topMargin + lp.bottomMargin
        val horizontalInsets = mDectorRect.left + mDectorRect.right + lp.leftMargin + lp.rightMargin
        val wSpec = View.MeasureSpec.makeMeasureSpec(
            spanLimitSize.toInt() - horizontalInsets,
            View.MeasureSpec.EXACTLY
        )
        val hSpec = RecyclerView.LayoutManager.getChildMeasureSpec(
            maxHeight, View.MeasureSpec.EXACTLY,
            verticalInsets, lp.height, false
        )




        view.measure(wSpec, hSpec)
    }


    private fun measureChildrenWithMaxWidth(child: View, parentUseWidth: Int, parentUseHeight: Int) {
        var widthUsed = parentUseWidth
        var heightUsed = parentUseHeight
        (child.layoutParams  as? RecyclerView.LayoutParams)?.also {
            val horizontalInsets = getDecoratedMeasurementHorizontal(child)
            val verticalInsets = getDecoratedMeasurementVertical(child)

            widthUsed += horizontalInsets
            heightUsed += verticalInsets

            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                mParentWidth - parentUseWidth - paddingLeft - paddingRight - it.leftMargin - it.rightMargin,
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(it.height, View.MeasureSpec.AT_MOST)
            child.measure(widthSpec, heightSpec)
        }
    }


    /**
     * 获取某个childView在水平方向所占的空间
     *
     * @param view
     * @return
     */
    private fun getDecoratedMeasurementHorizontal(view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return (getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin)
    }

    /**
     * 获取某个childView在竖直方向所占的空间
     *
     * @param view
     * @return
     */
    private fun getDecoratedMeasurementVertical(view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return (getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin)
    }


    fun getSelfSpanCount() = spanCount

}