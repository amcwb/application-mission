package com.starsflower.task_application

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskDataViewModel: ViewModel() {
    // Stores data of current task
    private var _task_id: MutableLiveData<Int?> = MutableLiveData()
    val task_id: LiveData<Int?> get() = _task_id

    private var _due: MutableLiveData<Int?> = MutableLiveData()
    val due: LiveData<Int?> get() = _due

    private var _content: MutableLiveData<String?> = MutableLiveData()
    val content: LiveData<String?> get() = _content

    private var _author_id: MutableLiveData<Int?> = MutableLiveData()
    val author_id: LiveData<Int?> get() = _author_id

    private var _assigned_users: MutableLiveData<Array<Int>?> = MutableLiveData()
    val assigned_users: LiveData<Array<Int>?> get() = _assigned_users

    fun empty() {
        this.setTaskID(null)
        this.setDue(null)
        this.setContent(null)
        this.setAuthorID(null)
        this.setAssignedUsers(null)
    }

    fun setTaskID(task_id: Int?) {
        this._task_id.value = task_id
    }

    fun setDue(due: Int?) {
        this._due.value = due
    }

    fun setContent(content: String?) {
        this._content.value = content
    }

    fun setAuthorID(author_id: Int?) {
        this._author_id.value = author_id
    }

    fun setAssignedUsers(assigned_users: Array<Int>?) {
        this._assigned_users.value = assigned_users
    }
}