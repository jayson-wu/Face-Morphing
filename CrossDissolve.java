import edu.princeton.cs.algs4.Picture;

import java.awt.Color;

public class CrossDissolve {

    private Picture from;
    private Picture to;

    public CrossDissolve(Picture from, Picture to) {
        this.from = from;
        this.to = to;
    }

    public void cdAlgorithm() {
        Picture now = new Picture(from);
        double s = 0.0075;
        for (double t = 0; t <= 1; t += s) {
            for (int col = 0; col < from.width(); col++) {
                for (int row = 0; row < from.height(); row++) {
                    Color n = getNewColor(col, row, t);
                    now.set(col, row, n);
                }
            }
            now.show();
        }
    }

    public Color getNewColor(int col, int row, double t) {
        if (col >= from.width() || col >= to.width() || row >= from.height() || row >= to.height())
            throw new IllegalArgumentException(
                    "the destination picture is smaller than the origin picture");
        Color fc = from.get(col, row);
        Color tc = to.get(col, row);
        int nr = (int) ((1 - t) * fc.getRed() + t * tc.getRed());
        int ng = (int) ((1 - t) * fc.getGreen() + t * tc.getGreen());
        int nb = (int) ((1 - t) * fc.getBlue() + t * tc.getBlue());
        return new Color(nr, ng, nb);
    }

    public static void main(String[] args) {
        Picture from = new Picture(args[0]);
        Picture to = new Picture(args[1]);

        CrossDissolve cd = new CrossDissolve(from, to);
        cd.cdAlgorithm();
    }
}
