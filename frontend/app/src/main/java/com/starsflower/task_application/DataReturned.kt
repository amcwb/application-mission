package com.starsflower.task_application

import kotlinx.serialization.Serializable

@Serializable
data class Error(val error: String)

@Serializable
data class JWTResponse(val jwt: String)

@Serializable
data class TaskID(
    val task_id: Int
)

@Serializable
data class Task(
    val task_id: Int,
    val content: String,
    val author_id: Int?,
    val assigned_users: Array<Int>
)

@Serializable
data class TaskList(
    val tasks: Array<Task>
)