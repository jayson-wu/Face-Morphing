import edu.princeton.cs.algs4.Point2D;

public class LinearAlgebra {

    public static double[][] findAffineMatrix(Point2D[] shapet, Point2D[] shapef) {

        double[] m0 = { shapet[0].x(), shapet[0].y(), 1 };
        double[] m1 = { shapet[1].x(), shapet[1].y(), 1 };
        double[] m2 = { shapet[2].x(), shapet[2].y(), 1 };
        double[] m3 = { shapet[3].x(), shapet[3].y(), 1 };

        double[] t1x = { shapef[0].x(), shapef[1].x(), shapef[2].x() };
        double[] t1y = { shapef[0].y(), shapef[1].y(), shapef[2].y() };
        double[] t2x = { shapef[0].x(), shapef[2].x(), shapef[3].x() };
        double[] t2y = { shapef[0].y(), shapef[2].y(), shapef[3].y() };

        double[][] matrix1 = { m0, m1, m2 };
        double[][] matrix2 = { m0, m2, m3 };

        double[] sol1abe = solve(matrix1, t1x);
        double[] sol1cdf = solve(matrix1, t1y);
        double[] sol2abe = solve(matrix2, t2x);
        double[] sol2cdf = solve(matrix2, t2y);

        double[][] t = { sol1abe, sol1cdf, sol2abe, sol2cdf };
        return t;
    }

    // mc is invertible matrix, vc is equation
    private static double[] solve(double[][] mc, double[] vc) {

        double[][] m = Tools.copy(mc);
        double[] v = Tools.copy(vc);

        double[] ans = getEchelonForm(m, v);
        if (ans != null) return ans;

        getRREF(m, v);

        // correct for double error
        for (int i = 0; i < v.length; i++) {
            v[i] = Math.round(10e8 * v[i]) / 10e8;
        }

        return v;
    }

    private static void getRREF(double[][] m, double[] v) {
        for (int i = m.length - 1; i >= 0; i--) { // row and col standard
            double first = m[i][i];
            m[i][i] /= first;
            v[i] /= first;
            // print(m, v);
            for (int j = i - 1; j >= 0; j--) { // row operating
                if (m[j][i] == 0) continue;
                double factor = m[j][i] / m[i][i];
                m[j][i] -= factor * m[i][i];
                v[j] -= factor * v[i];
            }
        }
    }

    private static double[] getEchelonForm(double[][] m, double[] v) {
        for (int i = 0; i < m.length; i++) {
            // print(m, v);
            if (m[i][i] == 0) {
                boolean works = swapAddWhenCorrect(m, i, v);
                if (!works) {
                    double[] ans = { 0, 0, 0 };
                    return ans;
                }
            }
            for (int j = i + 1; j < m.length; j++) {
                if (m[j][i] == 0) continue;
                double factor = m[j][i] / m[i][i];
                for (int k = i; k < m[0].length; k++) {
                    m[j][k] -= factor * m[i][k];
                }
                v[j] -= factor * v[i];
            }
        }
        return null;
    }

    public static double[] affineMultAdd(int col, int row, double[][] m) {
        double[] v = new double[2];
        v[0] = col * m[0][0] + row * m[0][1] + m[0][2];
        v[1] = col * m[1][0] + row * m[1][1] + m[1][2];
        return v;
    }

    // multiply matrices
    public static double[][] multiply(double[][] m, double[][] n) {
        double[][] ans = new double[m.length][n[0].length];
        for (int rowm = 0; rowm < m.length; rowm++) {
            for (int coln = 0; coln < n[0].length; coln++) {
                for (int i = 0; i < m[0].length; i++) {
                    ans[rowm][coln] += m[rowm][i] * n[i][coln];
                }
            }
        }
        return ans;
    }

    private static boolean swapAddWhenCorrect(double[][] m, int i, double[] v) {
        int n = i + 1;
        if (n >= m.length) return false;
        while (m[n][i] == 0) {
            n++;
            if (n >= m.length) return false;
        }
        for (int j = i; j < m[0].length; j++) {
            m[i][j] += m[n][j];
        }
        v[i] += v[n];
        return true;
    }

