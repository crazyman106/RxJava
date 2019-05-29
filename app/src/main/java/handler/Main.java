package handler;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class Main {

    // 一种
    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO 处理事件
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(callback);

    // 二种
    private static class MyHandler extends Handler {
        private final WeakReference<Activity> mWeakRefActivity;

        public MyHandler(Activity activity) {
            mWeakRefActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakRefActivity != null && mWeakRefActivity.get() != null) {
                // TODO do something
            }
        }
    }

    // 三种

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private void onDestroy() {
        handler.removeCallbacksAndMessages(null);
    }
}
