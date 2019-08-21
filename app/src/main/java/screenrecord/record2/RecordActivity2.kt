package screenrecord.record2

import android.annotation.SuppressLint
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import com.rxjava.R
import kotlinx.android.synthetic.main.record_activity.*
import java.io.File

class RecordActivity2 : AppCompatActivity() {

    lateinit var mMediaProjectionManager: MediaProjectionManager

    companion object {
        const val LOCAL_REQUEST_CODE: Int = 10001;
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_activity)

        // 1.初始化MediaProjectionManager实例
        mMediaProjectionManager = getSystemService("media_projection") as MediaProjectionManager

        start.setOnClickListener {
            // 2.申请权限
            val captureIntent = mMediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(captureIntent, LOCAL_REQUEST_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 3.在onActivityResult中获取结果
        val mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.let {
            // 4.创建MediaRecord线程进行音频处理,同时对音频和视频统一处理
            val file = File("xx.mp4")  //录屏生成文件
            MediaRecordService(1080, 1920, 6000000, 1,
                    mediaProjection, file.getAbsolutePath()).start()
        }
    }

}