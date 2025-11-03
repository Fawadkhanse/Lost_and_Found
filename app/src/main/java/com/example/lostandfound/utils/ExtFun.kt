package com.example.lostandfound.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import com.example.lostandfound.MainActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

@SuppressLint("HardwareIds")
fun Context.getDeviceIMEI(): String? {
    try {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    } catch (e: Exception) {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (telephonyManager != null) {
                return try {
                    telephonyManager.imei
                } catch (exception: java.lang.Exception) {
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                }
            }
        } else {
            if (telephonyManager != null) {
                try {
                    telephonyManager.imei
                } catch (exception: java.lang.Exception) {
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                }
            }
        }
    }
    return ""
}





/*fun <T> encryptRequest(request: T): EncryptedRequestModel {
    val text = CryptoUtil.encryptAES(Gson().toJson(request), AE_SK)
    return EncryptedRequestModel(text?.trim()?.replace("\n", "").toString())
}

fun decryptResponse(response: EncryptedRequestModel?): String {
    return CryptoUtil.decryptAES(
        response?.Text?.trim()?.replace("\n", "") ?: "",
        AE_SK
    )
}*/

/*fun <T> encryptRequest(request: T): EncryptedRequestModel {
    val gson = GsonBuilder().disableHtmlEscaping().create()
    val text = CryptoUtil.encryptAES(gson.toJson(request), AE_SK)
    return EncryptedRequestModel(text ?: "")
}*/




inline fun <reified T> String.toPojo(): T {
    return Gson().fromJson(this, T::class.java)
}

inline fun <reified T> T.toJson(): String {
    return Gson().toJson(this)
}

fun String.toJsonObject(): JsonObject {
    val gson = Gson()
    return gson.fromJson(this, JsonObject::class.java)
}

fun Any.checkIfArray(): Boolean {
    return this is Array<*>
}

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.isInternetOn(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}



inline fun <reified T> String.toPojoOrNull(): T? {
    return try {
        Gson().fromJson(this, T::class.java)
    } catch (e: Exception) {
        null
    }
}



/**
 * Extension functions for fragments to interact with MainActivity
 */

/**
 * Get MainActivity instance from fragment
 */
fun Fragment.getMainActivity(): MainActivity? {
    return activity as? MainActivity
}

/**
 * Update message badge count from fragment
 */
fun Fragment.updateMessageBadge(count: Int) {
    getMainActivity()?.updateMessageBadge(count)
}

/**
 * Update admin pending requests badge from fragment
 */
fun Fragment.updateAdminBadge(count: Int) {
    getMainActivity()?.updateAdminBadge(count)
}

/**
 * Check if fragment has MainActivity
 */
fun Fragment.hasMainActivity(): Boolean {
    return getMainActivity() != null
}