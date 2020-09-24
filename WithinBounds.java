import edu.princeton.cs.algs4.Point2D;

public class WithinBounds {

    public static boolean check(int col, int row, Point2D[] shape) {
        if (corner(col, row, shape)) return true;
        double[] stats = findStats(shape);
        if (col >= stats[0] && col <= stats[1]
                && row >= stats[2] && row <= stats[3]) {
            int count = 0;
            for (int i = 0; i < shape.length; i++) {
                if (intersect(col, row, shape[i], shape[(i + 1) % shape.length])) count++;
            }
            if (intersectCorner(col, row, shape)) count--;
            if (row == (int) stats[2] && !line(shape, stats[2])) count--;
            if (row == (int) stats[3] && !line(shape, stats[3])) count--;
            if (count == 1) return true;
        }
        return false;
    }

    private static boolean line(Point2D[] shape, double minY) {
        int count = 0;
        for (Point2D p : shape) {
            if (p.y() == minY) count++;
        }
        if (count == 2) return true;
        return false;
    }

    private static boolean intersectCorner(int col, int row, Point2D[] shape) {
        for (Point2D p : shape) {
            if (row == p.y() && col < p.x())
                return true;
        }
        return false;
    }

    private static boolean corner(int col, int row, Point2D[] shape) {
        for (Point2D p : shape) {
            if (col == p.x() && row == p.y())
                return true;
        }
        return false;
    }

    // {minX, maxX, minY, maxY}
    private static double[] findStats(Point2D[] shape) {
        double[] stats = new double[4];
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < shape.length; i++) {
            double x = shape[i].x();
            double y = shape[i].y();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        stats[0] = minX;
        stats[1] = maxX;
        stats[2] = minY;
        stats[3] = maxY;
        return stats;
    }

    private static boolean intersect(int x, int y, Point2D p1, Point2D p2) {
        // check if in bounds
        if (y >= Math.min(p1.y(), p2.y()) && y <= Math.max(p1.y(), p2.y())) {
            // if straight line
            if (p1.x() - p2.x() == 0) {
                if (x <= p1.x()) return true;
                return false;
            }
            // check whether on left or right side of line
            double m = (p1.y() - p2.y()) / (p1.x() - p2.x()); // find slope
            double b = p2.y() - m * p2.x(); // find intercept
            double liney = m * x + b;
            if (m >= 0 && liney <= y) return true;
            if (m < 0 && liney > y) return true;
        }
        return false;
    }

    public static void main(String[] args) {


        int a = 1;
        int b = 1;

        Point2D[] coordinatesfrom = {
                new Point2D(a, b), new Point2D(6 * a, b),
                new Point2D(6 * a, 7 * b), new Point2D(a, 7 * b)
        };

        Point2D[] coordinatesto = new Point2D[] {
                new Point2D(4 * a, b), new Point2D(9 * a, b),
                new Point2D(9 * a, 7 * b), new Point2D(4 * a, 7 * b)
        };

        Point2D[] testcoords = new Point2D[] {
                new Point2D(9 * a, b), new Point2D(12 * a, 0),
                new Point2D(12 * a, 10 * b), new Point2D(9 * a, 7 * b)
        };

        System.out.println(check(11, 4, testcoords));

        /*
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            System.out.println(check(in.nextInt(), in.nextInt(), coordinatesto));
        }

         */

    }
}
