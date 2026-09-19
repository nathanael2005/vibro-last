package com.nate.app.data.api

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ApiResult<Nothing>()
}

fun <T> ApiResult<T>.getOrEmpty(default: T): T = when (this) {
    is ApiResult.Success -> data
    is ApiResult.Error -> default
}

fun <T> ApiResult<T>.errorMessageOrNull(): String? = when (this) {
    is ApiResult.Error -> message
    is ApiResult.Success -> null
}
