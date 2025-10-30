package com.opc.demo.OpcUtils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.opc.demo.error
import com.opc.demo.info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.opcfoundation.ua.application.Client
import org.opcfoundation.ua.core.EndpointDescription
import org.opcfoundation.ua.utils.EndpointUtil
import java.io.File
import kotlin.coroutines.resume

/**
 * ● OpcManager
 *
 * ● 2024/5/17 18:27
 * @author crowforkotlin
 * @formatter:on
 */
class OpcManager(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val mContext: Context,
) {

    companion object { const val URL = "opc.tcp://192.168.1.242:59520"; }
//    companion object { const val URL = "opc.tcp://192.168.1.101:49320"; }

    private var opcManager: ManagerOPC? = null

    suspend fun initOpc(): SessionElement? = suspendCancellableCoroutine { continuation ->
        mLifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                opcManager = null
                "init opc".info()
                val opcManager = ManagerOPC.CreateManagerOPC(File(mContext.filesDir, "OPCCert.der"), File(mContext.filesDir, "OPCCert.pem")).also { this@OpcManager.opcManager = it }
                val client = opcManager.client
                val endpoints: Array<EndpointDescription> = EndpointUtil.selectByProtocol(EndpointUtil.sortBySecurityLevel( client.discoverEndpoints(URL) as Array<EndpointDescription?>), "opc.tcp")
                val sessionPosition = opcManager.CreateSession(URL, endpoints[0])
                val session = opcManager.sessions[sessionPosition]
                continuation.resume(session)
            }
                .onFailure { it.stackTraceToString().info() }
                .getOrElse { continuation.resume(null) }
        }
    }
    fun readValue(session: SessionChannel, opc: Opc): String {
        val resp = Read(null, 0.0, TimestampsToReturn.Both, ReadValueId(NodeId(2, opc.mTag), Attributes.Value, null, null))
        val result =  (resp.results.first().value.value ?: "").toString()
        return result
    }
    fun clean() {
        runCatching {
            opcManager?.apply {
                for (session in getSessions()) {
                    session.stopRunning()
                    session.session.closeAsync()
                }
                sessions.clear()
            }
        }
            .onFailure { it.stackTraceToString().error() }
    }
}