package engine

/**
 * 工厂类,通过泛型获取对应类的对象
 *
 * 如果产品为为多个层级,多种类别,例如:
 *
 * car
 *
 *  奔驰:
 *      奔驰1,泵池2
 *  宝马:
 *      宝马1,宝马2
 *
 *  这时,我们需要给工厂设计一个工厂生产类:
 */
object ImageLoaderFactory {

    public  fun <T> createImageEngine(clazz: Class<T>): T {
        return clazz.newInstance();
    }
}