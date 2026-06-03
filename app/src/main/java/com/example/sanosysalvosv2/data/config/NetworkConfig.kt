package com.example.sanosysalvosv2.data.config

import android.content.Context
import com.example.sanosysalvosv2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.URI
import java.util.Collections

object NetworkConfig {
    private const val PREFS_NAME = "network_config"
    private const val KEY_BACKEND_HOST = "backend_host"
    private const val CONNECT_TIMEOUT_MS = 180

    @Volatile
    private var backendHost: String = defaultBackendHost()
    private lateinit var appContext: Context
    private val recoveryMutex = Mutex()

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_BACKEND_HOST, null)
        backendHost = saved?.normalizeHostOrNull() ?: defaultBackendHost()
    }

    fun currentBackendHost(): String = backendHost

    suspend fun recoverBackendHost(): String? = withContext(Dispatchers.IO) {
        recoveryMutex.withLock {
            val candidates = linkedSetOf<String>()
            candidates += backendHost
            candidates += defaultBackendHost()
            candidates += "127.0.0.1"
            candidates += "10.0.2.2"

            val firstReachable = candidates.firstOrNull { host -> isHostReachable(host) }
            if (firstReachable != null) {
                persistHost(firstReachable)
                return@withLock firstReachable
            }

            val discovered = discoverHostInLocalSubnet()
            if (discovered != null) {
                persistHost(discovered)
            }
            return@withLock discovered
        }
    }

    fun apiBaseUrl(): String = "http://$backendHost:8081/"

    fun bffBaseUrl(): String = "http://$backendHost:8080/"

    private fun persistHost(host: String) {
        backendHost = host
        if (::appContext.isInitialized) {
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_BACKEND_HOST, host)
                .apply()
        }
    }

    private fun discoverHostInLocalSubnet(): String? {
        val localIp = localIpv4Address() ?: return null
        val parts = localIp.split('.')
        if (parts.size != 4) return null
        val prefix = "${parts[0]}.${parts[1]}.${parts[2]}"
        val current = parts[3].toIntOrNull() ?: return null

        val prioritized = listOf(120, 100, 101, 10, 20, 30, 40, 50, 200)
            .filter { it in 2..254 && it != current }
            .map { "$prefix.$it" }

        for (host in prioritized) {
            if (isHostReachable(host)) return host
        }

        for (last in 2..254) {
            if (last == current) continue
            val host = "$prefix.$last"
            if (isHostReachable(host)) return host
        }

        return null
    }

    private fun localIpv4Address(): String? {
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (iface in interfaces) {
            if (!iface.isUp || iface.isLoopback) continue
            for (addr in Collections.list(iface.inetAddresses)) {
                if (addr is Inet4Address && !addr.isLoopbackAddress && addr.isSiteLocalAddress) {
                    return addr.hostAddress
                }
            }
        }
        return null
    }

    private fun isHostReachable(host: String): Boolean {
        val normalized = host.normalizeHostOrNull() ?: return false
        return isPortOpen(normalized, 8080) && isPortOpen(normalized, 8081)
    }

    private fun isPortOpen(host: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun defaultBackendHost(): String =
        URI(BuildConfig.API_BASE_URL).host?.trim().orEmpty().ifBlank { "10.0.2.2" }

    private fun String.normalizeHostOrNull(): String? {
        val raw = trim().removePrefix("http://").removePrefix("https://")
        val host = raw.substringBefore('/').substringBefore(':').trim()
        if (host.isBlank()) return null
        return host
    }
}