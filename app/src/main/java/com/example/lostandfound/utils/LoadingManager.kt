package com.example.lostandfound.utils

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.data.Resource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * LoadingManager - Centralized loading state management
 * Handles both dialog and inline loading indicators
 */
class LoadingManager(private val fragment: Fragment) {

    private var loaderDialog: LoaderDialog? = null
    private var inlineProgressBar: ProgressBar? = null

    init {
        loaderDialog = LoaderDialog(fragment.requireContext())
    }

    /**
     * Set inline progress bar for views that have their own loading indicator
     */
    fun setInlineProgressBar(progressBar: ProgressBar) {
        this.inlineProgressBar = progressBar
    }

    /**
     * Show dialog loader
     */
    fun showDialog(message: String = "Loading...") {
        loaderDialog?.show(message)
    }

    /**
     * Show inline loader
     */
    fun showInline() {
        inlineProgressBar?.visibility = View.VISIBLE
    }

    /**
     * Show both dialog and inline
     */
    fun showBoth(message: String = "Loading...") {
        showDialog(message)
        showInline()
    }

    /**
     * Hide dialog loader
     */
    fun hideDialog() {
        loaderDialog?.dismiss()
    }

    /**
     * Hide inline loader
     */
    fun hideInline() {
        inlineProgressBar?.visibility = View.GONE
    }

    /**
     * Hide both loaders
     */
    fun hideBoth() {
        hideDialog()
        hideInline()
    }

    /**
     * Update dialog message
     */
    fun updateMessage(message: String) {
        loaderDialog?.updateMessage(message)
    }

    /**
     * Automatically handle Resource states with dialog
     */
    fun <T> observeResourceWithDialog(
        lifecycleOwner: LifecycleOwner,
        stateFlow: StateFlow<Resource<T>>,
        loadingMessage: String = "Loading...",
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        onLoading: (() -> Unit)? = null
    ) {
        lifecycleOwner.lifecycleScope.launch {
            stateFlow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showDialog(loadingMessage)
                        onLoading?.invoke()
                    }
                    is Resource.Success -> {
                        hideDialog()
                        onSuccess(resource.data)
                    }
                    is Resource.Error -> {
                        hideDialog()
                        onError(resource.exception.message ?: "An error occurred")
                    }
                    Resource.None -> {
                        hideDialog()
                    }
                }
            }
        }
    }

    /**
     * Automatically handle Resource states with inline loader
     */
    fun <T> observeResourceWithInline(
        lifecycleOwner: LifecycleOwner,
        stateFlow: StateFlow<Resource<T>>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        onLoading: (() -> Unit)? = null
    ) {
        lifecycleOwner.lifecycleScope.launch {
            stateFlow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showInline()
                        onLoading?.invoke()
                    }
                    is Resource.Success -> {
                        hideInline()
                        onSuccess(resource.data)
                    }
                    is Resource.Error -> {
                        hideInline()
                        onError(resource.exception.message ?: "An error occurred")
                    }
                    Resource.None -> {
                        hideInline()
                    }
                }
            }
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        hideDialog()
        hideInline()
        loaderDialog = null
        inlineProgressBar = null
    }
}