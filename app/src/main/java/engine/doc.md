1.静态工厂
interface Product{
}

class ProductA implement Product{
}

class ProductB implement Product{
}

class ProductC implement Product{
}

class Factory{
    Product create(Class<T> clazz){
        switch(){
        }
    }
}

1 它是一个具体的类，非接口 抽象类。有一个重要的create()方法，利用if或者 switch创建产品并返回。
2 create()方法通常是静态的，所以也称之为静态工厂

1 扩展性差（我想增加一种面条，除了新增一个面条产品类，还需要修改工厂类方法）
2 不同的产品需要不同额外参数的时候 不支持。

```java
public class FactoryProducer
{
    public static AbstractFactory getFactory(Class<T> type)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        return (AbstractFactory)type.newInstance();
    }
}
```

```java
/**
 * 宝马工厂，覆盖所有宝马车型的构造方法
 */
public class BMWFactory extends AbstractFactory
{
    public Car getCar(String type) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException
    {
        Class cl = Class.forName(type);
        return (BMWCar)cl.newInstance();
    }
}
```

```java
/**
 * 奔驰工厂，覆盖所有奔驰车型的构造方法
 */
public class BenzFactory extends AbstractFactory
{
    public Car getCar(String type) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException
    {
        Class cl = Class.forName(type);
        return (BenzCar)cl.newInstance();
    }
}
```

使用
```java
AbstractFactory abstractFactory = FactoryProducer.getFactory("BMWFactory");
abstractFactory.getCar(class);


```




