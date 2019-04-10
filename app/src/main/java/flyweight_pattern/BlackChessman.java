package flyweight_pattern;

public class BlackChessman extends AbstractChessman {
    public BlackChessman() {
        super("黑棋");
    }

    @Override
    public void point(int x, int y) {
        this.x = x;
        this.y = y;
        show();
    }
}
