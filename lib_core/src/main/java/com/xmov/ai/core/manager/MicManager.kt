import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.xmov.ai.core.log.LogUtil

object MicManager {

    // 音频参数配置
    private const val SAMPLE_RATE = 44100 // 采样率(Hz)
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO // 单声道
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT // 16位PCM编码

    private var audioRecord: AudioRecord? = null
    private var isRecording = false // 麦克风是否打开
    private var recordThread: Thread? = null

    /**
     * 打开麦克风（开始录音）
     * @return 是否成功打开
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun openMic(): Boolean {
        if (isRecording) {
            LogUtil.w("麦克风已处于打开状态")
            return true
        }

        // 计算最小缓冲区大小
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            LogUtil.e( "音频参数错误，无法初始化麦克风")
            return false
        }

        // 初始化AudioRecord
        val tempRecorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC, // 音频源：麦克风
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
        } catch (e: Exception) {
            LogUtil.e( "初始化麦克风失败", e)
            return false
        }

        if (tempRecorder.state != AudioRecord.STATE_INITIALIZED) {
            LogUtil.e("麦克风初始化未完成，状态异常")
            tempRecorder.release()
            return false
        }

        // 开始录音
        tempRecorder.startRecording()
        isRecording = true
        audioRecord = tempRecorder

        // 启动录音线程（子线程读取音频数据）
        recordThread = Thread {
            val buffer = ByteArray(bufferSize)
            while (isRecording) {
                // 读取麦克风数据（实际应用中可在此处理音频数据）
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                when (readSize) {
                    AudioRecord.ERROR_INVALID_OPERATION -> {
                        LogUtil.e( "读取音频数据失败：无效操作")
                        stopRecording()
                    }
                    AudioRecord.ERROR_BAD_VALUE -> {
                        LogUtil.e( "读取音频数据失败：参数错误")
                        stopRecording()
                    }
                    else -> {
                        // TODO: 处理音频数据（如实时分析、保存等）
                    }
                }
            }
        }.apply { start() }

        LogUtil.d( "麦克风打开成功")
        return true
    }

    /**
     * 关闭麦克风（停止录音）
     */
    fun closeMic() {
        if (!isRecording) {
            LogUtil.w("麦克风已处于关闭状态")
            return
        }

        stopRecording()
        LogUtil.d( "麦克风关闭成功")
    }

    /**
     * 停止录音并释放资源
     */
    private fun stopRecording() {
        isRecording = false

        // 等待录音线程结束
        recordThread?.let {
            try {
                it.join()
            } catch (e: InterruptedException) {
                LogUtil.w( "录音线程中断", e)
            }
            recordThread = null
        }

        // 释放AudioRecord资源
        audioRecord?.let {
            if (it.state == AudioRecord.STATE_INITIALIZED) {
                it.stop()
            }
            it.release()
            audioRecord = null
        }
    }

    /**
     * 判断麦克风是否打开
     */
    fun isMicOpen() = isRecording
}
