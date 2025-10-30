# **OpcUa**

> 基于OpcUa协议开发的一个Kotlin项目，项目仅供参考，完整配置需要将依赖，实现拷贝到自己项目，本项目仅提供一些核心的参考配置

- 库依赖基于此开源构建 
- [Sources Library](https://github.com/OPCFoundation/UA-Java-Legacy)
- [Sources Library Fork](https://github.com/crowforkotlin/UA-Java-Legacy)
- [OpcuaDemo.apk，项目也存在一份编译好的apk](https://github.com/crowforkotlin?tab=repositories&q=opc&type=&language=&sort=)

- **OpcUa踩坑，这里需要下载源码，自己编译Jar包，因为现在远程依赖的版本有很多问题没有修复，该库也停止了维护**
- **android系统时间一定要同步到最新的不然读写会有问题**
- **这个库通过XXXRequest类进行读写，每次构造这个Request就会产生一个新线程，Opc的服务器负载有限，不可以太多的连接，不然会失败，所以，要通过将多个需要采集的数据合并到一个请求，这个时候就可以单独构造XXXRequest(这里面传入数组[需要读取的节点])**

# 核心封装
> 此项目基本只需要用到OpcManager.kt, 其他的都java代码是从OpcuaDemo 拷贝过来的，可能有一点改动（？...不确定）,如果需要自己扩展、优化，可在OpcManager下增加写入、读取的一些操作，这里仅增加了我个人需要用到的实现
```kotlin
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
```
