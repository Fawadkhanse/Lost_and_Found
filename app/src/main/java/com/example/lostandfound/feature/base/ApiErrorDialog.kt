package com.example.lostandfound.feature.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.lostandfound.R
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * ApiErrorDialog - Comprehensive error dialog system
 * Handles all types of API errors with user-friendly messages
 *
 * Usage:
 * ApiErrorDialog.show(context, exception) { retry ->
 *     if (retry) {
 *         // Retry action
 *     }
 * }
 */
object ApiErrorDialog {

    /**
     * Show error dialog with automatic error type detection
     */
    fun show(
        context: Context,
        error: Throwable,
        onAction: ((retry: Boolean) -> Unit)? = null
    ) {
        val errorInfo = parseError(error)
        showErrorDialog(
            context = context,
            errorInfo = errorInfo,
            onAction = onAction
        )
    }

    /**
     * Show error dialog with custom message
     */
    fun showCustom(
        context: Context,
        title: String = "Error",
        message: String,
        isCancelable: Boolean= false,
        showRetry: Boolean = true,

        onAction: ((retry: Boolean) -> Unit)? = null
    ) {
        val errorInfo = ErrorInfo(
            title = title,
            message = message,
            iconRes = R.drawable.ic_error,
            showRetry = showRetry,
            errorType = ErrorType.CUSTOM,
            isCancelable = isCancelable
        )
        showErrorDialog(
            context = context,
            errorInfo = errorInfo,
            onAction = onAction
        )
    }

    /**
     * Show network error dialog
     */
    fun showNetworkError(
        context: Context,
        onRetry: (() -> Unit)? = null
    ) {
        val errorInfo = ErrorInfo(
            title = "No Internet Connection",
            message = "Please check your internet connection and try again.",
            iconRes = R.drawable.ic_no_internet,
            showRetry = true,
            errorType = ErrorType.NETWORK
        )
        showErrorDialog(
            context = context,
            errorInfo = errorInfo,
            onAction = { retry ->
                if (retry) {
                    onRetry?.invoke()
                }
            }
        )
    }

    /**
     * Show server error dialog
     */
    fun showServerError(
        context: Context,
        onRetry: (() -> Unit)? = null
    ) {
        val errorInfo = ErrorInfo(
            title = "Server Error",
            message = "Something went wrong on our end. Please try again later.",
            iconRes = R.drawable.ic_error,
            showRetry = true,
            errorType = ErrorType.SERVER
        )
        showErrorDialog(
            context = context,
            errorInfo = errorInfo,
            onAction = { retry ->
                if (retry) {
                    onRetry?.invoke()
                }
            }
        )
    }

    /**
     * Show authentication error dialog
     */
    fun showAuthError(
        context: Context,
        onLogin: (() -> Unit)? = null
    ) {
        val errorInfo = ErrorInfo(
            title = "Authentication Required",
            message = "Your session has expired. Please log in again.",
            iconRes = R.drawable.ic_account,
            showRetry = false,
            primaryButtonText = "Login",
            errorType = ErrorType.AUTH
        )
        showErrorDialog(
            context = context,
            errorInfo = errorInfo,
            onAction = { _ ->
                onLogin?.invoke()
            }
        )
    }

    /**
     * Show timeout error dialog
     */
    fun showTimeoutError(
        context: Context,
        onRetry: (() -> Unit)? = null
    ) {
        val errorInfo = ErrorInfo(
            title = "Request Timeout",
            message = "The request took too long. Please check your connection and try again.",
            iconRes = R.drawable.ic_error,
            showRetry = true,
            errorType = ErrorType.TIMEOUT
        )
        showErrorDialog(
            context = context,
            errorInfo = errorInfo,
            onAction = { retry ->
                if (retry) {
                    onRetry?.invoke()
                }
            }
        )
    }

