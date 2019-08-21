package screenrecord.record2

import android.annotation.SuppressLint
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection


/**
 * 使用该线程独立录屏
 */
@SuppressLint("NewApi")
class MediaRecordService : Thread {

    companion object {
        const val TAG = "MediaRecordService"
    }

    var mWidth: Int = 0
    var mHeight: Int = 0
    var mBitRate: Int = 0
    var mDpi: Int = 0
    var mDstPath: String? = null
    var mMediaRecorder: MediaRecorder? = null
    var mMediaProjection: MediaProjection? = null
    var FRAME_RATE = 60 // 60 fps

    var mVirtualDisplay: VirtualDisplay? = null


    constructor(width: Int, height: Int, bitrate: Int, dpi: Int, mp: MediaProjection, dstPath: String) {
        mWidth = width
        mHeight = height
        mBitRate = bitrate
        mDpi = dpi
        mMediaProjection = mp
        mDstPath = dstPath
    }

    override fun run() {
        try {
            initMediaRecorder()
            //在mediarecorder.prepare()方法后调用
            mVirtualDisplay = mMediaProjection?.createVirtualDisplay(TAG + "-display", mWidth, mHeight, mDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mMediaRecorder?.getSurface(), null, null)
            mMediaRecorder?.start();
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            release()
        }
    }

    private fun initMediaRecorder() {
        mMediaRecorder = MediaRecorder();
        mMediaRecorder?.let {
            it.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            it.setAudioSource(MediaRecorder.AudioSource.MIC)
            it.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            it.setOutputFile(mDstPath);
            it.setVideoSize(mWidth, mHeight)
            it.setVideoFrameRate(FRAME_RATE);
            it.setVideoEncodingBitRate(mBitRate);
            it.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            it.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            it.prepare()
        }
    }

    public fun release() {
        mVirtualDisplay?.let {
            it.release();
            mVirtualDisplay = null;
        }
        mMediaRecorder?.let {
            it.setOnErrorListener(null);
            it.stop();
            it.reset();
            it.release();
        }
        mMediaProjection?.let {
            it.stop();
            mMediaProjection = null;
        }
    }
}