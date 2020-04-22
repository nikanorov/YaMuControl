/*
 * Copyright 2020 Andrey Nikanorov (andrey@nikanorov.com) and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package com.nikanorov.yamucontrol

import android.content.Context
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat


class NotificationListener : NotificationListenerService() {
    companion object {
        fun isEnabled(context: Context): Boolean {
            return NotificationManagerCompat
                .getEnabledListenerPackages(context)
                .contains(context.packageName)
        }
    }
}