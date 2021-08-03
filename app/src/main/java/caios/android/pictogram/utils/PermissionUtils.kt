package caios.android.pictogram.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {

    val requestPermissions = listOf(Manifest.permission.CAMERA)

    fun requestPermission(activity: AppCompatActivity, permissions: Collection<String>, id: Int) {
        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), id)
    }

    fun requestPermission(fragment: Fragment, permissions: Collection<String>, id: Int) {
        fragment.requestPermissions(permissions.toTypedArray(), id)
    }

    fun isAllowed(context: Context, permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }

    fun isAllowed(context: Context, permissions: Collection<String>): Boolean {
        return (!permissions.map { isAllowed(context, it) }.contains(false))
    }

    fun isShouldRequest(activity: AppCompatActivity, permission: String): Boolean {
        return (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
    }

    fun isShouldRequest(fragment: Fragment, permission: String): Boolean {
        return (fragment.shouldShowRequestPermissionRationale(permission))
    }

    fun isShouldRequest(activity: AppCompatActivity, permissions: Collection<String>): Boolean {
        return (!permissions.map { isShouldRequest(activity, it) }.contains(false))
    }

    fun isShouldRequest(fragment: Fragment, permissions: Collection<String>): Boolean {
        return (!permissions.map { isShouldRequest(fragment, it) }.contains(false))
    }

    fun getShouldRequestPermission(activity: AppCompatActivity, permissions: Collection<String>): List<String> {
        return (permissions.filter { isShouldRequest(activity, it) })
    }

    fun getShouldRequestPermission(fragment: Fragment, permissions: Collection<String>): List<String> {
        return (permissions.filter { isShouldRequest(fragment, it) })
    }

    fun getAllowedPermission(context: Context, permissions: Collection<String>): List<String> {
        return permissions.mapNotNull { if (!isAllowed(context, it)) null else it }
    }

    fun getNotAllowedPermission(context: Context, permissions: Collection<String>): List<String> {
        return permissions.mapNotNull { if (isAllowed(context, it)) null else it }
    }

    fun startAppInfoActivity(context: Context) {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
    }
}