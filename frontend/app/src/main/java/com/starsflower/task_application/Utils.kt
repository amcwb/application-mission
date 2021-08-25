package com.starsflower.task_application

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class Utils {

    companion object {
        private val client = OkHttpClient.Builder()
            .connectTimeout(200, TimeUnit.MILLISECONDS)
            .build()

        fun makeSafeRequest(request: Request, view: View, result: (Response) -> Any?): Boolean {
            try {
                this.client.newCall(request).execute().use {
                    if (it.body == null) {
                        Snackbar.make(view, "A serious error occurred", Snackbar.LENGTH_SHORT)
                            .show()
                        return false
                    } else {
                        val res = result(it)
                        if (res is Boolean) {
                            return res
                        }

                        return true
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                Snackbar.make(view, "The server could not be reached", Snackbar.LENGTH_SHORT)
                    .show()

                return false
            }
        }
        
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