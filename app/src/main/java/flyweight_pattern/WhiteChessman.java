package flyweight_pattern;

public class WhiteChessman extends AbstractChessman {
    public WhiteChessman() {
        super("白棋");
    }

    @Override
    public void point(int x, int y) {
        this.x = x;
        this.y = y;
        show();
    }
}
