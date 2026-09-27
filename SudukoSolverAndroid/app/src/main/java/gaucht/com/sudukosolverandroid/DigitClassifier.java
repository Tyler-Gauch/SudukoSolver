package gaucht.com.sudukosolverandroid;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Recognizes a single printed digit (1-9) from a thresholded, binary cell image using
 * an OpenCV KNearest classifier (org.opencv.ml.KNearest) - the "ml" module is the part of
 * OpenCV's own toolkit built for exactly this, since OpenCV's Android distribution doesn't
 * include the Tesseract-backed "text" OCR module.
 * <p>
 * The classifier is trained at construction time from assets/digits.dat, a small binary
 * dump of normalized SIZE x SIZE feature vectors rendered from installed system fonts
 * (see tools/digit-training-set-generator/Generator.java for how that file is produced).
 */
public class DigitClassifier {

    private static final String TAG = "DigitClassifier";
    private static final String ASSET_NAME = "digits.dat";

    /** Side length (in pixels) that every candidate digit blob is normalized to before classifying. */
    static final int SAMPLE_SIZE = 20;

    /** How many neighbors vote on each prediction. */
    private static final int K = 3;

    private final KNearest knn;

    public DigitClassifier(Context context) {
        knn = KNearest.create();
        try {
            loadTrainingData(context);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load digit training data from assets/" + ASSET_NAME, e);
        }
    }

    private void loadTrainingData(Context context) throws IOException {
        InputStream is = context.getAssets().open(ASSET_NAME);
        DataInputStream in = new DataInputStream(is);
        try {
            int count = in.readInt();
            int dim = in.readInt();

            int[] labelInts = new int[count];
            for (int i = 0; i < count; i++) {
                labelInts[i] = in.readInt();
            }

            Mat samples = new Mat(count, dim, CvType.CV_32F);
            float[] row = new float[dim];
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < dim; j++) {
                    row[j] = in.readFloat();
                }
                samples.put(i, 0, row);
            }

            Mat responses = new Mat(count, 1, CvType.CV_32F);
            for (int i = 0; i < count; i++) {
                responses.put(i, 0, (float) labelInts[i]);
            }

            knn.train(samples, Ml.ROW_SAMPLE, responses);
            Log.i(TAG, "Trained KNearest digit classifier on " + count + " samples (" + dim + " dims)");
        } finally {
            in.close();
        }
    }

    /**
     * Classifies one binary (0/255) cell image and returns the recognized digit 1-9,
     * or 0 if the cell doesn't contain a plausible, isolated digit blob (i.e. it's blank
     * or the "ink" found is noise/grid-line leakage rather than a digit).
     */
    public int recognize(Mat cellBinary) {
        Mat nonZero = new Mat();
        Core.findNonZero(cellBinary, nonZero);
        if (nonZero.empty()) {
            return 0;
        }

        Rect bbox = Imgproc.boundingRect(new MatOfPoint(nonZero));

        int cellArea = cellBinary.rows() * cellBinary.cols();
        int bboxArea = bbox.width * bbox.height;
        // Too small to be a digit (stray noise) or too large (grid line leaked into the cell).
        if (bboxArea < cellArea * 0.02 || bbox.width > cellBinary.cols() * 0.9 || bbox.height > cellBinary.rows() * 0.9) {
            return 0;
        }

        Mat digit = new Mat(cellBinary, bbox);

        int side = Math.max(bbox.width, bbox.height);
        side += (int) (side * 0.4);
        Mat square = Mat.zeros(side, side, CvType.CV_8UC1);
        int offsetX = (side - bbox.width) / 2;
        int offsetY = (side - bbox.height) / 2;
        digit.copyTo(square.submat(new Rect(offsetX, offsetY, bbox.width, bbox.height)));

        Mat resized = new Mat();
        Imgproc.resize(square, resized, new Size(SAMPLE_SIZE, SAMPLE_SIZE), 0, 0, Imgproc.INTER_LINEAR);

        Mat sample = new Mat();
        resized.convertTo(sample, CvType.CV_32F, 1.0 / 255.0);
        Mat sampleRow = sample.reshape(1, 1);

        Mat results = new Mat();
        float predicted = knn.findNearest(sampleRow, K, results);

        int digitValue = Math.round(predicted);
        if (digitValue < 1 || digitValue > 9) {
            return 0;
        }
        return digitValue;
    }
}
