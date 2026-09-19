package com.nate.core.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0
        var currentDelay = initialDelayMs

        while (tryCount <= maxRetries) {
            try {
                response = null
                exception = null
                if (tryCount > 0) {
                    Thread.sleep(currentDelay)
                    currentDelay *= 2 // Exponential backoff: 1s -> 2s -> 4s
                }
                response = chain.proceed(request)
                // If response is successful or it is a client error other than timeout, return immediately.
                if (response.isSuccessful || (response.code != 408 && response.code < 500)) {
                    return response
                }
                
                // Close the response body if we have more retries left to avoid resource leaks.
                if (tryCount < maxRetries) {
                    response.close()
                }
            } catch (e: IOException) {
                exception = e
            }
            tryCount++
        }

        return response ?: throw exception ?: IOException("Failed to execute request after $maxRetries retries")
    }
}