    public static double[] bilinearInterpolation(int col, int row, Point2D[] shapef,
                                                 Point2D[] shapet) {

        double[][] mc = getVals(shapef, col, row);

        // swap rows
        if (mc[0][0] == 0) {
            mc = new double[][] { mc[1], mc[0] };
        }

        double firstsum = mc[0][0];
        double secondsum = mc[1][0];

        // get rid of uv term in second row
        if (secondsum != 0) {
            double product = -1 * firstsum / secondsum;
            for (int i = 0; i < mc[1].length; i++) {
                mc[1][i] = mc[1][i] * product + mc[0][i];
            }
        }

        double[] uv = solveForUV(mc);

        double u = uv[0];
        double v = uv[1];

        double[] ans = solveForXY(u, v, shapet);
        return ans;
    }

    private static double[] solveForXY(double u, double v, Point2D[] shapet) {
        double[][] um = {
                { 1 - u, u }
        };
        double[][] vm = {
                { 1 - v },
                { v }
        };
        double[][] abcdx = {
                { shapet[0].x(), shapet[3].x() },
                { shapet[1].x(), shapet[2].x() }
        };
        double[][] abcdy = {
                { shapet[0].y(), shapet[3].y() },
                { shapet[1].y(), shapet[2].y() }
        };

        double x = multiply(multiply(um, abcdx), vm)[0][0];
        double y = multiply(multiply(um, abcdy), vm)[0][0];

        double[] ans = { Math.round(x * 10e8) / 10e8, Math.round(y * 10e8) / 10e8 };

        return ans;
    }

    private static double[] solveForUV(double[][] mc) {
        double x0 = mc[0][0];
        double x1 = mc[0][1];
        double x2 = mc[0][2];
        double x3 = mc[0][3];
        double y1 = mc[1][1];
        double y2 = mc[1][2];
        double y3 = mc[1][3];

        double u;
        double v;
        if (y2 == 0) {
            u = y3 / y1;
            v = (x3 - u * x1) / (u * x0 + x2);
        }
        else if (y1 == 0) {
            v = y3 / y2;
            u = (x3 - v * x2) / (v * x0 + x1);
        }
        else {
            double a = -1 * y2 * x0 / y1;
            double b = (y3 * x0 - y2 * x1) / y1 + x2;
            double c = y3 * x1 / y1 - x3;

            v = solveQuadraticFormula(a, b, c);
            u = (y3 - y2 * v) / y1;
        }

        double[] uv = new double[2];
        uv[0] = u;
        uv[1] = v;
        return uv;

    }

    private static double[][] getVals(Point2D[] shapef, int col, int row) {
        double a1 = shapef[0].x();
        double a2 = shapef[0].y();
        double b1 = shapef[1].x();
        double b2 = shapef[1].y();
        double c1 = shapef[2].x();
        double c2 = shapef[2].y();
        double d1 = shapef[3].x();
        double d2 = shapef[3].y();

        double[][] mc = {
                { a1 - b1 + c1 - d1, b1 - a1, d1 - a1, col - a1 },
                { a2 - b2 + c2 - d2, b2 - a2, d2 - a2, row - a2 }
        };

        return mc;
    }

    private static double solveQuadraticFormula(double a, double b, double c) {
        double determinant = b * b - 4 * a * c;
        if (determinant < 0) throw new IllegalArgumentException("cannot do bilinear on neg. vals");
        double x1 = (-1 * b + Math.sqrt(determinant)) / (2 * a);
        double x2 = (-1 * b - Math.sqrt(determinant)) / (2 * a);
        if (x1 >= 0 && x1 <= 1) return x1;
        return x2;
        // throw new IllegalArgumentException("no possible values");
    }

    public static void main(String[] args) {

        int a = 1;
        int b = 1;

        Point2D[] shapet = {
                new Point2D(4, 1), new Point2D(9, 1),
                new Point2D(9, 8), new Point2D(5, 7)
        };

        Point2D[] shapef = {
                new Point2D(3, 1), new Point2D(6, 1),
                new Point2D(6, 7), new Point2D(1, 7)
        };

        double[] ans = bilinearInterpolation(6, 3, shapet, shapef);
        Tools.print(ans);
        /*
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) {
            double[] ans = bilinearInterpolation(in.nextInt(), in.nextInt(), shapet, shapef);
            Tools.print(ans);
        }

         */


    }
}
