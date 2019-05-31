package rxbus.sample1

import java.lang.annotation.*

/**
 * 注解类
 * @author wzg 2016/9/21
 */
@Documented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Subscribe(val code: Int = -1, val threadMode: ThreadMode = ThreadMode.CURRENT_THREAD, val sticky: Boolean = false)
