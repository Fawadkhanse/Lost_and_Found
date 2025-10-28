package com.example.lostandfound.feature.base

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lostandfound.utils.LoaderDialog
import com.example.lostandfound.utils.LoadingManager

/**
 * BaseFragment with generic loader support
 * All fragments can extend this to get automatic loader functionality
 */
abstract class BaseFragment : Fragment() {

    private var loaderDialog: LoaderDialog? = null
     lateinit var loadingManager: LoadingManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loaderDialog = LoaderDialog(requireContext())
    }

    /**
     * Show loading dialog with default message
     */
    protected fun showLoading() {
        showLoading("Loading...")
    }

    /**
     * Show loading dialog with custom message
     */
    protected fun showLoading(message: String="Loading...") {
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

    /**
     * Show error toast
     */
    protected fun showError(message: String?) {
        Toast.makeText(
            requireContext(),
            message ?: "An error occurred",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Show success toast
     */
    protected fun showSuccess(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Show info toast
     */
    protected fun showInfo(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideLoading()
        loaderDialog = null
    }
}