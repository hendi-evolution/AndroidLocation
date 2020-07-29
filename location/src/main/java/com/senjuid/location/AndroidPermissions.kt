package com.senjuid.location

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class AndroidPermissions(private val mContext: Activity, vararg requiredPermissions: String) {
    private val mRequiredPermissions: Array<String> = arrayOf(*requiredPermissions)
    private val mPermissionsToRequest: MutableList<String> = ArrayList()

    fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) return true
        for (permission in mRequiredPermissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionsToRequest.add(permission)
            }
        }
        return mPermissionsToRequest.isEmpty()
    }

    fun requestPermissions(requestCode: Int) {
        val request = mPermissionsToRequest.toTypedArray()
        val log = StringBuilder()
        log.append("Requesting permissions:\n")
        for (permission in request) {
            log.append(permission).append("\n")
        }
        Log.i(javaClass.simpleName, log.toString())
        ActivityCompat.requestPermissions(mContext, request, requestCode)
    }

    fun areAllRequiredPermissionsGranted(permissions: Array<String>?, grantResults: IntArray?): Boolean {
        if (permissions == null || permissions.isEmpty() || grantResults == null || grantResults.isEmpty()) {
            return false
        }
        val perms = LinkedHashMap<String, Int?>()
        for (i in permissions.indices) {
            if (!perms.containsKey(permissions[i])
                    || perms.containsKey(permissions[i]) && perms[permissions[i]] == PackageManager.PERMISSION_DENIED) perms[permissions[i]] = grantResults[i]
        }
        for ((_, value) in perms) {
            if (value != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}