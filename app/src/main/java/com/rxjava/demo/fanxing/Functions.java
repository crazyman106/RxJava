package com.rxjava.demo.fanxing;

public class Functions {

    public static <T, U> Func1<T> isInstanceOf(Class<U> clazz) {
        return new ClassFilter<T, U>(clazz);
    }


    static final class ClassFilter<T, U> implements Func1<T> {
        final Class<U> clazz;

        ClassFilter(Class<U> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean test(T t){
            return clazz.isInstance(t);
        }
    }
}
