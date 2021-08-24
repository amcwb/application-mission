package com.starsflower.task_application

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import kotlin.math.roundToInt


class Utils {
    companion object {
        fun setListViewHeightBasedOnChildren(listView: ListView, max30: Boolean = false) {
            val listAdapter: ListAdapter = listView.adapter
                ?: // pre-condition
                return

            var totalHeight = 0
            for (i in 0 until listAdapter.count) {
                val listItem: View = listAdapter.getView(i, null, listView)
                listItem.measure(0, 0)
                totalHeight += listItem.measuredHeight
            }

            val params: ViewGroup.LayoutParams = listView.layoutParams
            var calculatedHeight = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
            if (max30) {
                val maxHeight = Resources.getSystem().displayMetrics.heightPixels * 0.3
                if (calculatedHeight > maxHeight) {
                    calculatedHeight = maxHeight.roundToInt()
                }
            }
            params.height = calculatedHeight
            listView.layoutParams = params
            listView.requestLayout()
        }
    }
}