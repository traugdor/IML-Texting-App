package com.hack.smsgroups.telephony

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony

object DefaultRoleManager {

    fun isDefaultSmsApp(context: Context): Boolean {
        val pkg = Telephony.Sms.getDefaultSmsPackage(context)
        return pkg == context.packageName
    }

    fun requestRoleIntent(context: Context): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            }
        }
        return Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
    }
}
