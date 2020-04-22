/*
 * Copyright 2020 Andrey Nikanorov (andrey@nikanorov.com) and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package com.nikanorov.yamucontrol

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestNotificationAccess()
        loadInfoText()
    }

    private fun requestNotificationAccess() {
        if (!NotificationListener.isEnabled(this)) {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)


            dialog.setTitle(getString(R.string.permission_dialog_title))
                .setCancelable(false)
                .setMessage(getString(R.string.notification_access))

                .setPositiveButton(getString(R.string.go_to_settings_button)) { _, _ ->
                    try {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.notification_permission_failed,
                            Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                    }
                }
                .setNegativeButton(getString(R.string.button_quit)) { _, _ -> finish() }
                .show()
        }
    }

    private fun loadInfoText() {
        txtInfo.linksClickable = true
        txtInfo.movementMethod = LinkMovementMethod.getInstance()
        txtInfo.text = Html.fromHtml(getString(R.string.info_text))

    }
}