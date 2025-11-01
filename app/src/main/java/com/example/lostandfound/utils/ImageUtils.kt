package com.example.lostandfound.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for image handling operations
 * Provides image compression, rotation correction, and file conversion
 */
object ImageUtils {

    /**
     * Compress image to reduce file size
     * @param file Original image file
     * @param maxWidth Maximum width (default 1024px)
     * @param maxHeight Maximum height (default 1024px)
     * @param quality JPEG quality 0-100 (default 80)
     * @return Compressed file or original if compression fails
     */
    fun compressImage(
        file: File,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File {
        return try {
            // Decode image with inJustDecodeBounds to get dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

            // Decode with inSampleSize
            options.inJustDecodeBounds = false
            var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

            // Correct image orientation
            bitmap = correctImageOrientation(file.absolutePath, bitmap)

            // Scale bitmap if still too large
            bitmap = scaleBitmapIfNeeded(bitmap, maxWidth, maxHeight)

            // Save compressed bitmap
            val compressedFile = File(
                file.parent,
                "compressed_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            bitmap.recycle()
            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            file // Return original if compression fails
        }
    }

    /**
     * Calculate sample size for bitmap loading
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Correct image orientation based on EXIF data
     */
    private fun correctImageOrientation(imagePath: String, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            if (!matrix.isIdentity) {
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    true
                )
                bitmap.recycle()
                rotatedBitmap
            } else {
                bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * Scale bitmap if dimensions exceed maximum
     */
    private fun scaleBitmapIfNeeded(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scale = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )

        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            scaledWidth,
            scaledHeight,
            true
        )

        bitmap.recycle()
        return scaledBitmap
    }

    /**
     * Create file from URI with compression
     */
    fun createCompressedFileFromUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): File? {
        return try {
            // First create temporary file
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")

            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Then compress it
            val compressedFile = compressImage(tempFile, maxWidth, maxHeight, quality)

            // Delete temp file if different from compressed file
            if (tempFile != compressedFile) {
                tempFile.delete()
            }

            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get file size in KB
     */
    fun getFileSizeInKB(file: File): Long {
        return file.length() / 1024
    }

    /**
     * Get file size in MB
     */
    fun getFileSizeInMB(file: File): Double {
        return file.length() / (1024.0 * 1024.0)
    }

    /**
     * Format file size for display
     */
    fun formatFileSize(file: File): String {
        val sizeInKB = getFileSizeInKB(file)
        return when {
            sizeInKB < 1024 -> "$sizeInKB KB"
            else -> String.format("%.2f MB", getFileSizeInMB(file))
        }
    }

    /**
     * Delete temporary files in cache directory
     */
    fun cleanupTempFiles(context: Context) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("temp_") || file.name.startsWith("compressed_")) {
                    val fileAge = System.currentTimeMillis() - file.lastModified()
                    // Delete files older than 1 hour
                    if (fileAge > 3600000) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Batch compress multiple images
     */
    fun compressImages(
        files: List<File>,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): List<File> {
        val compressedFiles = mutableListOf<File>()

        files.forEachIndexed { index, file ->
            val compressed = compressImage(file, maxWidth, maxHeight, quality)
            compressedFiles.add(compressed)
            onProgress?.invoke(index + 1, files.size)
        }

        return compressedFiles
    }

    /**
     * Check if image needs compression based on size threshold
     */
    fun needsCompression(file: File, maxSizeKB: Long = 500): Boolean {
        return getFileSizeInKB(file) > maxSizeKB
    }

    /**
     * Create thumbnail bitmap
     */
    fun createThumbnail(
        file: File,
        thumbnailSize: Int = 200
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, thumbnailSize, thumbnailSize)
            options.inJustDecodeBounds = false

            var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            bitmap = correctImageOrientation(file.absolutePath, bitmap)
            bitmap = scaleBitmapIfNeeded(bitmap, thumbnailSize, thumbnailSize)

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}