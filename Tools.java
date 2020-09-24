import edu.princeton.cs.algs4.Point2D;

public class Tools {

    public static void print(double[][] m, double[] v) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                System.out.print(m[i][j] + " ");
            }
            System.out.println(v[i]);
        }
        System.out.println();
    }

    public static void print(double[][] m) {
        for (double[] i : m) {
            for (double j : i) {
                System.out.print(j + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void print(double[] v) {
        for (double i : v) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public static void print(Point2D[] v) {
        for (Point2D i : v) {
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public static double[][] copy(double[][] mc) {
        double[][] m = new double[mc.length][mc[0].length];
        for (int i = 0; i < mc.length; i++) {
            for (int j = 0; j < mc[0].length; j++) {
                m[i][j] = mc[i][j];
            }
        }
        return m;
    }

    public static double[] copy(double[] tc) {
        double[] t = new double[tc.length];
        for (int j = 0; j < tc.length; j++) {
            t[j] = tc[j];
        }
        return t;
    }

    public static void main(String[] args) {

    }
}
