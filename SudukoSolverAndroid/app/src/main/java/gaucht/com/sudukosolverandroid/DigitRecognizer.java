package gaucht.com.sudukosolverandroid;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;

/**
 * Created by gaucht on 3/25/2016.
 */
public class DigitRecognizer {

    public List<Rect> rectangles;
    public Mat grid;
    public Mat cropped;

    public DigitRecognizer(){
        rectangles = new ArrayList<Rect>(81);
        grid = new Mat();
    }

    public void process(Mat image, Rect gridRect){

        //first thing we do is crop the input image to the gridRectangle
        cropped = new Mat(image, gridRect);
        grid = new Mat(cropped.size(), CvType.CV_8UC1);

        //now we want to invert the image
        adaptiveThreshold(cropped, grid, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 5, 2);

        //next we want to invert the image
        bitwise_not(grid, grid);

        rectangles.clear();
        //step one is to break the grid into 81 equal pieces.
        int cellWidth = gridRect.width/9;
        int cellHeight = gridRect.height/9;
        for(int row = 0; row < 9; row++)
        {
            int startY = cellHeight * row;
            for(int col = 0; col < 9; col++){
                int startX = cellWidth * col;

                Rect cellRect = new Rect(startX, startY, cellWidth, cellHeight);
                rectangles.add(cellRect);
                Mat cell = new Mat(grid, cellRect);

                int cellCount = 0;
                cellCount = countNonZero(cell);
                if(cellCount > 500) {
                    //now we need to figure out the number
                }
            }
        }

    }

}
