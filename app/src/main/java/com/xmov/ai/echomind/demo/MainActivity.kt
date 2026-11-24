package com.xmov.ai.echomind.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.xmov.ai.core.base.BaseMvvmActivity
import com.xmov.ai.echomind.demo.databinding.ActivityMainBinding
import com.xmov.metahuman.sdk.PreCacheListener
import com.xmov.xingyunavataragent.LlmUsage
import com.xmov.xingyunavataragent.VolumeAndRepetitionTextResult
import com.xmov.xingyunavataragent.XingyunAvatarAgent
import com.xmov.metahuman.sdk.impl.data.IRawEventFrameData
import com.xmov.xingyunavataragent.SemanticJudgeResult
import com.xmov.xingyunavataragent.XingyunAvatarAgentConfig
import com.xmov.xingyunavataragent.XingyunAvatarAgentError
import com.xmov.xingyunavataragent.XingyunAvatarAgentListener
import com.xmov.xingyunavataragent.XingyunAvatarAgentState
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class MainActivity : BaseMvvmActivity<ActivityMainBinding, MainViewModel>(),
    XingyunAvatarAgentListener {

    private companion object {
        private const val TAG = "MainActivity"
        const val REQUEST_RECORD_AUDIO = 1
        const val DEFAULT_SPEAK_TEXT = "你好，我是数字人。"
        const val CONFIG_ASSET = "agent_config.json"
    }

    private lateinit var client: XingyunAvatarAgent
    private var volumeOn = true
    private var avatarInitialized = false
    private val llmSubtitle = StringBuilder()
    private var avatarInitConfigJson: String = "{}"
    private var useExternalPcmAudio = false
    private var audioFileCounter = 0
    private var isOffline = false

    override fun initView(savedInstanceState: Bundle?) {
        client = XingyunAvatarAgent()
        mBinding.tvLog.movementMethod = ScrollingMovementMethod()
        mBinding.tvSubtitle.movementMethod = ScrollingMovementMethod()

        mBinding.btnToggleParams.setOnClickListener {
            mBinding.paramPanel.isVisible = !mBinding.paramPanel.isVisible
        }
        bindAvatarControls()
        bindAgentControls()
        observeViewModel()
    }

    override fun initData() {
        loadConfigFromAssets()
    }

    /** 从 assets/agent_config.json 读取联调参数并填充到参数面板（面板内仍可临时修改）。 */
    private fun loadConfigFromAssets() {
        val cfg = try {
            val json = assets.open(CONFIG_ASSET).bufferedReader().use { it.readText() }
            Gson().fromJson(json, AssetConfig::class.java)
        } catch (e: Exception) {
            mViewModel.appendLog("load $CONFIG_ASSET failed: ${e.message}")
            return
        } ?: return
        with(mBinding) {
            cfg.asrConfigJson?.let { etAsrConfig.setText(it) }
            cfg.ttsConfigJson?.let { etTtsConfig.setText(it) }
            cfg.brainConfigJson?.let { etBrainConfig.setText(it) }
            cfg.sessionSpeakReqId?.let { etSessionSpeakReqId.setText(it.toString()) }
            cfg.avatarAppId?.takeIf { it.isNotBlank() }?.let { etAvatarAppId.setText(it) }
            cfg.avatarAppSecret?.takeIf { it.isNotBlank() }?.let { etAvatarAppSecret.setText(it) }
            cfg.avatarGatewayServer?.takeIf { it.isNotBlank() }?.let { etAvatarGateway.setText(it) }
            cfg.preCacheUrl?.takeIf { it.isNotBlank() }?.let { etPreCacheUrl.setText(it) }
            cfg.avatarInitConfigJson?.takeIf { it.isNotBlank() }?.let {
                etAvatarInitConfigJson.setText(it)
                avatarInitConfigJson = it
            }
            cfg.useExternalPcmAudio?.let { useExternalPcmAudio = it }
            cfg.featuresJson?.let { etFeatures.setText(it) }
            cfg.extrasJson?.let { etExtras.setText(it) }
        }
        mViewModel.appendLog("config loaded from assets/$CONFIG_ASSET")
    }

    private class AssetConfig(
        @SerializedName("asrConfig") val asrConfigJson: String? = null,
        @SerializedName("ttsConfig") val ttsConfigJson: String? = null,
        @SerializedName("brainConfig") val brainConfigJson: String? = null,
        @SerializedName("features") val featuresJson: String? = null,
        @SerializedName("sessionSpeakReqId") val sessionSpeakReqId: Int? = null,
        @SerializedName("avatarAppId") val avatarAppId: String? = null,
        @SerializedName("avatarAppSecret") val avatarAppSecret: String? = null,
        @SerializedName("avatarGatewayServer") val avatarGatewayServer: String? = null,
        @SerializedName("preCacheUrl") val preCacheUrl: String? = null,
        @SerializedName("avatarInitConfigJson") val avatarInitConfigJson: String? = null,
        @SerializedName("useExternalPcmAudio") val useExternalPcmAudio: Boolean? = null,
        @SerializedName("extras") val extrasJson: String? = null,
    )

    private fun bindAvatarControls() = with(mBinding) {
        btnInit.setOnClickListener { connect() }
        btnRelease.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            client.stop()
            avatarInitialized = false
            mViewModel.appendLog("released")
        }
        btnOffline.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            if (isOffline) {
                client.switchModel(false)
                isOffline = false
                btnOffline.text = "切离线"
                mViewModel.appendLog("switched to online (reconnect)")
            } else {
                client.switchModel(true)
                isOffline = true
                btnOffline.text = "重连"
                mViewModel.appendLog("switched to offline")
            }
        }
        btnVolume.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            volumeOn = !volumeOn
            client.setVolume(if (volumeOn) 1f else 0f)
            btnVolume.text = if (volumeOn) "静音" else "取消静音"
        }
        btnSpeak.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            val text = etAsk.text.toString().trim().ifEmpty { DEFAULT_SPEAK_TEXT }
            client.speak(text, isStart = true, isEnd = true)
        }
        btnListen.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            client.listen()
        }
        btnThink.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            client.think()
        }
        btnIdle.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            client.idle()
        }
        btnInteractive.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            client.interactiveidle()
        }
        btnPreCache.setOnClickListener { preCache(char = false) }
        btnPreCacheChar.setOnClickListener { preCache(char = true) }
    }

    private fun bindAgentControls() = with(mBinding) {
        btnStartListen.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            startAsrWithPermission()
        }
        btnStopListen.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            client.stopASR()
        }
        btnInterrupt.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            client.interrupt()
        }
        btnAsk.setOnClickListener {
            if (!requireInitialized()) return@setOnClickListener
            val text = etAsk.text.toString().trim()
            if (text.isNotEmpty()) client.ask(text)
        }
    }

    private fun observeViewModel() {
        mViewModel.state.observe(this) { mBinding.tvState.text = "状态: $it" }
        mViewModel.subtitle.observe(this) { text ->
            mBinding.tvSubtitle.text = text
            mBinding.tvSubtitle.post {
                val layout = mBinding.tvSubtitle.layout ?: return@post
                val overflow = layout.getLineTop(mBinding.tvSubtitle.lineCount) - mBinding.tvSubtitle.height
                mBinding.tvSubtitle.scrollTo(0, maxOf(overflow, 0))
            }
        }
        mViewModel.logs.observe(this) { text ->
            mBinding.tvLog.text = text
            mBinding.tvLog.post {
                val layout = mBinding.tvLog.layout ?: return@post
                val overflow = layout.getLineTop(mBinding.tvLog.lineCount) - mBinding.tvLog.height
                mBinding.tvLog.scrollTo(0, maxOf(overflow, 0))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (avatarInitialized) client.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (avatarInitialized) client.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.stop()
    }

    private fun connect() {
        val asrConfig = text(mBinding.etAsrConfig).takeIf { it.isNotBlank() }
        val ttsConfig = text(mBinding.etTtsConfig).takeIf { it.isNotBlank() }
        val brainConfig = text(mBinding.etBrainConfig).takeIf { it.isNotBlank() }
        val featuresConfig = text(mBinding.etFeatures).takeIf { it.isNotBlank() }
        val extrasConfig = text(mBinding.etExtras).takeIf { it.isNotBlank() }
        val sessionSpeakReqId = text(mBinding.etSessionSpeakReqId).toIntOrNull()
        val avatarAppId = text(mBinding.etAvatarAppId)
        val avatarAppSecret = text(mBinding.etAvatarAppSecret)
        val avatarGateway = text(mBinding.etAvatarGateway)
        val initConfigJson = text(mBinding.etAvatarInitConfigJson).ifBlank { avatarInitConfigJson }
        if (avatarAppId.isEmpty() || avatarAppSecret.isEmpty() || avatarGateway.isEmpty()) {
            toast("请完整填写连接参数（点右上角\"参数\"展开）")
            mBinding.paramPanel.isVisible = true
            return
        }
        Log.d("xingYunAgent", "connect: appId=$avatarAppId gateway=$avatarGateway sessionSpeakReqId=$sessionSpeakReqId")
        mBinding.paramPanel.isVisible = false
        client.init(
            this,
            mBinding.avatarLayout,
            XingyunAvatarAgentConfig(
                asrConfig = asrConfig,
                ttsConfig = ttsConfig,
                brainConfig = brainConfig,
                sessionSpeakReqId = sessionSpeakReqId ?: 1,
                avatarAppId = avatarAppId,
                avatarAppSecret = avatarAppSecret,
                avatarGatewayServer = avatarGateway,
                features = featuresConfig,
                avatarInitConfigJson = initConfigJson,
                useExternalPcmAudio = useExternalPcmAudio,
                debugLogEnabled = true,
                extras = extrasConfig,
            ),
            this,
        )
        avatarInitialized = true
        mViewModel.appendLog("init...")
    }

    private fun preCache(char: Boolean) {
        val appId = text(mBinding.etAvatarAppId)
        val appSecret = text(mBinding.etAvatarAppSecret)
        val url = text(mBinding.etPreCacheUrl)
        if (appId.isEmpty() || appSecret.isEmpty() || url.isEmpty()) {
            toast("请先在参数面板填写 AppId/AppSecret/预缓存 URL")
            mBinding.paramPanel.isVisible = true
            return
        }
        val listener = object : PreCacheListener {
            override fun onPreCacheProgress(progress: Int) {
                runOnUiThread { mViewModel.appendLog("preCache progress: $progress%") }
            }

            override fun onPreCacheComplete() {
                runOnUiThread { mViewModel.appendLog("preCache complete") }
            }

            override fun onPreCacheFailed(errorCode: Int, errorMessage: String) {
                runOnUiThread { mViewModel.appendLog("preCache failed: $errorCode $errorMessage") }
            }
        }
        if (char) {
            client.preCacheCharBin(this, appId, appSecret, url, listener)
        } else {
            client.preCache(this, appId, appSecret, url, listener)
        }
    }

    private fun startAsrWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO,
            )
            return
        }
        client.startASR()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            client.startASR()
        } else {
            toast("需要麦克风权限")
        }
    }

    private fun text(et: EditText) = et.text.toString().trim()

    /** 需要初始化成功后才能调用的操作：未初始化时弹出提示并返回 false。 */
    private fun requireInitialized(): Boolean {
        if (avatarInitialized) return true
        toast("请先初始化")
        return false
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // ---------- XingyunAvatarAgentListener（E2E 语音 Agent） ----------

    override fun onStateChanged(state: XingyunAvatarAgentState) {
        mViewModel.setState(state.name)
        mViewModel.appendLog("state -> $state")
    }

    override fun onAsrResult(text: String, isFinal: Boolean) {
        mViewModel.setSubtitle("我：$text")
        if (isFinal) mViewModel.appendLog("ASR[final]: $text")
    }

    override fun onLlmChunk(text: String, isFirst: Boolean) {
       /* if (isFirst) llmSubtitle.clear()
        llmSubtitle.append(text)
        mViewModel.setSubtitle(llmSubtitle.toString())*/
    }

    override fun onVoiceStateChange(status: String, clientSpeakId: String) {
        super.onVoiceStateChange(status, clientSpeakId)
    }

/*    override fun onAudioData(audioData: ByteArray, speechId: Int) {
        super.onAudioData(audioData, speechId)
        try {
            val webmData = if (audioData.size > 8 &&
                audioData[0] == 0x41.toByte() && audioData[1] == 0x55.toByte()
            ) {
                audioData.copyOfRange(8, audioData.size)
            } else {
                audioData
            }
            val dir = getExternalFilesDir(null) ?: cacheDir
            val file = File(dir, "farend_${audioFileCounter++}_sid${speechId}.webm")
            file.writeBytes(webmData)
            Log.d(TAG, "saved: ${file.absolutePath} (${webmData.size} bytes)")
        } catch (e: Exception) {
            Log.e(TAG, "save audio failed", e)
        }
    }*/

    override fun onLlmDone(usage: LlmUsage) {
        mViewModel.appendLog("LLM done: $usage")
    }

    override fun onVolumeAndRepetitionText(result: VolumeAndRepetitionTextResult) {
        mViewModel.appendLog(
            "音量/重复检测: skip=${result.skip} reason=${result.skipReason} " +
                "similarity=${result.similarity} subtitle=${result.currentSubtitle}"
        )
    }

    override fun onSemanticJudge(result: SemanticJudgeResult) {
        mViewModel.appendLog(
            "语义判别: query=${result.query} meaningful=${result.meaningful} " +
                    "action=${result.action}"
        )    }


    override fun onError(error: XingyunAvatarAgentError) {
        mViewModel.appendLog("ERROR: $error")
    }

    // ---------- XingyunAvatarAgentListener（数字人，EventBridge 已统一切换到主线程） ----------

    override fun onInitEvent(code: Int, message: String?) {
        mViewModel.appendLog("avatar init: code=$code $message")
        if (code == 0) mBinding.tvSession.text = message
    }

    override fun onAvatarStateChange(state: String) {
        mViewModel.appendLog("avatar state: $state")
    }

    override fun onSpeakStateChange(speakState: String, clientSpeakId: String, errorMsg: String) {
        mViewModel.appendLog("speak: $speakState $errorMsg")
    }

    override fun onAvatarReconnectEvent(code: Int, message: String?) {
        mViewModel.appendLog("avatar reconnect: code=$code $message")
    }

    override fun onOfflineEvent() {
        isOffline = true
        mBinding.btnOffline.text = "重连"
        mViewModel.appendLog("avatar offline")
    }

    override fun onSDKRuntimeError(code: Int, message: String?) {
        mViewModel.appendLog("avatar runtime error: code=$code $message")
    }

    override fun onWidgetEvent(widgetData: Any) {
        handleWidgetData(widgetData as? IRawEventFrameData)
    }

    private fun handleWidgetData(widgetData: IRawEventFrameData?) {
        if (widgetData?.event == null) return
        for (i in 0 until widgetData.event!!.length()) {
            try {
                handleWidgetEvent(widgetData.event!!.getJSONObject(i))
            } catch (e: JSONException) {
                Log.e("demo", "handleWidgetData ex: $e")
            }
        }
    }

    private fun handleWidgetEvent(event: JSONObject) {
        try {
            val type = event.getString("type")
            if (TextUtils.equals(type, "subtitle_on")) {
                val text = event.optString("text", "")
                mBinding.tvAvatarSubtitle.text = text
            } else if (TextUtils.equals(type, "subtitle_off")) {
                mBinding.tvAvatarSubtitle.text = ""
            }
        } catch (e: JSONException) {
            Log.e("demo", "handleWidgetEvent ex: $e")
        }
    }
}
