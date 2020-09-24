import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Point2D;

import java.awt.Color;
import java.util.ArrayList;

public class Morphing {

    public static final double SPEED = 50;
    public ArrayList<Point2D[]> shapesfrom;
    public ArrayList<Point2D[]> shapesto;
    private Picture from;
    private Picture to;
    private Picture morphed;
    private String transform;

    public Morphing(Point2D[] cf, Point2D[] ct, Picture f, Picture t, String transform) {
        from = new Picture(f);
        to = new Picture(t);
        morphed = new Picture(from);
        this.transform = transform;

        /*
        if (from.height() != to.height() || from.width() != to.width())
            throw new IllegalArgumentException(
                    "origin and destination picture do not have same dimensions");

         */

        shapesfrom = makeShapesList(cf, from);
        shapesto = makeShapesList(ct, to);
    }

    public void morphingAlgorithm() {

        transform();

        morphed.show();

    }

    private void transform() {
        ArrayList<double[][]> transformmatrices = new ArrayList<>();
        for (int i = 0; i < shapesfrom.size(); i++) {
            Point2D[] shapef = shapesfrom.get(i);
            Point2D[] shapet = shapesto.get(i);
            if (transform.equals("affine")) {
                double[][] t = LinearAlgebra.findAffineMatrix(shapet, shapef);
                if (t == null) throw new IllegalArgumentException("transform matrix null");
                transformmatrices.add(t);
            }
        }
        mapCoordinates(transformmatrices);
    }

    private void mapCoordinates(ArrayList<double[][]> ts) {

        for (int t = 0; t <= SPEED; t++) {
            // int t = (int) SPEED;
            for (int col = 0; col < from.width(); col++) {
                for (int row = 0; row < from.height(); row++) {
                    Color c = null;
                    if (transform.equals("affine")) {
                        c = projectionColor(col, row, ts, t);
                    }
                    else if (transform.equals("bilinear")) {
                        c = bilinearColor(col, row, t);
                    }

                    if (c != null) morphed.set(col, row, c);

                    CrossDissolve cd2 = new CrossDissolve(morphed, to);
                    Color cn = cd2.getNewColor(col, row, (double) t / SPEED);
                    morphed.set(col, row, cn);

                    morphed.show();
                }
            }
        }
    }

    private Color bilinearColor(int col, int row, int time) {
        for (int s = 0; s < shapesfrom.size(); s++) {
            Point2D[] shapef = shapesfrom.get(s);
            Point2D[] shapet = shapesto.get(s);

            double[] transformedVector = null;
            boolean b = WithinBounds.check(col, row, shapet);
            // if (col == 120 && row == 60) {
            //     System.out.println(b);
            // }
            if (b) {
                transformedVector = findBilinearVector(col, row, shapef, shapet);
                // if (col == 120 && row == 60) {
                //     Tools.print(shapef);
                //     Tools.print(shapet);
                // }
            }

            if (transformedVector != null) {
                return setTime(transformedVector, col, row, time);
            }
        }
        return null;
    }

    private double[] findBilinearVector(int col, int row, Point2D[] shapef, Point2D[] shapet) {
        double[] vector = LinearAlgebra.bilinearInterpolation(col, row, shapet, shapef);
        if (vector[0] < from.width() && vector[0] >= 0
                && vector[1] < from.height() && vector[1] >= 0) {
            return vector;
        }
        return null;
    }

    private Color projectionColor(int col, int row, ArrayList<double[][]> ts, int time) {
        for (int s = 0; s < shapesto.size(); s++) {
            Point2D[] shapet = shapesto.get(s);
            Point2D[] shapet1 = { shapet[0], shapet[1], shapet[2] };
            Point2D[] shapet2 = { shapet[0], shapet[2], shapet[3] };

            double[][] t = ts.get(s);
            double[][] m1 = { t[0], t[1] };
            double[][] m2 = { t[2], t[3] };

            double[] transformedVector = null;
            if (WithinBounds.check(col, row, shapet1)) {
                transformedVector = findProjectiveVector(col, row, m1);
            }
            else if (WithinBounds.check(col, row, shapet2)) {
                transformedVector = findProjectiveVector(col, row, m2);
            }

            if (transformedVector != null) {
                return setTime(transformedVector, col, row, time);
            }

        }
        return null;
    }

    private Color setTime(double[] transformedVector, int col, int row, int time) {
        double[] v = { col, row };
        v[0] += (transformedVector[0] - col) * (double) time / SPEED;
        v[1] += (transformedVector[1] - row) * (double) time / SPEED;
        return (from.get((int) v[0], (int) v[1]));
    }

    private double[] findProjectiveVector(int col, int row, double[][] m) {
        double[] vector = LinearAlgebra.affineMultAdd(col, row, m);
        if (vector[0] < from.width() && vector[0] >= 0
                && vector[1] < from.height() && vector[1] >= 0) {
            return vector;
        }
        return null;
    }

    private ArrayList<Point2D[]> makeShapesList(Point2D[] coords, Picture pic) {
        ArrayList<Point2D[]> shapes = new ArrayList<>();
        Point2D lt = new Point2D(0, 0);
        Point2D rt = new Point2D(pic.width(), 0);
        Point2D lb = new Point2D(0, pic.height());
        Point2D rb = new Point2D(pic.width(), pic.height());
        Point2D[] shape1 = { lt, coords[0], coords[3], lb };
        Point2D[] shape2 = { lt, rt, coords[1], coords[0] };
        Point2D[] shape3 = { rt, rb, coords[2], coords[1] };
        Point2D[] shape4 = { lb, coords[3], coords[2], rb };
        Point2D[] shape5 = { coords[0], coords[1], coords[2], coords[3] };
        shapes.add(shape1);
        shapes.add(shape2);
        shapes.add(shape3);
        shapes.add(shape4);
        shapes.add(shape5);
        return shapes;
    }

    public static void main(String[] args) {
        // read input

        String from = args[0];
        String to = args[1];
        String transform = args[2];
        Picture pfrom = new Picture(from);
        Picture pto = new Picture(to);

        FeatureFinder ff = new FeatureFinder(from);
        FeatureFinder ft = new FeatureFinder(to);

        Point2D[] coordinatesfrom = ff.findFeatures();
        Point2D[] coordinatesto = ft.findFeatures();

        Morphing m = new Morphing(coordinatesfrom, coordinatesto, pfrom, pto, transform);
        m.morphingAlgorithm();
    }
}
