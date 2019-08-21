package screenrecord

import android.util.Log
import java.io.DataOutputStream


object Record1 {
    @JvmStatic
    fun startRecordScreen() {
        if (checkRootExecutable())
            Runtime.getRuntime().exec("adb shell screenrecord --size 1080x1920 --bit-rate 8000000 --time-limit 10 /sccard/record.mp4")
    }

    private fun checkRootExecutable(): Boolean {

        var process: Process? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process!!.outputStream)
            os!!.writeBytes("exit\n")
            os!!.flush()
            val exitValue = process!!.waitFor()
            return if (exitValue == 0) {
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: " + e.message)
            return false
        } finally {
            try {
                if (os != null) {
                    os!!.close()
                }
                process!!.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}