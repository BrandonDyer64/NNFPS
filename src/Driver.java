import engine.Camera;
import engine.FrostByte;
import engine.Wall;
import engine.World;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Created by brandon on 10/12/2016.
 */
public class Driver {

    public static float time = 0;
    public static int mouseX = 0, mouseY = 0;
    public static boolean moveForward, moveBackward, moveLeft, moveRight, walk, crouch;
    public static float runspeed = 2.0f, walkspeed = 1f, crouchspeed = 0.8f;

    public static void main(String[] args) {
        FrostByte engine = new FrostByte("Test Game");
        engine.frame.setVisible(true);
        World world = new World();
        world.shader = line -> {
            line.distance++;
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

        engine.renderPost = (g) -> {
            g.setColor(Color.BLACK);
            g.fillRect(engine.canvas.getWidth()/2-2,engine.canvas.getHeight()/2-2,4,4);
            g.setColor(Color.WHITE);
            g.fillRect(engine.canvas.getWidth()/2-1,engine.canvas.getHeight()/2-1,2,2);
        };

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

        long oldTime = System.nanoTime();
        while (true) {
            long newTime = System.nanoTime();
            float delta = (newTime - oldTime) / 1000000000f;
            oldTime = newTime;
            time += delta;
            {
                camera.r = mouseX / 5f;
                camera.p = (mouseY - engine.canvas.getHeight() / 2) * 2;
                float speed;
                if (walk) {
                    speed = walkspeed;
                } else if (crouch) {
                    speed = crouchspeed;
                } else {
                    speed = runspeed;
                }
                if (crouch) {
                    camera.z = -1f;
                } else {
                    camera.z = 0;
                }
                if (moveForward) {
                    camera.x += Math.cos(Math.toRadians(camera.r)) * speed * delta;
                    camera.y += Math.sin(Math.toRadians(camera.r)) * speed * delta;
                }
                if (moveBackward) {
                    camera.x -= Math.cos(Math.toRadians(camera.r)) * speed * delta;
                    camera.y -= Math.sin(Math.toRadians(camera.r)) * speed * delta;
                }
            }
            engine.renderWorld(delta, world, camera);

        }
    }

}
