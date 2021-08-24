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

    private var _user_id: MutableLiveData<Int> = MutableLiveData()
    val user_id: LiveData<Int> get() = _user_id

    fun setJWT(jwt: String) {
        _jwt.value = jwt
    }

    fun setURL(url: String) {
        _url.value = url
    }

    fun setUserID(userId: Int) {
        _user_id.value = userId
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