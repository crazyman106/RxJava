package screenrecord.record3

import android.annotation.SuppressLint
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.Surface
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("NewApi")
class ScreenRecordService : Thread {
    companion object {
        private const val TAG = "ScreenRecordService"
    }

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mBitRate: Int = 0
    private var mDpi: Int = 0
    private var mDstPath: String? = null
    private var mMediaProjection: MediaProjection? = null

    // parameters for the encoder
    private val MIME_TYPE = "video/avc" // H.264 Advanced
    // Video Coding
    private val FRAME_RATE = 30 // 30 fps
    private val IFRAME_INTERVAL = 10 // 10 seconds between
    // I-frames
    private var TIMEOUT_US: Long = 10000

    private var mEncoder: MediaCodec? = null
    private var mSurface: Surface? = null
    private var mMuxer: MediaMuxer? = null
    private var mMuxerStarted = false
    private var mVideoTrackIndex = -1
    private val mQuit = AtomicBoolean(false)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private var mBufferInfo = MediaCodec.BufferInfo()
    private var mVirtualDisplay: VirtualDisplay? = null

    constructor(width: Int, height: Int, bitrate: Int, dpi: Int, mp: MediaProjection, dstPath: String) {
        mWidth = width;
        mHeight = height;
        mBitRate = bitrate;
        mDpi = dpi;
        mMediaProjection = mp;
        mDstPath = dstPath;
    }


    /**
     * stop task
     */
    fun quit() {
        mQuit.set(true)
    }

    override fun run() {
        try {
            prepareEncoder()
            mMuxer = MediaMuxer(mDstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            mVirtualDisplay = mMediaProjection?.createVirtualDisplay(TAG + "-display", mWidth, mHeight, mDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mSurface, null, null);
            Log.d(TAG, "created virtual display: " + mVirtualDisplay);
            recordVirtualDisplay();
        } catch (ex: Exception) {
            throw  RuntimeException(ex);
        } finally {
            release()
        }
    }


    private fun recordVirtualDisplay() {
        while (!mQuit.get()) {
            var index = mEncoder?.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US)
            if (index === MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // 后续输出格式变化
                resetOutputFormat()
            } else if (index === MediaCodec.INFO_TRY_AGAIN_LATER) {
                // 请求超时
                Log.d(TAG, "retrieving buffers time out!");
                try {
                    // wait 10ms
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                }

            } else if (index!! >= 0) {
                // 有效输出
                if (!mMuxerStarted) {
                    throw IllegalStateException("MediaMuxer dose not call addTrack(format) ")
                }
                encodeToVideoTrack(index)
                mEncoder?.releaseOutputBuffer(index, false)
            }
        }
    }

    /**
     * 硬解码获取实时帧数据并写入mp4文件
     */
    private fun encodeToVideoTrack(index: Int) {
        // 获取到的实时帧视频数据
        var encodedData = mEncoder?.getOutputBuffer(index)
        if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG !== 0) {
            // The codec config data was pulled out and fed to the muxer when we got the INFO_OUTPUT_FORMAT_CHANGED status.
            // Ignore it.
            Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG")
            mBufferInfo.size = 0
        }
        if (mBufferInfo.size == 0) {
            Log.d(TAG, "info.size == 0, drop it.");
            encodedData = null;
        } else {
//      Log.d(TAG, "got buffer, info: size=" + mBufferInfo.size + ", presentationTimeUs="
//          + mBufferInfo.presentationTimeUs + ", offset=" + mBufferInfo.offset);
        }
        if (encodedData != null) {
            mMuxer?.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);
        }
    }

    private fun resetOutputFormat() {
        // should happen before receiving buffers, and should only happen once
        if (mMuxerStarted) throw IllegalStateException("output format already changed!");
        var mediaFormat = mEncoder?.outputFormat
        mVideoTrackIndex = mMuxer?.addTrack(mediaFormat)!!
        mMuxer?.start()
        mMuxerStarted = true;
        Log.i(TAG, "started media muxer, videoIndex=" + mVideoTrackIndex);
    }

    private fun prepareEncoder() {
        val videoFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight)
        videoFormat?.let {
            it.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            it.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);// 一个以位/秒为单位描述平均比特率的键
            it.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE); //以帧/秒为单位描述视频格式的帧速率的键。
            it.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            Log.d(TAG, "created video format: " + videoFormat);
        }
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder?.let {
            it.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mSurface = it.createInputSurface();
            Log.d(TAG, "created input surface: " + mSurface);
            it.start();
        }
    }

    private fun release() {
        mEncoder?.let {
            it.stop()
            it.release()
            mEncoder = null
        }
        mVirtualDisplay?.release()
        if (mMediaProjection != null) {
            mMediaProjection?.stop()
        }
        mMuxer?.let {
            it.stop()
            it.release()
            mMuxer = null
        }
    }
}