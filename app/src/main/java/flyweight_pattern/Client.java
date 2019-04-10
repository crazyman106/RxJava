package flyweight_pattern;

import java.util.Random;

public class Client {

    public static void main(String args[]) {
        AbstractChessman chessman = null;
        for (int i = 1; i < 20; i++) {
            switch (new Random().nextInt(2)) {
                case 0:
                    chessman = FiveChessmanFactory.getInstance().getChessman('B');
                    break;
                case 1:
                    chessman = FiveChessmanFactory.getInstance().getChessman('W');
                    break;
                default:
                    break;
            }
            if (chessman != null) {
                chessman.point(i, new Random().nextInt(20));
            }
        }
    }
}
