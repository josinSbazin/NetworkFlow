package com.github.lion4ik.networkflow.networkstate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.github.lion4ik.networkflow.Connectivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged

internal class PreLollipopNetworkStateObservingStrategy(
    private val connectivityManager: ConnectivityManager,
    private val filter: IntentFilter = IntentFilter()
) : NetworkObservingStrategy {

    @SuppressWarnings("deprecation")
    override fun observeNetworkState(appContext: Context): Flow<Connectivity> =
        channelFlow<Connectivity> {
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    sendBlocking(Connectivity.fromNetworkInfo(connectivityManager.activeNetworkInfo))
                }
            }
            appContext.registerReceiver(receiver, filter)
            awaitClose { appContext.unregisterReceiver(receiver) }
        }.distinctUntilChanged()
}