package engine;

import java.awt.*;

/**
 * Created by brandon on 9/21/2016.
 */
public class RenderLine {

    public float wX, wY, distance;
    public int x, y, width, height;
    public Color color;
    public Camera camera;

    public RenderLine(float wX, float wY, int x, int y, int width, int height, float distance, Color color, Camera camera) {
        this.wX = wX;
        this.wY = wY;
        this.distance = distance;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.camera = camera;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, -height);
    }

}
