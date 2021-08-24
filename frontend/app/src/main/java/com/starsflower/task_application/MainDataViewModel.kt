package com.starsflower.task_application

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainDataViewModel  : ViewModel() {
    // Stores data for requests
    private var _jwt: MutableLiveData<String> = MutableLiveData()
    val jwt: LiveData<String> get() = _jwt

    private var _url: MutableLiveData<String> = MutableLiveData()
    val url: LiveData<String> get() = _url

    fun setJWT(jwt: String) {
        this._jwt.value = jwt
    }

    fun setURL(url: String) {
        this._url.value = url
    }

    fun createURL(path: Array<String>): String {
        val builder = Uri.Builder()

        builder.scheme("http")
            .authority(this.url.value!!)

        path.forEach { it
            builder.appendPath(it)
        }

        return builder.build().toString()
    }
}