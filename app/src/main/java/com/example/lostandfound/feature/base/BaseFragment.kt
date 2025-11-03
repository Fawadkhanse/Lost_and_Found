package com.example.lostandfound.feature.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lostandfound.utils.LoaderDialog
import com.example.lostandfound.utils.LoadingManager

/**
 * Enhanced BaseFragment with integrated API error handling
 * All fragments can extend this to get automatic error dialog functionality
 */
abstract class BaseFragment : Fragment() {

    private var loaderDialog: LoaderDialog? = null
    lateinit var loadingManager: LoadingManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loaderDialog = LoaderDialog(requireContext())
    }

    // ============================================
    // Loading Dialog Methods
    // ============================================

    /**
     * Show loading dialog with default message
     */
    protected fun showLoading() {
        showLoading("Loading...")
    }

    /**
     * Show loading dialog with custom message
     */
    protected fun showLoading(message: String = "Loading...") {
        loaderDialog?.show(message)
    }

    /**
     * Update loading message
     */
    protected fun updateLoadingMessage(message: String) {
        loaderDialog?.updateMessage(message)
    }

    /**
     * Hide loading dialog
     */
    protected fun hideLoading() {
        loaderDialog?.dismiss()
    }

    /**
     * Check if loading dialog is showing
     */
    protected fun isLoadingShowing(): Boolean {
        return loaderDialog?.isShowing() ?: false
    }

    // ============================================
    // Navigation Methods
    // ============================================

    fun navigateTo(actionId: Int, bundle: Bundle? = null) {
        try {
            findNavController().navigate(actionId, bundle)
        } catch (e: Exception) {
            // Navigation action not found or already navigated
            e.printStackTrace()
        }
    }

    fun navigateTo(actionId: Int) {
        try {
            findNavController().navigate(actionId)
        } catch (e: Exception) {
            // Navigation action not found or already navigated
            e.printStackTrace()
        }
    }

    // ============================================
    // Error Handling Methods - NEW!
    // ============================================

    /**
     * Show API error dialog with automatic error type detection
     * This automatically handles all error types and shows appropriate dialog
     *
     * @param error The exception/error to display
     * @param onRetry Optional callback when user clicks retry (only shown for retryable errors)
     */
    protected fun showApiError(error: Throwable, onRetry: (() -> Unit)? = null) {
        hideLoading() // Always hide loading when showing error

        ApiErrorDialog.show(requireContext(), error) { retry ->
            if (retry) {
                onRetry?.invoke()
            }
        }
    }

    /**
     * Show network error dialog
     * Use this when you detect network is unavailable
     *
     * @param onRetry Callback when user clicks retry
     */
    protected fun showNetworkError(onRetry: (() -> Unit)? = null) {
        hideLoading()
        ApiErrorDialog.showNetworkError(requireContext(), onRetry)
    }

    /**
     * Show server error dialog
     * Use this for 500+ server errors
     *
     * @param onRetry Callback when user clicks retry
     */
    protected fun showServerError(onRetry: (() -> Unit)? = null) {
        hideLoading()
        ApiErrorDialog.showServerError(requireContext(), onRetry)
    }

    /**
     * Show authentication error dialog
     * Use this for 401/403 errors when user needs to login
     *
     * @param onLogin Callback to navigate to login screen
     */
    protected fun showAuthError(onLogin: (() -> Unit)? = null) {
        hideLoading()
        ApiErrorDialog.showAuthError(requireContext(), onLogin)
    }

    /**
     * Show timeout error dialog
     * Use this for timeout exceptions
     *
     * @param onRetry Callback when user clicks retry
     */
    protected fun showTimeoutError(onRetry: (() -> Unit)? = null) {
        hideLoading()
        ApiErrorDialog.showTimeoutError(requireContext(), onRetry)
    }

    /**
     * Show custom error dialog
     * Use this for custom error messages
     *
     * @param title Error title
     * @param message Error message
     * @param showRetry Whether to show retry button
     * @param onRetry Callback when user clicks retry
     */
    protected fun showCustomError(
        title: String = "Error",
        message: String,
        showRetry: Boolean = false,
        onRetry: (() -> Unit)? = null
    ) {
        hideLoading()
        ApiErrorDialog.showCustom(
            context = requireContext(),
            title = title,
            message = message,
            showRetry = showRetry
        ) { retry ->
            if (retry) {
                onRetry?.invoke()
            }
        }
    }

    // ============================================
    // Toast Methods (kept for simple messages)
    // ============================================

    /**
     * Show error toast - Use for simple, non-critical errors
     * For API errors, prefer showApiError()
     */
    protected fun showError(message: String?) {
        ApiErrorDialog.showCustom(
            context = requireContext(),
            title = "Error",
            isCancelable = true,
            message = message ?: "An unexpected error occurred.",
            showRetry = false // Only OK button, no Retry/Cancel

        )

    }

    /**
     * Show success toast
     */
    protected fun showSuccess(message: String) {

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

//        ApiErrorDialog.showCustom(
//            context = requireContext(),
//            title = "Success",
//            isCancelable = true,
//            message = message ?: "Succes",
//            showRetry = false
//
//        )
    }

    /**
     * Show info toast
     */
    protected fun showInfo(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//        ApiErrorDialog.showCustom(
//            context = requireContext(),
//            title = "Info",
//            isCancelable = true,
//            message = message ?: "Succes",
//            showRetry = false
//
//        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideLoading()
        loaderDialog = null
    }
}