package flyweight_pattern;

public abstract class AbstractChessman {

    // 棋子坐标
    protected int x;
    protected int y;

    // 棋子类型(黑|白)
    protected String chess;

    public AbstractChessman(String chess) {
        this.chess = chess;
    }

    public abstract void point(int x, int y);

    // 显示棋子信息
    public void show() {
        System.out.println(this.chess + "(" + this.x + "," + this.y + ")" + this);
    }
}
