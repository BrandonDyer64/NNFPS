import engine.Camera;
import engine.FrostByte;
import engine.Wall;
import engine.World;

import java.awt.*;

/**
 * Created by brandon on 10/12/2016.
 */
public class Driver {

    public static float time = 0;

    public static void main(String[] args) {
        FrostByte engine = new FrostByte("Test Game");
        engine.frame.setVisible(true);
        World world = new World();
        world.shader = line -> {
            line.color = new Color((int) ((255 / line.distance) * (line.color.getRed() / 255f)), (int) ((255 / line.distance) * (line.color.getGreen() / 255f)), (int) ((255 / line.distance) * (line.color.getBlue() / 255f)));
            //line.height += (Math.cos(line.wX * 10 + time) + Math.sin(line.wY * 10 + time)) * 20;
            return line;
        };
        world.walls.add(new Wall(3, 3, 3, -3, 1, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        world.walls.add(new Wall(3, -3, -3, -3, 1, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        world.walls.add(new Wall(-3, 3, -3, -3, 1, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        world.walls.add(new Wall(3, 3, -3, 3, 1, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        Camera camera = new Camera(0, 0, 0, 0, 0);
        long oldTime = System.nanoTime();
        while (true) {
            long newTime = System.nanoTime();
            float delta = (newTime - oldTime) / 1000000000f;
            oldTime = newTime;
            time += delta;
            camera.r += delta * 16;
            engine.renderWorld(delta, world, camera);
        }
    }

}
