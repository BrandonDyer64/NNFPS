package race;

import engine.*;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by brandon on 10/12/2016.
 */
public class DriverRace {

    public static float time = 0, snapTime = 0;
    public static int mouseX = 0, mouseY = 0;
    public static boolean moveForward, moveBackward, moveLeft, moveRight, walk, crouch, shoot;
    public static float runspeed = 5.0f, walkspeed = 1f, crouchspeed = 10f;
    public static float shot = 0, shotTime = 0;
    public static boolean useAI = false, runSnapshots = false;
    public static MLP network = new MLP(3, new int[]{30, 2, 1});
    public static Random random = new Random();
    public static LinkedList<Snapshot> snapshots = new LinkedList<>();

    public static void main(String[] args) {
        Frost engine = new Frost("Test Game", 1000, 600);
        engine.frame.setVisible(true);
        World world = new World();
        world.shader = line -> {
            line.distance++;
            line.color = new Color((int) ((255 / line.distance) * (line.color.getRed() / 255f)), (int) ((255 / line.distance) * (line.color.getGreen() / 255f)), (int) ((255 / line.distance) * (line.color.getBlue() / 255f)));
            //line.height += (Math.cos(line.wX * 10 + time) + Math.sin(line.wY * 10 + time)) * 20;
            return line;
        };
        generateTrack(world);
        Camera camera = new Camera(-4, 4, 0, 0, 0);

        engine.renderPost = (g) -> {
            g.setColor(Color.GREEN);
            g.fillRect(engine.canvas.getWidth() / 2 - 1, engine.canvas.getHeight() / 2 - 1, 2, 2);
            g.fillRect(engine.canvas.getWidth() / 2 - 6 + (int) shot, engine.canvas.getHeight() / 2 - 2, 1, 4);
            g.fillRect(engine.canvas.getWidth() / 2 + 5 - (int) shot, engine.canvas.getHeight() / 2 - 2, 1, 4);
        };

        engine.renderPre = (g) -> {
            g.setColor(new Color(255, 150, 0));
            //g.fillRect(0,engine.canvas.getHeight()/2,engine.canvas.getWidth(),engine.canvas.getHeight()/2);
        };

        engine.canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                shoot = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                shoot = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        engine.canvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        engine.canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_W:
                        moveForward = true;
                        break;
                    case KeyEvent.VK_S:
                        moveBackward = true;
                        break;
                    case KeyEvent.VK_A:
                        moveLeft = true;
                        break;
                    case KeyEvent.VK_D:
                        moveRight = true;
                        break;
                    case KeyEvent.VK_SHIFT:
                        walk = true;
                        break;
                    case KeyEvent.VK_CONTROL:
                        crouch = true;
                        break;
                    case KeyEvent.VK_P:
                        useAI ^= true;
                        break;
                    case KeyEvent.VK_O:
                        runSnapshots = true;
                        break;
                    case KeyEvent.VK_C:
                        camera.x = -4;
                        camera.y = 4;
                        camera.r = 0;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_W:
                        moveForward = false;
                        break;
                    case KeyEvent.VK_S:
                        moveBackward = false;
                        break;
                    case KeyEvent.VK_A:
                        moveLeft = false;
                        break;
                    case KeyEvent.VK_D:
                        moveRight = false;
                        break;
                    case KeyEvent.VK_SHIFT:
                        walk = false;
                        break;
                    case KeyEvent.VK_CONTROL:
                        crouch = false;
                        break;
                }
            }
        });

        network.layers[network.layers.length - 1].setIsSigmoid(false);

        long oldTime = System.nanoTime();
        while (true) {
            long newTime = System.nanoTime();
            float delta = (newTime - oldTime) / 1000000000f;
            oldTime = newTime;
            time += delta;
            snapTime += delta;
            if (time > 3f) {
                int tX, tY;
                float[] nnin = engine.renderWorld(world, camera, 3, 3);
                int nX = (int) (network.run(nnin)[0]);
                if (useAI) {
                    tX = nX;
                    tY = 0;
                } else {
                    tX = mouseX - engine.canvas.getWidth() / 2;
                    if (snapTime > 0.5f) {
                        snapTime = 0;
                        snapshots.add(new Snapshot(nnin, tX));
                        System.out.println("Snapshot taken. I: " + snapshots.size() + "\tError: " + Math.abs(tX - nX));
                    }
                    tY = mouseY - engine.canvas.getHeight() / 2;
                }
                if (runSnapshots) {
                    runSnapshots();
                    runSnapshots = false;
                }
                camera.r += tX * delta;
                camera.p = (tY) * 2 + shot;
                shotTime += delta;
                float speed;
                if (walk) {
                    speed = walkspeed;
                } else if (crouch) {
                    speed = crouchspeed;
                } else {
                    speed = runspeed;
                }
                if (crouch) {
                    camera.z = 10f;
                } else {
                    camera.z = 0;
                }
                camera.x += Math.cos(Math.toRadians(camera.r)) * speed * delta;
                camera.y += Math.sin(Math.toRadians(camera.r)) * speed * delta;
                shot = Maths.lerp(shot, crouch ? 3 : 0, 0.05f, delta);
            }
            engine.renderWorld(delta, world, camera);

        }
    }

    public static void runSnapshots() {
        for (int i = 0; i < 2400; i++) {
            for (Snapshot snapshot : snapshots) {
                network.train(snapshot.ins, new float[]{snapshot.out}, 0.1f, 2f);
            }
        }
        snapshots.clear();
    }

    public static void generateTrack(World world) {
        float size = 6f;
        // Inner Square 1
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                world.walls.add(new Wall(i * size - size / 2, i * size - size / 2, j * size - size / 2, size - j * size - size / 2, 0.2f, line -> {
                    line.color = Color.WHITE;
                    return line;
                }));
            }
        }
        // Inner Square 2
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                world.walls.add(new Wall(size + i * size - size / 2, size + i * size - size / 2, size + j * size - size / 2, size - j * size + size - size / 2, 0.2f, line -> {
                    line.color = Color.WHITE;
                    return line;
                }));
            }
        }
        size *= 2;
        // Inner Square 1
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                world.walls.add(new Wall(i * (i == 1 && j == 0 ? 0.5f : 1) * size - size / 2, i * (i == 1 && j == 1 ? 0.5f : 1) * size - size / 2, j * size - size / 2, size - j * size - size / 2, 0.2f, line -> {
                    line.color = Color.WHITE;
                    return line;
                }));
            }
        }
        // Inner Square 2
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                world.walls.add(new Wall(size + i * size - size + (i == 0 && j == 1 ? 0.5f : 0) * (size), size + i * size - size + (i == 0 && j == 0 ? 0.5f : 0) * (size), size + j * size - size, size - j * size + size - size, 0.2f, line -> {
                    line.color = Color.WHITE;
                    return line;
                }));
            }
        }

    }

}
