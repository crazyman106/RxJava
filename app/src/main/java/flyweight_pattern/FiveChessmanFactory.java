package flyweight_pattern;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FiveChessmanFactory {

    private static FiveChessmanFactory instance;

    private FiveChessmanFactory() {
    }

    public static FiveChessmanFactory getInstance() {
        if (instance == null) {
            synchronized (FiveChessmanFactory.class) {
                if (instance == null) {
                    instance = new FiveChessmanFactory();
                }
            }
        }
        return instance;
    }

    private Map<Character, AbstractChessman> cacheChessman = new ConcurrentHashMap<>();

    public AbstractChessman getChessman(Character key) {
        AbstractChessman chessman = cacheChessman.get(key);
        if (chessman == null) {
            // 缓存中没有对应的对象,创建对象,存入缓存中
            switch (key) {
                case 'B':
                    chessman = new BlackChessman();
                    break;
                case 'W':
                    chessman = new WhiteChessman();
                    break;
                default:
            }
            if (chessman != null) {
                cacheChessman.put(key, chessman);
            }
        }
        return chessman;
    }
}
