package org.poul.bits.android.services

import android.app.IntentService
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import org.poul.bits.R
import org.poul.bits.android.broadcasts.BitsStatusReceivedBroadcast
import org.poul.bits.android.controllers.bitsclient.IBitsClient
import org.poul.bits.android.controllers.bitsclient.impl.BitsJsonV3Client
import eu.depau.commons.android.kotlin.ktexts.buildCompat
import eu.depau.commons.android.kotlin.ktexts.getNotificationBuilder

private const val ACTION_RETRIEVE_STATUS = "org.poul.bits.android.services.action.ACTION_RETRIEVE_STATUS"
private const val CHANNEL_RETRIEVE_STATUS = "org.poul.bits.android.notification.CHANNEL_RETRIEVE_STATUS"

private const val FOREGROUND_RETRIEVE_STATUS_ID = 4389

class BitsRetrieveStatusService : IntentService("BitsRetrieveStatusService") {

    private val bitsClient: IBitsClient = BitsJsonV3Client()

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_RETRIEVE_STATUS -> {
                handleActionRetrieveStatus()
            }
        }
    }

    private fun getForegroundNotification(): Notification {
        return getNotificationBuilder(this, CHANNEL_RETRIEVE_STATUS)
            .setContentTitle(getString(R.string.updating_bits_status))
            .buildCompat()
    }


    private fun handleActionRetrieveStatus() {
        startForeground(FOREGROUND_RETRIEVE_STATUS_ID, getForegroundNotification())
        val data = bitsClient.downloadData()
        BitsStatusReceivedBroadcast.localBroadcast(this, data)
        stopForeground(true)
    }

    companion object {
        @JvmStatic
        fun startActionRetrieveStatus(context: Context) {
            val intent = Intent(context, BitsRetrieveStatusService::class.java).apply {
                action = ACTION_RETRIEVE_STATUS
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
