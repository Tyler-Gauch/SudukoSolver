package gaucht.com.sudukosolverandroid;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.findContours;

/**
 * Created by gaucht on 3/23/2016.
 */
public class GridDetector {
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 100000;

    public boolean foundGrid;
    public Rect gridRect;

    // Cache
    private Mat mDilatedMask;
    private Mat mHierarchy;
    private Mat blur;
    private Mat outerBox;
    private Mat kernel;
    private MatOfPoint maxContour;
    private Mat cropped;

    public GridDetector() {
        mDilatedMask = new Mat();
        mHierarchy = new Mat();
        blur = new Mat();
        outerBox = new Mat();
        kernel = new Mat();
        kernel.put(0, 0, new byte[]{0, 3, 0, 3, 3, 3, 0, 3, 0});
        maxContour = new MatOfPoint();
        foundGrid = false;
        gridRect = new Rect();
    }

    public void process(Mat image, Rect rect) {

        cropped = new Mat(image, rect);
        outerBox = new Mat(cropped.size(), CvType.CV_8UC1);

        GaussianBlur(cropped, cropped, new Size(15, 15), 0);

        adaptiveThreshold(cropped, outerBox, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 5, 2);

        bitwise_not(outerBox, outerBox);

        dilate(outerBox, mDilatedMask, kernel);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        findContours(mDilatedMask, contours, mHierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE, rect.tl());

        // Find max contour area
        double maxArea = 0;
        maxContour = null;
        foundGrid = false;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = contourArea(wrapper);
            if (area >= mMinContourArea && area > maxArea) {
                maxArea = area;
                maxContour = wrapper;
            }
        }

        if(maxContour != null)
        {
            gridRect = boundingRect(maxContour);
            foundGrid = true;
        }

    }

    public MatOfPoint getMaxContour(){return maxContour;}

}
