package kai.wang.recycle.support.lib

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author kai.w
 * @des  $des
 */
class GridCenterManager(val context: Context, val spanCount: Int = 0) : RecyclerView.LayoutManager() {
    private var mVerticalOffset: Int = 0//竖直偏移量 每次换行时，要根据这个offset判断
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
        mLastVisiRow = itemCount / spanCount + if (itemCount % spanCount > 0) {
            1
        } else {
            0
        }
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
                for (i in childCount - 1 downTo 0) {
                    val spanIndex = i % spanCount
                    getChildAt(i)?.apply {
                        if (dy > 0) {//需要回收当前屏幕，上越界的View
                            if (getDecoratedBottom(this) - dy < topOffset) {
                                removeAndRecycleView(this, recycler!!)
                                if (spanIndex == 1) mFirstVisiRow++
                            }
                        } else if (dy < 0) {//回收当前屏幕，下越界的View
                            if (getDecoratedTop(this) - dy > this@GridCenterManager.height - this@GridCenterManager.paddingBottom) {
                                removeAndRecycleView(this, recycler!!)
                                if (spanIndex == 1) mLastVisiRow--
                            }
                        }
                    }
                }
            }
        }

        Log.d("fill", "mFirstVisiRow=$mFirstVisiRow and mLastVisiRow=$mLastVisiRow")

        var leftOffset = paddingLeft
        var lineHeight = 0

        //布局子View阶段
        with(dy >= 0) {
            if (this) {
                var minPos = mFirstVisiRow * spanCount
                var mLastVisiPos = itemCount - 1
                Log.d("fill", "childCount=$childCount")
                if (childCount > 0) {
                    val lastView = getChildAt(childCount - 1)
                    minPos = getPosition(lastView!!) + 1//从最后一个View+1开始吧
                    topOffset = getDecoratedTop(lastView)
                    leftOffset = getDecoratedRight(lastView)
                    lineHeight = Math.max(lineHeight, getDecoratedMeasurementVertical(lastView))
                }
                Log.d("fill", "minPos=$minPos and mLastVisiPos=$mLastVisiPos")
                for (i in minPos..mLastVisiPos) {
                    val child = recycler!!.getViewForPosition(i)
                    addView(child)
                    calculateItemDecorationsForChild(child, mDectorRect)
                    //测量子view阶段
                    measureChild(child)

                    val spanIndex = i % spanCount + 1
                    if (spanIndex == 1) {//换行
                        topOffset += lineHeight
                        leftOffset = 0
                        lineHeight = 0

                        if (mLastVisiPos - i < spanCount) {//最后一行居中显示
                            val spaceWidth =
                                ((width - paddingLeft - paddingRight) / spanCount) * (spanCount - (mLastVisiPos - i) - 1)
                            leftOffset += spaceWidth / 2
                        }
                    }

                    if (topOffset - dy > height - paddingBottom) {
                        removeAndRecycleView(child, recycler)
                        if (spanIndex == 1) mLastVisiPos = i - 1
                    } else {
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
                            topOffset + mVerticalOffset,
                            leftOffset + getDecoratedMeasurementHorizontal(child),
                            topOffset + getDecoratedMeasurementVertical(child) + mVerticalOffset
                        )
                        mItemRects.put(i, rect)
                        leftOffset += getDecoratedMeasurementHorizontal(child)
                        lineHeight = Math.max(lineHeight, getDecoratedMeasurementVertical(child))
                    }
                }
                //添加完后，判断是否已经没有更多的ItemView，并且此时屏幕仍有空白，则需要修正dy
                val lastChild = getChildAt(childCount - 1)
                if (getPosition(lastChild!!) == itemCount - 1) {
                    val gap = height - paddingBottom - getDecoratedBottom(lastChild)
                    if (gap > 0) {
                        dy -= gap
                    }
                }
            } else {
                var maxPos = itemCount - 1
                val mFirstVisiPos = 0
                if (childCount > 0) {
                    val firstView = getChildAt(0)
                    maxPos = getPosition(firstView!!) - 1
                }
                Log.d("fill", "maxPos=$maxPos and mFirstVisiPos=$mFirstVisiPos and childCount=$childCount")

                for (i in maxPos downTo mFirstVisiPos) {
                    val rect = mItemRects.get(i)
                    if (rect.bottom - mVerticalOffset - dy < paddingTop) {
                        break
                    } else {
                        val child = recycler!!.getViewForPosition(i)
                        addView(child, 0)//将View添加至RecyclerView中，childIndex为1，但是View的位置还是由layout的位置决定
                        calculateItemDecorationsForChild(child, mDectorRect)
                        measureChild(child)
                        layoutDecoratedWithMargins(
                            child,
                            rect.left,
                            rect.top - mVerticalOffset,
                            rect.right,
                            rect.bottom - mVerticalOffset
                        )
                    }
                }
            }
        }
        return dy
    }


    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        //位移0、没有子View 当然不移动
        if (dy == 0 || childCount == 0) {
            return 0
        }

        var realOffset = dy//实际滑动的距离， 可能会在边界处被修复
        //边界修复代码
        if (mVerticalOffset + realOffset < 0) {//上边界
            realOffset = -mVerticalOffset
        } else if (realOffset > 0) {//下边界
            //利用最后一个子View比较修正
            val lastChild = getChildAt(childCount - 1)
            if (getPosition(lastChild!!) == itemCount - 1) {
                val gap = height - paddingBottom - getDecoratedBottom(lastChild)
                realOffset = when {
                    gap > 0 -> -gap
                    gap == 0 -> 0
                    else -> Math.min(realOffset, -gap)
                }
            }
        }

        realOffset = fill(recycler, state, realOffset)//先填充，再位移。

        mVerticalOffset += realOffset//累加实际滑动距离

        offsetChildrenVertical(-realOffset)//滑动

        return realOffset
    }

    private fun measureChild(view: View) {
        val spanLimitSize = (width.toFloat() - paddingLeft - paddingRight) / spanCount
        val lp = view.layoutParams as RecyclerView.LayoutParams
        val verticalInsets = mDectorRect.top + mDectorRect.bottom + lp.topMargin + lp.bottomMargin
        val horizontalInsets = mDectorRect.left + mDectorRect.right + lp.leftMargin + lp.rightMargin
        val wSpec = View.MeasureSpec.makeMeasureSpec(
            spanLimitSize.toInt() - horizontalInsets,
            View.MeasureSpec.EXACTLY
        )
        val hSpec = RecyclerView.LayoutManager.getChildMeasureSpec(
            height, View.MeasureSpec.AT_MOST,
            verticalInsets, lp.height, false
        )
        view.measure(wSpec, hSpec)
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