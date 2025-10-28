package com.example.lostandfound.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import com.example.lostandfound.databinding.DialogLoaderBinding


/**
 * Generic Loader Dialog
 * Can be used across the entire app for loading states
 *
 * Usage:
 * val loader = LoaderDialog(requireContext())
 * loader.show("Loading...")
 * loader.dismiss()
 */
class LoaderDialog(context: Context) {

    private val dialog: Dialog = Dialog(context)
    private val binding: DialogLoaderBinding

    init {
        binding = DialogLoaderBinding.inflate(LayoutInflater.from(context))
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    /**
     * Show loader with custom message
     */
    fun show(message: String = "Loading...") {
        if (!dialog.isShowing) {
            binding.tvMessage.text = message
            dialog.show()
        }
    }

    /**
     * Update loader message while showing
     */
    fun updateMessage(message: String) {
        binding.tvMessage.text = message
    }

    /**
     * Dismiss loader
     */
    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    /**
     * Check if loader is showing
     */
    fun isShowing(): Boolean = dialog.isShowing
}