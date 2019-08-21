package rxbus.sample1

import io.reactivex.subjects.PublishSubject
import org.reactivestreams.Subscription
import java.util.concurrent.ConcurrentHashMap

class RxBus {

    private val subscriptionsByEventType: MutableMap<Class<*>, MutableList<Subscription>> = HashMap();
    private val eventTypesBySubscriber: MutableMap<Any, MutableList<Class<*>>> = HashMap()
    private val subscriberMethodByEventType: MutableMap<Class<*>, Any> = HashMap()
    private val stickyEvent: MutableMap<Class<*>, Object> = ConcurrentHashMap()

    private val subject = PublishSubject.create<Any>().toSerialized()
    val instace: RxBus by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        RxBus()
    }
}
