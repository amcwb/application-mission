package com.starsflower.task_application

import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView


class Utils {
    companion object {
        fun setListViewHeightBasedOnChildren(listView: ListView) {
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
            params.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
            listView.layoutParams = params
            listView.requestLayout()
        }
    }
}