package engine;

import java.util.LinkedList;

/**
 * Created by brandon on 9/21/2016.
 */
public class World {

    public LinkedList<Wall> walls;

    public Wall.Shader shader;

    public World() {
        this(new LinkedList<>());
    }

    public World(LinkedList<Wall> walls) {
        this.walls = walls;
    }

}
