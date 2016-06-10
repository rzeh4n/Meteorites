package rzeh4n.meteorite;

/**
 * Created by Martin Řehánek on 10.6.16.
 */
public class Utils {

    public static String formatMass(int massGrams) {
        if (massGrams < 1000) {
            return String.format("%d g", massGrams);
        } else if (massGrams % 1000 == 0) {
            return String.format("%d kg", massGrams / 1000);
        } else {
            return String.format("%.3f kg", massGrams / 1000f);
        }
    }

}
