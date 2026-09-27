package gaucht.com.sudukosolverandroid;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import gaucht.com.sudukosolverandroid.solver.SudokuPuzzle;
import gaucht.com.sudukosolverandroid.solver.SudokuSolver;

import static org.opencv.imgproc.Imgproc.getTextSize;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.rectangle;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final String TAG = "MainActivity";

    // A sudoku needs at least this many givens to have any chance of a unique solution;
    // fewer than that almost certainly means our digit recognition misread the grid.
    private static final int MIN_GIVEN_DIGITS = 17;

    // Recognition is the expensive part of the pipeline, so while unsolved we only retry
    // it every RECOGNITION_RETRY_INTERVAL_FRAMES frames instead of on every single frame.
    private static final int RECOGNITION_RETRY_INTERVAL_FRAMES = 15;

    private CameraBridgeViewBase mOpenCvCameraView;
    private GridDetector bd;
    private DigitRecognizer dr;

    // Once a grid has been fully recognized and solved, we stop re-running recognition
    // entirely and just keep drawing the cached solution on top of wherever the grid is
    // currently detected, so the overlay still tracks the camera.
    private boolean solved = false;
    private int framesUntilNextAttempt = 0;
    private int[][] solution;
    private boolean[][] given;

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
        // Tapping the preview resets the scan, so the user can point at a new puzzle
        // once the current one has been solved (or if a misread needs retrying).
        mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    resetScan();
                }
                return true;
            }
        });
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
        dr = new DigitRecognizer(this);
        resetScan();
    }

    @Override
    public void onCameraViewStopped() {

    }

    private void resetScan() {
        solved = false;
        framesUntilNextAttempt = 0;
        solution = null;
        given = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //get the gray and color images

        //we will process on the gray because we don't need color
        Mat gray = inputFrame.gray();
        //we will draw on and show the color to the user
        Mat color = inputFrame.rgba();

        //calculate the height of the crop box
        int height = inputFrame.rgba().height() - 100;
        //build the crop rectangle
        Rect box = new Rect((inputFrame.rgba().width() - height) / 2, 50, height, height);

        //process to find the grid
        bd.process(gray, box);

        if (bd.foundGrid) {
            Point br1 = new Point(bd.gridRect.x, bd.gridRect.y);
            Point br2 = new Point(bd.gridRect.x + bd.gridRect.width, bd.gridRect.y + bd.gridRect.height);
            rectangle(color, br1, br2, CommonUtils.GREEN, 20);

            if (!solved) {
                if (framesUntilNextAttempt <= 0) {
                    framesUntilNextAttempt = RECOGNITION_RETRY_INTERVAL_FRAMES;
                    attemptRecognizeAndSolve(gray, bd.gridRect);
                } else {
                    framesUntilNextAttempt--;
                }
            }

            if (solved) {
                drawSolution(color, br1);
            } else {
                // still searching/recognizing: show the cell grid we're about to read so the
                // user can see we found the puzzle boundary.
                for (Rect r : dr.rectangles) {
                    rectangle(color, new Point(r.x + br1.x, r.y + br1.y),
                            new Point(r.x + r.width + br1.x, r.y + r.height + br1.y), CommonUtils.RED, 5);
                }
            }

        } else {
            framesUntilNextAttempt = 0;
            //if we didn't find the puzzle display the area to crop in
            //draw the rectangle on the image.  This is the area that the puzzle should be placed to do the solving
            //this makes it faster by eliminating pixels
            rectangle(color, new Point(box.x, box.y), new Point(box.x + box.width, box.y + box.height), CommonUtils.GREEN, 20);
        }

        return color;
    }

    private void attemptRecognizeAndSolve(Mat gray, Rect gridRect) {
        dr.process(gray, gridRect);

        int givenCount = 0;
        int[][] grid = new int[9][9];
        boolean[][] givenMask = new boolean[9][9];
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = dr.digits[row][col];
                grid[row][col] = value;
                givenMask[row][col] = value != 0;
                if (value != 0) {
                    givenCount++;
                }
            }
        }

        if (givenCount < MIN_GIVEN_DIGITS) {
            // Almost certainly a misread rather than a real puzzle; let the next frame retry.
            return;
        }

        SudokuSolver solver = new SudokuSolver(new SudokuPuzzle(grid));
        if (solver.isSolved()) {
            solution = solver.getPuzzle().toGrid();
            given = givenMask;
            solved = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Solved! Tap the screen to scan a new puzzle.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void drawSolution(Mat color, Point gridOrigin) {
        int cellWidth = bd.gridRect.width / 9;
        int cellHeight = bd.gridRect.height / 9;
        int fontFace = org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
        double fontScale = cellHeight / 45.0;
        int thickness = Math.max(2, cellHeight / 20);

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (given[row][col]) {
                    continue; // leave the user's original digits alone; only fill in the blanks
                }
                String text = String.valueOf(solution[row][col]);
                Size textSize = getTextSize(text, fontFace, fontScale, thickness, new int[1]);
                double cellCenterX = gridOrigin.x + col * cellWidth + cellWidth / 2.0;
                double cellCenterY = gridOrigin.y + row * cellHeight + cellHeight / 2.0;
                Point textOrigin = new Point(cellCenterX - textSize.width / 2, cellCenterY + textSize.height / 2);
                putText(color, text, textOrigin, fontFace, fontScale, CommonUtils.BLUE, thickness);
            }
        }
    }
}
