package race.plate.detection.util;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * The information from
 * 
 * <pre>
 * http://cell0907.blogspot.com.au/2013/12/from-mat-to-bufferedimage.html
 * </pre>
 * 
 * was helpful. There are a lot of other examples around that will trip up in
 * the conversion and fail to do the color byte swaps conversion (example from
 * RGB to BGR).
 * 
 * @author pete
 *
 */
public class MatFXConversionUtil {

    public static Image convertToFxImage(Mat inputMatImage) {
        BufferedImage intermediateImage = null;
        int width = inputMatImage.width();
        int height = inputMatImage.height();
        byte[] rasterData = new byte[width * height * (int) inputMatImage.elemSize()];
        Mat convertedImg; // used for 3 or 4 byte images
        int bufferedImageType;
        // There is a need to perform a color conversion to the BGR formats that
        // Java BufferedImage supports.
        System.out.println("Converting Mat image of " + inputMatImage.channels() + " channels");
        switch (inputMatImage.channels()) {
        case 1:
            inputMatImage.get(0, 0, rasterData);
            bufferedImageType = BufferedImage.TYPE_BYTE_GRAY;
            break;
        case 3:
            convertedImg = new Mat();
            Imgproc.cvtColor(inputMatImage, convertedImg, Imgproc.COLOR_RGB2BGR);
            convertedImg.get(0, 0, rasterData);
            bufferedImageType = BufferedImage.TYPE_3BYTE_BGR;
            break;
        case 4: 
            convertedImg = new Mat();
            Imgproc.cvtColor(inputMatImage, convertedImg, Imgproc.COLOR_RGBA2BGRA);
            convertedImg.get(0, 0, rasterData);
            bufferedImageType = BufferedImage.TYPE_4BYTE_ABGR;
            break;
        case 2:
        default:
            System.out.println("Unexpected number of channels in conversion. Will return null");
            return null;
        }

        intermediateImage = new BufferedImage(width, height, bufferedImageType);
        intermediateImage.getRaster().setDataElements(0, 0, width, height, rasterData);

        WritableImage outputImage = null;
        if (intermediateImage != null) {
            outputImage = new WritableImage(intermediateImage.getWidth(), intermediateImage.getHeight());
            SwingFXUtils.toFXImage(intermediateImage, outputImage);
        }
        return outputImage;
    }

}