    /**
     * Parse error and extract relevant information
     */
    private fun parseError(error: Throwable): ErrorInfo {
        return when (error) {
            // Network errors
            is UnknownHostException -> ErrorInfo(
                title = "No Internet Connection",
                message = "Please check your internet connection and try again.",
                iconRes = R.drawable.ic_no_internet,
                showRetry = true,
                errorType = ErrorType.NETWORK
            )

            is SocketTimeoutException -> ErrorInfo(
                title = "Request Timeout",
                message = "The request took too long. Please check your connection and try again.",
                iconRes = R.drawable.ic_error,
                showRetry = true,
                errorType = ErrorType.TIMEOUT
            )

            is IOException -> ErrorInfo(
                title = "Connection Error",
                message = "Failed to connect to the server. Please try again.",
                iconRes = R.drawable.ic_no_internet,
                showRetry = true,
                errorType = ErrorType.NETWORK
            )

            // HTTP errors
            is HttpException -> {
                when (error.code()) {
                    400 -> ErrorInfo(
                        title = "Invalid Request",
                        message = parseHttpErrorBody(error) ?: "The request was invalid. Please check your input.",
                        iconRes = R.drawable.ic_error,
                        showRetry = false,
                        errorType = ErrorType.BAD_REQUEST
                    )

                    401, 403 -> ErrorInfo(
                        title = "Authentication Required",
                        message = "Your session has expired. Please log in again.",
                        iconRes = R.drawable.ic_account,
                        showRetry = false,
                        primaryButtonText = "Login",
                        errorType = ErrorType.AUTH
                    )

                    404 -> ErrorInfo(
                        title = "Not Found",
                        message = "The requested resource was not found.",
                        iconRes = R.drawable.ic_error,
                        showRetry = false,
                        errorType = ErrorType.NOT_FOUND
                    )

                    409 -> ErrorInfo(
                        title = "Conflict",
                        message = parseHttpErrorBody(error) ?: "A conflict occurred. The resource may already exist.",
                        iconRes = R.drawable.ic_error,
                        showRetry = false,
                        errorType = ErrorType.CONFLICT
                    )

                    422 -> ErrorInfo(
                        title = "Validation Error",
                        message = parseHttpErrorBody(error) ?: "Please check your input and try again.",
                        iconRes = R.drawable.ic_error,
                        showRetry = false,
                        errorType = ErrorType.VALIDATION
                    )

                    500, 502, 503, 504 -> ErrorInfo(
                        title = "Server Error",
                        message = "Something went wrong on our end. Please try again later.",
                        iconRes = R.drawable.ic_error,
                        showRetry = true,
                        errorType = ErrorType.SERVER
                    )

                    else -> ErrorInfo(
                        title = "Error ${error.code()}",
                        message = parseHttpErrorBody(error) ?: "An error occurred. Please try again.",
                        iconRes = R.drawable.ic_error,
                        showRetry = true,
                        errorType = ErrorType.HTTP
                    )
                }
            }

            // JSON parsing errors
            is JsonSyntaxException -> ErrorInfo(
                title = "Data Error",
                message = "Failed to process server response. Please try again.",
                iconRes = R.drawable.ic_error,
                showRetry = true,
                errorType = ErrorType.PARSE
            )

            // Generic errors
            else -> ErrorInfo(
                title = "Error",
                message = error.message ?: "An unexpected error occurred. Please try again.",
                iconRes = R.drawable.ic_error,
                showRetry = true,
                errorType = ErrorType.UNKNOWN
            )
        }
    }

    /**
     * Parse HTTP error body to extract error message
     */
    private fun parseHttpErrorBody(error: HttpException): String? {
        return try {
            val errorBody = error.response()?.errorBody()?.string()

            // Try to parse common error formats
            when {
                errorBody == null -> null

                // Format: {"error": "message"}
                errorBody.contains("\"error\"") -> {
                    val start = errorBody.indexOf("\"error\"") + 9
                    val end = errorBody.indexOf("\"", start + 2)
                    if (end > start) errorBody.substring(start + 2, end) else null
                }

                // Format: {"message": "message"}
                errorBody.contains("\"message\"") -> {
                    val start = errorBody.indexOf("\"message\"") + 11
                    val end = errorBody.indexOf("\"", start + 2)
                    if (end > start) errorBody.substring(start + 2, end) else null
                }

                // Format: {"detail": "message"}
                errorBody.contains("\"detail\"") -> {
                    val start = errorBody.indexOf("\"detail\"") + 10
                    val end = errorBody.indexOf("\"", start + 2)
                    if (end > start) errorBody.substring(start + 2, end) else null
                }

                // Return first 150 characters if no specific format found
                else -> errorBody.take(150)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Show the error dialog with custom layout
     */
    private fun showErrorDialog(
        context: Context,
        errorInfo: ErrorInfo,
        onAction: ((retry: Boolean) -> Unit)?
    ) {
        val dialog = Dialog(context)
        dialog.setCancelable(errorInfo.isCancelable)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_api_error, null)
        dialog.setContentView(view)

        // Make dialog background transparent
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // ✅ Set dialog width to match parent
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set up views
        val ivIcon = view.findViewById<ImageView>(R.id.ivErrorIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvErrorTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvErrorMessage)
        val btnPrimary = view.findViewById<Button>(R.id.btnPrimary)
        val btnSecondary = view.findViewById<Button>(R.id.btnSecondary)

        // Set content
        ivIcon.setImageResource(errorInfo.iconRes)
        tvTitle.text = errorInfo.title
        tvMessage.text = errorInfo.message

        // Configure buttons
        if (errorInfo.showRetry) {
            // Two-button layout
            btnPrimary.text = errorInfo.primaryButtonText
            btnSecondary.visibility = View.VISIBLE
        } else {
            // Single-button layout
            btnPrimary.text = errorInfo.primaryButtonText
            btnSecondary.visibility = View.GONE
            btnPrimary.layoutParams = (btnPrimary.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 0f
                width = LinearLayout.LayoutParams.MATCH_PARENT
            }
            btnPrimary.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    /**
     * Error information data class
     */
    private data class ErrorInfo(
        val title: String,
        val message: String,
        val iconRes: Int,
        val showRetry: Boolean,
        val isCancelable: Boolean = false,
        val primaryButtonText: String = if (showRetry) "Retry" else "OK",
        val secondaryButtonText: String = "Cancel",
        val errorType: ErrorType
    )

    /**
     * Error types enum
     */
    enum class ErrorType {
        NETWORK,
        TIMEOUT,
        SERVER,
        AUTH,
        BAD_REQUEST,
        NOT_FOUND,
        CONFLICT,
        VALIDATION,
        HTTP,
        PARSE,
        CUSTOM,
        UNKNOWN
    }
}