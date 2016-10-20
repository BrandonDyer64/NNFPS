package engine;

import java.util.Arrays;
import java.util.Set;

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

    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static <K> Object[] sortReverse(Set<K> set) {
        Object[] ks = set.toArray();
        Arrays.sort(ks);
        Object[] newKs = new Object[ks.length];
        for (int i = 0; i < ks.length; i++) {
            newKs[ks.length - i - 1] = ks[i];
        }
        return newKs;
    }

}
