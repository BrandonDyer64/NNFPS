package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.LinkedList;

public class FrostByte {

    public JFrame frame;
    public Canvas canvas;
    public float fov = 50f;
    public float renderDistance = 2000;
    public float clipDistance = 0;

    public FrostByte(String title) {
        this(title, 640, 400);
    }

    public FrostByte(String title, int width, int height) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.setLocationRelativeTo(null);
        frame.add(canvas = new Canvas());
    }

    public void renderWorld(float delta, World world, Camera camera) {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderWorld(delta, world, camera, canvas.getWidth(), canvas.getHeight(), g);

        g.dispose();
        bs.show();
    }

    public void renderWorld(float delta, World world, Camera camera, int width, int height, Graphics2D g) {
        for (int pixel = 0; pixel < width; pixel++) {
            float nearest = renderDistance;
            Wall selectedWall = null;
            double dir = Math.toRadians(camera.r + (-width / 2 + pixel) * (fov / height));
            for (Wall wall : world.walls) {
                float dist = getRayCast(camera.x, camera.y, camera.x + (float) Math.cos(dir) * renderDistance, camera.y + (float) Math.sin(dir) * renderDistance, wall.x1, wall.y1, wall.x2, wall.y2);
                if (dist < nearest && dist > clipDistance) {
                    nearest = dist;
                    selectedWall = wall;
                }
            }
            if (selectedWall != null) {
                int h = (int) ((height / nearest) * selectedWall.height);
                int he = (int) (height / nearest);
                int y = height / 2 + he / 2 - (int) camera.p;
                RenderLine renderLine = new RenderLine(camera.x + (float) Math.cos(dir) * nearest, camera.y + (float) Math.sin(dir) * nearest, pixel, y, 1, h, nearest, Color.WHITE, camera);
                if (selectedWall.shader != null) {
                    renderLine = selectedWall.shader.shade(renderLine);
                }
                if (world.shader != null) {
                    renderLine = world.shader.shade(renderLine);
                }
                renderLine.draw(g);
            }
        }
    }


    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public static float getRayCast(float p0_x, float p0_y, float p1_x, float p1_y, float p2_x, float p2_y, float p3_x, float p3_y) {
        float s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;
        s2_y = p3_y - p2_y;

        float s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // Collision detected
            float x = p0_x + (t * s1_x);
            float y = p0_y + (t * s1_y);

            return dist(p0_x, p0_y, x, y);
        }

        return -1; // No collision
    }
}
