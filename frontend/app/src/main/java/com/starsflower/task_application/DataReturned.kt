package com.starsflower.task_application

import kotlinx.serialization.Serializable

@Serializable
data class Error(val error: String)

@Serializable
data class JWTResponse(val jwt: String)

@Serializable
data class Task(
    val task_id: Integer,
    val content: String,
    val author_id: Integer?,
    val assigned_users: Array<Integer>
)

@Serializable
data class TaskList(
    val tasks: Array<Task>
)