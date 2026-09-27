import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Offline generator for the digit-recognition training set used by
 * gaucht.com.sudukosolverandroid.DigitClassifier.
 *
 * Renders the digits 1-9 in a variety of installed fonts/styles/sizes/rotations,
 * binarizes them the same way the on-device camera pipeline does (ink = 255,
 * background = 0), crops to the ink bounding box, pads to a square and resizes
 * to SIZE x SIZE, then writes everything to a small binary file that is bundled
 * as an Android asset and loaded by DigitClassifier at runtime to train an
 * OpenCV KNearest model.
 *
 * Binary format (big-endian, via DataOutputStream):
 *   int32 sampleCount
 *   int32 featureDim   (== SIZE*SIZE)
 *   int32[sampleCount] labels   (1-9)
 *   float32[sampleCount * featureDim] features, row-major per sample
 *
 * Usage (needs a JDK with java.awt/Graphics2D; run headless is fine):
 *   javac Generator.java
 *   java -Djava.awt.headless=true Generator digits.dat
 *   cp digits.dat ../../app/src/main/assets/digits.dat
 */
public class Generator {

    static final int SIZE = 20;
    static final int CANVAS = 140;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        String[] fontNames = {
                Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED,
                "DejaVu Sans", "DejaVu Serif", "DejaVu Sans Mono",
                "Liberation Sans", "Liberation Serif", "Liberation Mono",
                "FreeSans", "FreeSerif", "FreeMono"
        };
        int[] styles = {Font.PLAIN, Font.BOLD};
        int[] sizes = {70, 85, 100};
        double[] rotationsDeg = {-10, -5, 0, 5, 10};

        List<float[]> samples = new ArrayList<float[]>();
        List<Integer> labels = new ArrayList<Integer>();

        for (int digit = 1; digit <= 9; digit++) {
            for (String fontName : fontNames) {
                for (int style : styles) {
                    for (int size : sizes) {
                        for (double rotDeg : rotationsDeg) {
                            float[] feat = renderAndExtract(String.valueOf(digit), fontName, style, size, rotDeg);
                            if (feat != null) {
                                samples.add(feat);
                                labels.add(digit);
                            }
                        }
                    }
                }
            }
        }

        String outPath = args.length > 0 ? args[0] : "digits.dat";
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outPath)));
        try {
            out.writeInt(samples.size());
            out.writeInt(SIZE * SIZE);
            for (int l : labels) {
                out.writeInt(l);
            }
            for (float[] f : samples) {
                for (float v : f) {
                    out.writeFloat(v);
                }
            }
        } finally {
            out.close();
        }
        System.out.println("Wrote " + samples.size() + " samples (" + (SIZE * SIZE) + " dims) to " + outPath);
    }

    /**
     * Renders one glyph and returns its normalized SIZE x SIZE feature vector,
     * or null if nothing was drawn (shouldn't happen for digits 1-9).
     */
    static float[] renderAndExtract(String text, String fontName, int style, int size, double rotDeg) {
        BufferedImage img = new BufferedImage(CANVAS, CANVAS, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, CANVAS, CANVAS);

        Font font = new Font(fontName, style, size);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent() - fm.getDescent();
        double cx = CANVAS / 2.0;
        double cy = CANVAS / 2.0;

        g.rotate(Math.toRadians(rotDeg), cx, cy);
        g.setColor(Color.BLACK);
        g.drawString(text, (float) (cx - textWidth / 2.0), (float) (cy + textHeight / 2.0));
        g.dispose();

        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        int minX = CANVAS, minY = CANVAS, maxX = -1, maxY = -1;
        boolean[][] ink = new boolean[CANVAS][CANVAS];
        for (int y = 0; y < CANVAS; y++) {
            for (int x = 0; x < CANVAS; x++) {
                int v = pixels[y * CANVAS + x] & 0xFF;
                if (v < 128) {
                    ink[y][x] = true;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < 0) {
            return null;
        }

        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        int side = Math.max(w, h);
        int pad = (int) (side * 0.2);
        side += pad * 2;

        BufferedImage square = new BufferedImage(side, side, BufferedImage.TYPE_BYTE_GRAY);
        int offX = (side - w) / 2;
        int offY = (side - h) / 2;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (ink[minY + y][minX + x]) {
                    square.getRaster().setSample(offX + x, offY + y, 0, 255);
                }
            }
        }

        BufferedImage resized = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gr = resized.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gr.drawImage(square, 0, 0, SIZE, SIZE, null);
        gr.dispose();

        byte[] rp = ((DataBufferByte) resized.getRaster().getDataBuffer()).getData();
        float[] feat = new float[SIZE * SIZE];
        for (int i = 0; i < feat.length; i++) {
            feat[i] = (rp[i] & 0xFF) / 255f;
        }
        return feat;
    }
}
