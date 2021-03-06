import engine.*;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by brandon on 10/12/2016.
 */
public class Driver {

    public static float time = 0;
    public static int mouseX = 0, mouseY = 0;
    public static boolean moveForward, moveBackward, moveLeft, moveRight, walk, crouch, shoot, jump;
    public static float runspeed = 2.0f, walkspeed = 1f, crouchspeed = 0.8f;
    public static float shot = 0, shotTime = 0;
    public static MLP network = new MLP(3, new int[]{20, 4});
    public static LinkedList<Player> alpha = new LinkedList<>(), beta = new LinkedList<>();
    public static Player player = new Player(0, 0, 0);
    public static Random random = new Random();

    public static void main(String[] args) {
        Frost engine = new Frost("Test Game");
        engine.frame.setVisible(true);
        World world = new World();
        world.shader = line -> {
            line.distance++;
            line.color = new Color((int) ((255 / line.distance) * (line.color.getRed() / 255f)), (int) ((255 / line.distance) * (line.color.getGreen() / 255f)), (int) ((255 / line.distance) * (line.color.getBlue() / 255f)));
            //line.color = new Color((int)(line.distance * 255) % 256, (int) (line.distance * 255 * 2) % 256, (int) (line.distance * 255 * 4) % 256);
            //line.color = new Color(line.distance - 1 <= engine.priorityDistance ? 0 : 255, 0, line.distance - 1 <= engine.priorityDistance ? 255 : 0);
            line.height += line.height * (Math.sin(line.wY * 10 + time) + Math.cos(line.wX * 10 + time)) * 0.1f;
            //line.height += (Math.cos(line.wX * 10 + time) + Math.sin(line.wY * 10 + time)) * 20;
            return line;
        };
        world.walls.add(new Wall(3, 3, 3, -3, 1, 5, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        world.walls.add(new Wall(3, -3, -3, -3, 1, 5, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        world.walls.add(new Wall(-3, 3, -3, -3, 1, 5, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        world.walls.add(new Wall(3, 3, -3, 3, 1, 5, line -> {
            line.color = Color.WHITE;
            return line;
        }));
        world.walls.add(new Wall(0.05f, 0.05f, 0.05f, -0.05f, 0.01f, line -> {
            line.color = Color.GREEN;
            return line;
        }));
        world.walls.add(new Wall(0.05f, -0.05f, -0.05f, -0.05f, 0.01f, line -> {
            line.color = Color.GREEN;
            return line;
        }));
        world.walls.add(new Wall(-0.05f, 0.05f, -0.05f, -0.05f, 0.01f, line -> {
            line.color = Color.GREEN;
            return line;
        }));
        world.walls.add(new Wall(0.05f, 0.05f, -0.05f, 0.05f, 0.01f, line -> {
            line.color = Color.GREEN;
            return line;
        }));
        Camera camera = new Camera(0, -2, 0, 0, 0);

        alpha.add(player);
        Player p = new Player(0, 0, 0);
        beta.add(p);
        world.walls.add(p.walls[0]);
        world.walls.add(p.walls[1]);


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
                    case KeyEvent.VK_SPACE:
                        jump = true;
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
                    case KeyEvent.VK_SPACE:
                        jump = false;
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
                camera.r = mouseX / 2f;
                camera.p = (mouseY - engine.canvas.getHeight() / 2) * 4 + shot;
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
                    camera.z = -1f;
                } else if (jump){
                    camera.z = 8;
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
                if (moveRight) {
                    camera.x += Math.cos(Math.toRadians(camera.r + 90)) * speed * delta / 2;
                    camera.y += Math.sin(Math.toRadians(camera.r + 90)) * speed * delta / 2;
                }
                if (moveLeft) {
                    camera.x -= Math.cos(Math.toRadians(camera.r + 90)) * speed * delta / 2;
                    camera.y -= Math.sin(Math.toRadians(camera.r + 90)) * speed * delta / 2;
                }
                p.move(delta, engine, network, alpha);
                player.train(delta, engine, network, beta, camera.x, camera.y, camera.r, (moveForward ? 1 : 0) - (moveBackward ? 1 : 0), (moveRight ? 1 : 0) - (moveLeft ? 1 : 0));
                shot = Maths.lerp(shot, crouch ? 3 : 0, 0.05f, delta);
                if (shoot && shotTime > 0.5f) {
                    shot -= 50;
                    shotTime = 0;
                    shoot(engine, beta, player);
                }
            }
            engine.renderWorld(delta, world, camera);

        }
    }

    public static void shoot(Frost engine, LinkedList<Player> enemies, Player player) {
        float nearest = engine.renderDistance;
        Player closest = null;
        double dir = Math.toRadians(player.r);
        for (Player p : enemies) {
            for (Wall wall : p.walls) {
                float dist = Frost.getRayCast(player.x, player.y, player.x + (float) Math.cos(dir) * engine.renderDistance, player.y + (float) Math.sin(dir) * engine.renderDistance, wall.x1, wall.y1, wall.x2, wall.y2);
                if (dist < nearest && dist > engine.clipDistance) {
                    nearest = dist;
                    closest = p;
                }
            }
        }
        if (closest != null) {
            closest.kill();
        }
    }

}
