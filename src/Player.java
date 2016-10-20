import engine.*;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created by brandon on 10/18/2016.
 */
public class Player {

    public float x, y, r;
    public Wall[] walls = new Wall[2];
    public static final float GURTH = 0.1f;
    public float h = 0;

    public static final Wall.Shader shader = line -> {
        line.color = Color.ORANGE;
        return line;
    };

    public Player(float x, float y, float r) {
        this.x = x;
        this.y = y;
        this.r = r;
        walls[0] = new Wall(x, y, x, y, 0, shader);
        walls[1] = new Wall(x, y, x, y, 0, shader);
        renderWalls();
    }

    public void renderWalls() {
        walls[0].x1 = x - GURTH;
        walls[0].x2 = x + GURTH;
        walls[0].y1 = y;
        walls[0].y2 = y;
        walls[1].x1 = x;
        walls[1].x2 = x;
        walls[1].y1 = y + GURTH;
        walls[1].y2 = y - GURTH;
        walls[0].height = h;
        walls[1].height = h;
    }

    public void move(float delta, Frost engine, MLP network, LinkedList<Player> enemies) {
        if (Driver.time < 5) {
            return;
        }
        h = Maths.lerp(h, 1f, 0.05f, delta);
        // Get Players
        Player closest = enemies.getFirst();
        float nearest = engine.renderDistance;
        for (Player player : enemies) {
            float dist = Maths.dist(x, y, player.x, player.y);
            if (dist < nearest) {
                nearest = dist;
                closest = player;
            }
        }
        double dir = Math.atan2(closest.y - y, closest.x - x) - r;
        float sin = (float) Math.sin(dir);
        float cos = (float) Math.cos(dir);

        // Run
        float[] ins = new float[3];

        ins[0] = sin;
        ins[1] = cos;

        float[] out = network.run(ins);
        float speed = Driver.runspeed;
        if (h > 0.9) {
            r += out[2];
            x += Math.cos(Math.toRadians(r)) * out[0] * speed * delta;
            y += Math.sin(Math.toRadians(r)) * out[0] * speed * delta;
            x += Math.cos(Math.toRadians(r + 90)) * out[1] * speed * delta / 2;
            y += Math.sin(Math.toRadians(r + 90)) * out[1] * speed * delta / 2;
        }
        if (x > 3)
            x = -3;
        if (x < -3)
            x = 3;
        if (y > 3)
            y = -3;
        if (y < -3)
            y = 3;
        renderWalls();
    }

    public void train(float delta, Frost engine, MLP network, LinkedList<Player> enemies, float x, float y, float r, float forward, float right) {
        // Get Players
        Player closest = enemies.getFirst();
        float nearest = engine.renderDistance;
        for (Player player : enemies) {
            float dist = Maths.dist(x, y, player.x, player.y);
            if (dist < nearest) {
                nearest = dist;
                closest = player;
            }
        }
        double dir = Math.atan2(closest.y - y, closest.x - x) - r;
        float sin = (float) Math.sin(dir);
        float cos = (float) Math.cos(dir);

        // Run
        float[] ins = new float[3];

        ins[0] = sin;
        ins[1] = cos;

        float[] outs = new float[4];

        outs[0] = forward;
        outs[1] = right;
        outs[2] = r - this.r;
        outs[3] = Driver.shoot ? 1 : 0;

        if (Driver.random.nextInt(5) == 0 || Driver.shotTime <= delta * 2)
            network.train(ins, outs, 0.0005f, 0.6f);
        float speed = Driver.runspeed;
        this.x = x;
        this.y = y;
        this.r = r;
        renderWalls();
    }

    public void kill() {
        x = 0;
        y = 0;
        h = 0;
        r = 0;
    }

}
