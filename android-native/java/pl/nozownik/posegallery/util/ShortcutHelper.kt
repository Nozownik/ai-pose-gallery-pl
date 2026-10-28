package pl.nozownik.posegallery.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import pl.nozownik.posegallery.MainActivity
import pl.nozownik.posegallery.R

object ShortcutHelper {
    const val ACTION_RANDOM = "pl.nozownik.posegallery.RANDOM_POSE"

    fun ensureDynamicShortcuts(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_RANDOM
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val shortcut = ShortcutInfo.Builder(context, "random_pose")
            .setShortLabel("Random pose")
            .setLongLabel("Pose of the day / random")
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()
        sm.dynamicShortcuts = listOf(shortcut)
    }

    fun requestPinRandom(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return false
        if (!sm.isRequestPinShortcutSupported) return false
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_RANDOM
        }
        val shortcut = ShortcutInfo.Builder(context, "random_pose_pin")
            .setShortLabel("Random pose")
            .setLongLabel("Random pose of the day")
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()
        return sm.requestPinShortcut(shortcut, null)
    }
}
