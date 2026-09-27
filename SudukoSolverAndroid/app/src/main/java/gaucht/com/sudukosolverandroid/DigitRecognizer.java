package gaucht.com.sudukosolverandroid;

import android.content.Context;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;

/**
 * Created by gaucht on 3/25/2016.
 */
public class DigitRecognizer {

    private static final int GRID_SIZE = 9;
    // Cells are shrunk inward by this fraction on each side before classifying, so the
    // surrounding grid lines don't get mistaken for part of the digit.
    private static final double CELL_MARGIN = 0.12;

    public List<Rect> rectangles;
    public Mat grid;
    public Mat cropped;
    /** Recognized digits, 0 for a blank square, indexed [row][col]. */
    public int[][] digits;

    private final DigitClassifier classifier;

    public DigitRecognizer(Context context) {
        rectangles = new ArrayList<Rect>(GRID_SIZE * GRID_SIZE);
        grid = new Mat();
        digits = new int[GRID_SIZE][GRID_SIZE];
        classifier = new DigitClassifier(context);
    }

    public void process(Mat image, Rect gridRect) {

        //first thing we do is crop the input image to the gridRectangle
        cropped = new Mat(image, gridRect);
        grid = new Mat(cropped.size(), CvType.CV_8UC1);

        //now we want to invert the image
        adaptiveThreshold(cropped, grid, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 5, 2);

        //next we want to invert the image
        bitwise_not(grid, grid);

        rectangles.clear();
        //step one is to break the grid into 81 equal pieces.
        int cellWidth = gridRect.width / GRID_SIZE;
        int cellHeight = gridRect.height / GRID_SIZE;
        int marginX = (int) (cellWidth * CELL_MARGIN);
        int marginY = (int) (cellHeight * CELL_MARGIN);

        for (int row = 0; row < GRID_SIZE; row++) {
            int startY = cellHeight * row;
            for (int col = 0; col < GRID_SIZE; col++) {
                int startX = cellWidth * col;

                Rect cellRect = new Rect(startX, startY, cellWidth, cellHeight);
                rectangles.add(cellRect);

                Rect innerRect = new Rect(startX + marginX, startY + marginY,
                        cellWidth - 2 * marginX, cellHeight - 2 * marginY);
                Mat cell = new Mat(grid, innerRect);

                int cellCount = countNonZero(cell);
                int cellArea = innerRect.width * innerRect.height;
                if (cellCount > cellArea * 0.03) {
                    digits[row][col] = classifier.recognize(cell);
                } else {
                    digits[row][col] = 0;
                }
            }
        }

    }

}
