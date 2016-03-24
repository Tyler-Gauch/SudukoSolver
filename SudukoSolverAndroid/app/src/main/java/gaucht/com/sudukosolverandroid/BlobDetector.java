package gaucht.com.sudukosolverandroid;

import android.content.pm.ActivityInfo;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.inRange;
import static org.opencv.core.Core.max;
import static org.opencv.core.Core.multiply;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV_FULL;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.floodFill;
import static org.opencv.imgproc.Imgproc.pyrDown;

/**
 * Created by gaucht on 3/23/2016.
 */
public class BlobDetector {
        // Lower and Upper bounds for range checking in HSV color space
        private Scalar mLowerBound = new Scalar(0);
        private Scalar mUpperBound = new Scalar(0);
        // Minimum contour area in percent for contours filtering
        private static double mMinContourArea = 0.1;
        // Color radius for range checking in HSV color space
        private Scalar mColorRadius = new Scalar(25,50,50,0);
        private Mat mSpectrum;
        private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

        // Cache
        Mat mPyrDownMat;
        Mat mHsvMat;
        Mat mMask;
        Mat mDilatedMask;
        Mat mHierarchy;
        Mat blur;
        Mat outerBox;
        Mat kernel;
        MatOfPoint maxContour;

        public BlobDetector() {
            mSpectrum = new Mat();
            mPyrDownMat = new Mat();
            mHsvMat = new Mat();
            mMask = new Mat();
            mDilatedMask = new Mat();
            mHierarchy = new Mat();
            blur = new Mat();
            outerBox = new Mat();
            kernel = new Mat();
            kernel.put(0, 0, new byte[]{0, 1, 0, 1, 1, 1, 0, 1, 0});
            maxContour = new MatOfPoint();
        }

        public void setMinContourArea(double area) {
            mMinContourArea = area;
        }

        public void process(Mat image) {
            //pyrDown(rgbaImage, mPyrDownMat);
            //pyrDown(mPyrDownMat, mPyrDownMat);
            outerBox = new Mat(image.size(), CvType.CV_8UC1);

            GaussianBlur(image, image, new Size(11,11), 0);

            adaptiveThreshold(image, outerBox, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 5, 2);

            bitwise_not(outerBox, outerBox);

            dilate(outerBox, mDilatedMask, kernel);

            mContours.clear();

            findContours(mDilatedMask, mContours, mHierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

            // Find max contour area
            double maxArea = 0;
            Iterator<MatOfPoint> each = mContours.iterator();
            while (each.hasNext()) {
                MatOfPoint wrapper = each.next();
                double area = contourArea(wrapper);
                if (area > maxArea)
                    maxArea = area;
                    maxContour = wrapper;
            }
        }

        public List<MatOfPoint> getContours() {
            return mContours;
        }

        public MatOfPoint getMaxContour(){return maxContour;}
    }
