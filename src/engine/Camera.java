package engine;

/**
 * Created by brandon on 9/21/2016.
 */
public class Camera {

    public float x, y, z, p, r;

    public Camera(float x, float y, float z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.p = pitch;
        this.r = yaw;
    }

}
