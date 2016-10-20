package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.*;

public class Frost {

    public JFrame frame;
    public Canvas canvas;
    public float fov = 50f;
    public float renderDistance = 2000;
    public float priorityDistance = 2f;
    public float clipDistance = 0.1f;
    public int renderLayers = 32;

    public Render renderPre = null;
    public Render renderPost = null;

    public Frost(String title) {
        this(title, 640, 400);
    }

    public Frost(String title, int width, int height) {
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

        if (renderPre != null) {
            renderPre.render(g);
        }

        renderWorldAdvanced(delta, world, camera, canvas.getWidth(), canvas.getHeight(), g);

        if (renderPost != null) {
            renderPost.render(g);
        }

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
                int y = height / 2 + he / 2 - (int) camera.p + (int) ((camera.z * 2000) / (nearest * 10 + 1));
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

    public void renderWorldAdvanced(float delta, World world, Camera camera, int width, int height, Graphics2D g) {
        for (int pixel = 0; pixel < width; pixel++) {
            HashMap<Float, Wall> walls = new HashMap<>();
            double dir = Math.toRadians(camera.r + (-width / 2 + pixel) * (fov / height));
            for (Wall wall : world.walls) {
                float dist = getRayCast(camera.x, camera.y, camera.x + (float) Math.cos(dir) * renderDistance, camera.y + (float) Math.sin(dir) * renderDistance, wall.x1, wall.y1, wall.x2, wall.y2);
                if (dist > clipDistance)
                    walls.put(dist, wall);
            }
            int i = walls.keySet().size();
            boolean forceDraw = false;
            for (Object disto : Maths.sortReverse(walls.keySet())) {
                Float dist = (Float) disto;
                Wall wall = walls.get(dist);
                i--;
                if ((i > renderLayers && dist > priorityDistance && dist > wall.priorityDistance) && !forceDraw)
                    continue;
                forceDraw = dist <= priorityDistance;
                int h = (int) ((height / dist) * wall.height);
                int he = (int) (height / dist);
                int y = height / 2 + he / 2 - (int) camera.p + (int) ((camera.z * 2000) / (dist * 10 + 1));
                RenderLine renderLine = new RenderLine(camera.x + (float) Math.cos(dir) * dist, camera.y + (float) Math.sin(dir) * dist, pixel, y, 1, h, dist, Color.WHITE, camera);
                if (wall.shader != null) {
                    renderLine = walls.get(dist).shader.shade(renderLine);
                }
                if (world.shader != null) {
                    renderLine = world.shader.shade(renderLine);
                }
                renderLine.draw(g);
            }
        }
    }

    public float[] renderWorld(World world, Camera camera, int width, int height) {
        float[] out = new float[width];
        for (int pixel = 0; pixel < width; pixel++) {
            float nearest = renderDistance;
            double dir = Math.toRadians(camera.r + (-width / 2 + pixel) * (fov / height));
            for (Wall wall : world.walls) {
                float dist = getRayCast(camera.x, camera.y, camera.x + (float) Math.cos(dir) * renderDistance, camera.y + (float) Math.sin(dir) * renderDistance, wall.x1, wall.y1, wall.x2, wall.y2);
                if (dist < nearest && dist > clipDistance) {
                    nearest = dist;
                }
            }
            out[pixel] = nearest;
        }
        return out;
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

    public interface Render {
        void render(Graphics g);
    }

}
