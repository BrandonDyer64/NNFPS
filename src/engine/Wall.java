package engine;

/**
 * Created by brandon on 9/21/2016.
 */
public class Wall {

    public float x1, y1, x2, y2, height;
    public float priorityDistance = 1f;

    public Shader shader;

    public Wall(float x1, float y1, float x2, float y2, float height, Shader shader) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.height = height;
        this.shader = shader;
    }

    public Wall(float x1, float y1, float x2, float y2, float height, float priorityDistance, Shader shader) {
        this(x1, y1, x2, y2, height, shader);
        this.priorityDistance = priorityDistance;
    }

    public interface Shader {
        public RenderLine shade(RenderLine line);
    }

}
