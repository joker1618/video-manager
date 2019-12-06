package xxx.joker.libs.core.object;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class JkArea {

    private int x;
    private int y;
    private int width;
    private int height;

    public JkArea() {

    }
    public JkArea(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return strf("[{}:{} - {}x{}]", x, y, width, height);
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
	public int getY() {
        return y;
    }
	public void setY(int y) {
        this.y = y;
    }
	public int getWidth() {
        return width;
    }
	public void setWidth(int width) {
        this.width = width;
    }
	public int getHeight() {
        return height;
    }
	public void setHeight(int height) {
        this.height = height;
    }
	public int getEndX() {
        return getX() + getWidth();
    }
    public int getEndY() {
        return getY() + getHeight();
    }
}
