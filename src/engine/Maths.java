package engine;

/**
 * Created by brandon on 10/12/2016.
 */
public class Maths {

    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }

    public static float lerp(float v0, float v1, float t, float dt) {
        return lerp(v0, v1, 1 - (float) Math.pow(t, dt));
    }

}
