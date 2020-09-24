/*******************************************************************************
 *  Name:    Jayson Wu
 *  NetID:   jnwu
 *
 *  Partner Name:    Connie Xu
 *  Partner NetID:   clxu
 *
 *  Description:  Finds the eyes and mouth in a image. findFeatures outputs
 *                a queue that contains the eyes (first 2 points), the left and
 *                right edgepoints of the mouth (3rd and 4th points), and
 *                edges of the picture (last 4 points).
 *
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdOut;

import java.awt.Color;

public class FeatureFinder {
    private Picture pic;
    private double[][] energy;
    private double[][] red;

    public FeatureFinder(String file) {
        this.pic = new Picture(file);
        energy = new double[pic.width()][pic.height()];
        for (int i = 0; i < pic.width(); i++) {
            for (int j = 0; j < pic.height(); j++) {
                // priority function for middle of the screen
                double yratio = (double) j / height();
                double xratio = (double) i / width();
                double prio = 50 * Math.sin(Math.PI * yratio) +
                        50 * Math.sin(Math.PI * xratio);
                energy[i][j] = energy(i, j) + prio;
            }
        }
        red = new double[pic.width()][pic.height()];
        for (int i = 0; i < pic.width(); i++) {
            for (int j = 0; j < pic.height(); j++) {
                Color color = pic.get(i, j);
                double yratio = (double) j / height();
                double xratio = (double) i / width();
                double prio = 60 * Math.sin(0.5 * Math.PI * yratio) +
                        300 * Math.sin(Math.PI * xratio);
                red[i][j] = color.getRed() + 256 - color.getGreen() + prio;
            }
        }
    }

    // width of the image
    public int width() {
        return pic.width();
    }

    // height of the image
    public int height() {
        return pic.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height())
            throw new IllegalArgumentException(
                    "This pixel isn't in the picture!");
        if (x == 1 || x + 1 == width() || y == 1 || y + 1 == height())
            return 0;
        int colorNorth = this.pic.getRGB(x, ((y + 1) % height()));
        // the extra heights are to make sure that parameter is positive
        int colorSouth = this.pic.getRGB(x, (
                (y - 1) % height() + height()) % height());
        int colorEast = this.pic.getRGB(((x + 1) % width()), y);
        // the extra widths are for the same reason as above
        int colorWest = this.pic.getRGB(
                ((x - 1) % width() + width()) % width(), y);

        // extract the red, blue, and green components of rgb and
        // find the square diff, these numbers are from the Picture class
        // description in the algs4 library
        int yRed = ((colorNorth >> 16) & 0xFF) - ((colorSouth >> 16) & 0xFF);
        int yGreen = ((colorNorth >> 8) & 0xFF) - ((colorSouth >> 8) & 0xFF);
        int yBlue = (colorNorth & 0xFF) - (colorSouth & 0xFF);
        int xRed = ((colorEast >> 16) & 0xFF) - ((colorWest >> 16) & 0xFF);
        int xGreen = ((colorEast >> 8) & 0xFF) - ((colorWest >> 8) & 0xFF);
        int xBlue = (colorEast & 0xFF) - (colorWest & 0xFF);
        double vert = yRed * yRed + yGreen * yGreen + yBlue * yBlue;
        double hori = xRed * xRed + xGreen * xGreen + xBlue * xBlue;
        return Math.sqrt(vert + hori);
    }

    // calculates the cumulative energy of a ySize by xSize rectangle
    // used to find the eyes
    // the input is the top left corner of the rectangle
    private double energyRect(int x, int y, int xSize, int ySize, double[][] input) {
        double totalRng = 0;
        for (int i = x; i < x + xSize; i++) {
            for (int j = y; j < y + ySize; j++) {
                totalRng += input[i % width()][j % height()];
            }
        }

        return totalRng;
    }

    // find the areas of greatest intensity according to the energy function
    // Assumes eyes are around 1/20 (height) x 1/5 (width)
    // Assume face is centered in the picture (face is in the middle with
    // regards to the x axis)
    public Queue<Point2D> eyeFinder() {
        Queue<Point2D> points = new Queue<Point2D>();
        Point2D champ = new Point2D(0, 0);
        Point2D secondChamp = new Point2D(0, 0);
        Point2D thirdChamp = new Point2D(0, 0);
        double thirdRng = -1;
        double maxRng = -1;
        double secondRng = -1;
        int xOffset = width() / 5;
        int yOffset = height() / 20;
        // Offsets to account for the rectangle
        for (int i = 0; i < pic.width() - xOffset; i++) {
            for (int j = 0; j < pic.height() - xOffset; j++) {
                double energyRect = energyRect(i, j, xOffset, yOffset, energy);
                if (energyRect > maxRng) {
                    thirdChamp = secondChamp;
                    thirdRng = secondRng;
                    secondChamp = champ;
                    secondRng = maxRng;
                    champ = new Point2D(i, j); // top left of the rect
                    maxRng = energyRect;
                }
                else if (energyRect > secondRng) {
                    thirdChamp = secondChamp;
                    thirdRng = secondRng;
                    secondChamp = new Point2D(i, j);
                    secondRng = energyRect;
                }
                else if (energyRect > thirdRng) {
                    thirdChamp = new Point2D(i, j);
                    thirdRng = energyRect;
                }
            }
        }

        int midx = xOffset / 2;
        int midy = yOffset / 2;
        if (champ.distanceTo(secondChamp) < 100 && secondChamp.distanceTo(thirdChamp) < 100) {
            /* handles the case for when all champs are either the left or right eye */
            points.enqueue(new Point2D(champ.x() + midx, champ.y() + midy));
            points.enqueue(new Point2D((double) width() - champ.x() - midx, champ.y() + midy));
        }
        /* next two if statements handle the 2 on one eye, 1 on the other eye case */
        else if (champ.distanceTo(secondChamp) < 25) {

            points.enqueue(new Point2D(champ.x() + midx, champ.y() + midy));
            points.enqueue(new Point2D(thirdChamp.x() + midx, thirdChamp.y() + midy));
        }
        else if (champ.distanceTo(thirdChamp) < 25) {
            points.enqueue(new Point2D(champ.x() + midx, champ.y() + midy));
            points.enqueue(new Point2D(secondChamp.x() + midx, secondChamp.y() + midy));
        }
        /* if all different, we want the two on the same y axis */
        else if (champ.y() < secondChamp.y() + height() / 20.0
                && champ.y() > secondChamp.y() - height() / 20.0) {
            points.enqueue(new Point2D(champ.x() + midx, champ.y() + midy));
            points.enqueue(new Point2D(secondChamp.x() + midx, secondChamp.y() + midy));
        }
        else if (champ.y() < thirdChamp.y() + height() / 20.0
                && champ.y() > thirdChamp.y() - height() / 20.0) {
            points.enqueue(new Point2D(champ.x() + midx, champ.y() + midy));
            points.enqueue(new Point2D(thirdChamp.x() + midx, thirdChamp.y() + midy));
        }
        else if (secondChamp.y() < thirdChamp.y() + height() / 20.0
                && secondChamp.y() > thirdChamp.y() - height() / 20.0) {
            points.enqueue(new Point2D(secondChamp.x() + midx, secondChamp.y() + midy));
            points.enqueue(new Point2D(thirdChamp.x() + midx, thirdChamp.y() + midy));
        }

        return points;
    }

    // Assume the mouth near a 1/10 (height) x 1/8 (width) box
    public Queue<Point2D> mouthFinder() {
        int xOffset = width() / 8;
        int yOffset = height() / 10;
        Queue<Point2D> mouth = new Queue<Point2D>();
        Point2D champ = new Point2D(0, 0);
        double maxRng = -1;
        for (int i = 0; i < pic.width() - xOffset; i++) {
            for (int j = 0; j < pic.height() - yOffset; j++) {
                double energyRect = energyRect(i, j, xOffset, yOffset, red);
                if (energyRect > maxRng) {
                    // center of leftmost vertical side of rect
                    champ = new Point2D(i, j + yOffset / 2);
                    maxRng = energyRect;
                }
            }
        }

        // enqueue the sizes of the mouth by enqueueing champ, and the center
        // of the rightmost vertical side of the champion rect
        mouth.enqueue(new Point2D(width() - champ.x(), champ.y()));
        mouth.enqueue(champ);

        return mouth;
    }

    // queues all the feature points and returns the queue
    // the first 2 are the eyes
    // second 2 are the mouth
    public Point2D[] findFeatures() {
        Queue<Point2D> features = new Queue<Point2D>();
        Queue<Point2D> eyes = eyeFinder();
        for (Point2D p : eyes) {
            features.enqueue(p);
        }
        Queue<Point2D> mouth = mouthFinder();
        for (Point2D p : mouth) {
            features.enqueue(p);
        }
        // // enqueue the endpoints
        // features.enqueue(new Point2D(0, 0));
        // features.enqueue(new Point2D(0, pic.height()));
        // features.enqueue(new Point2D(pic.width(), 0));
        // features.enqueue(new Point2D(pic.width(), pic.height()));
        Point2D[] featurepoints = new Point2D[4];
        int i = 0;
        for (Point2D p : features) {
            featurepoints[i] = p;
            i++;
        }
        // switch the featurepoints so we get left eye, right eye,
        // right point for mouth, left point for mouth
        if (featurepoints[0].x() > featurepoints[1].x()) {
            Point2D temp = featurepoints[0];
            featurepoints[0] = featurepoints[1];
            featurepoints[1] = temp;
        }
        if (featurepoints[2].x() < featurepoints[3].x()) {
            Point2D temp = featurepoints[3];
            featurepoints[3] = featurepoints[2];
            featurepoints[2] = temp;
        }
        return featurepoints;
    }

    // Takes in file name as command argument
    public static void main(String[] args) {
        String file = args[0];
        FeatureFinder ff = new FeatureFinder(file);
        Point2D[] features = ff.findFeatures();

        Picture pic = new Picture(file);

        for (Point2D p : features) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    pic.set((int) p.x() - 3 + i, (int) p.y() - 3 + j, Color.white);
                }
            }
        }
        pic.show();


        int count = 0;
        StdOut.println("Feature Points:");
        for (Point2D p : features) {
            if (count == 0)
                StdOut.println("Eyes: ");
            else if (count == 2)
                StdOut.println("Mouth: ");
            // else if (count == 4)
            //     StdOut.println("Edges: ");
            StdOut.println(p.x() + " " + p.y());
            count++;
        }

    }
}
