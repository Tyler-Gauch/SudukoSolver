package gaucht.com.sudukosolverandroid;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.max;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.floodFill;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.pyrDown;
import static org.opencv.imgproc.Imgproc.rectangle;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final String TAG = "MainActivity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private GridDetector bd;
    private DigitRecognizer dr;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
            }

            super.onManagerConnected(status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCvCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        bd = new GridDetector();
        dr = new DigitRecognizer();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //get the gray and color images

        //we will process on the gray because we don't need color
        Mat gray = inputFrame.gray();
        //we will draw on and show the color to the user
        Mat color = inputFrame.rgba();

        //calculate the height of the crop box
        int height = inputFrame.rgba().height()-100;
        //build the crop rectangle
        Rect box = new Rect((inputFrame.rgba().width()-height)/2, 50, height, height);

        //process to find the grid
        bd.process(gray, box);

        if(bd.foundGrid)
        {
            Point br1 = new Point(bd.gridRect.x, bd.gridRect.y);
            Point br2 = new Point(bd.gridRect.x+bd.gridRect.width, bd.gridRect.y+bd.gridRect.height);
            rectangle(color, br1, br2, CommonUtils.GREEN,20);

            //process to find the digits
            dr.process(gray, bd.gridRect);
            for(Rect r : dr.rectangles){
                rectangle(color, new Point(r.x+br1.x, r.y+br1.y), new Point(r.x+r.width+br1.x, r.y+r.height+br1.y), CommonUtils.RED, 5);
            }

        }else
        {
            //if we didn't find the puzzle display the area to crop in
            //draw the rectangle on the image.  This is the area that the puzzle should be placed to do the solving
            //this makes it faster by eliminating pixels
            rectangle(color, new Point(box.x, box.y), new Point(box.x+box.width, box.y+box.height), CommonUtils.GREEN,20);
        }

        return color;
    }
}
