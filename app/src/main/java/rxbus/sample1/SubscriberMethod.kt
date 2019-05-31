package rxbus.sample1

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class SubscriberMethod(var subscriber: Any, var method: Method, var eventType: Class<*>, var code: Int, var threadMode: ThreadMode, var sticky: Boolean) {


    /**
     * 调用方法
     * @param o 参数
     */
    operator fun invoke(o: Any) {
        try {
            method.invoke(subscriber, o)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }

}
