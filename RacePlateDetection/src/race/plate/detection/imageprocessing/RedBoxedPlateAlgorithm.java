package race.plate.detection.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class RedBoxedPlateAlgorithm implements IProcessingAlgorithm {

    private ColorPlaneExtractionAlgorithm colorPlaneExtraction;
    private SquareMorphologyAlgorithm squareMorphologyAlgorithm;

    public RedBoxedPlateAlgorithm() {
        colorPlaneExtraction = new ColorPlaneExtractionAlgorithm();
        squareMorphologyAlgorithm = new SquareMorphologyAlgorithm(17);
    }

    @Override
    public List<Mat> processImage(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();

        // De-noise
        Mat blurredInput = new Mat();
        Imgproc.blur(image, blurredInput, new Size(27, 27));
        outputImages.add(blurredInput.clone());
        
        List<Mat> colorPlaneSplit = colorPlaneExtraction.processImage(blurredInput);

        /* Use the blue channel. Expect that at index 0 */
        Mat blueChannelImage = colorPlaneSplit.get(0);
        outputImages.add(blueChannelImage.clone());

        
        // Perform thresholding
        Mat thresholdOutput = new Mat();
        /*
         * Not sure of the affect of this constant. Haven't found a good
         * explanation in the OpenCV Docs or tutorials. Output doesn't seem
         * overly affected by it
         */
        final int CONSTANT = 2;
        /*
         * Size of the square block that is used to perform the threshold over.
         */
        final int BLOCK_SIZE = 47;
        /* Value which is given for pixels over the threshold. */
        final double MAX_VALUE = 255.0;
        Imgproc.adaptiveThreshold(blueChannelImage, thresholdOutput, MAX_VALUE, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, BLOCK_SIZE, CONSTANT);
        outputImages.add(thresholdOutput.clone());

        // Morphology
        List<Mat> morphOutputList = squareMorphologyAlgorithm.processImage(thresholdOutput);
        outputImages.addAll(morphOutputList);
        
        // Find contours
//        List<MatOfPoint> contours = findContours(thresholdOutput);
//        drawContours(contours, thresholdOutput, outputImages);
        List<MatOfPoint> contours = findContours(morphOutputList.get(0));
        drawContours(contours, morphOutputList.get(0), outputImages);
        
        DetectRectanglesInContours.findRectangles(image, contours, outputImages);
        
        return outputImages;
    }


    private List<MatOfPoint> findContours(Mat inputImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(inputImage, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    private void drawContours(List<MatOfPoint> contours, Mat imageToOverlay, List<Mat> outputImages) {
        Mat contourDrawing = imageToOverlay.clone();
        Imgproc.cvtColor(contourDrawing, contourDrawing, Imgproc.COLOR_GRAY2BGR);
        Imgproc.drawContours(contourDrawing, contours, -1, new Scalar(0,255,0), 3);
        outputImages.add(contourDrawing);
        
    }
}
