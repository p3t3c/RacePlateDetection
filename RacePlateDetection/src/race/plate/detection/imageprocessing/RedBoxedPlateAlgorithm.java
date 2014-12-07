package race.plate.detection.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class RedBoxedPlateAlgorithm implements IProcessingAlgorithm {

    private ColorPlaneExtractionAlgorithm colorPlaneExtraction;

    public RedBoxedPlateAlgorithm() {
        colorPlaneExtraction = new ColorPlaneExtractionAlgorithm();
    }

    @Override
    public List<Mat> processImage(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();

        List<Mat> colorPlaneSplit = colorPlaneExtraction.processImage(image);

        /* Use the blue channel. Expect that at index 0 */
        Mat blueChannelImage = colorPlaneSplit.get(0);
        outputImages.add(blueChannelImage.clone());

        // Perform thresholding
        Mat thresholdOutput = new Mat();
        /*
         * Not sure of the affect of this constant. Haven't found a good
         * explaination in the OpenCV Docs or tutorials. Output doesn't seem
         * overly affected by it
         */
        final int CONSTANT = 2;
        /*
         * Size of the square block that is used to perform the theshold over.
         */
        final int BLOCK_SIZE = 23;
        /* Value which is given for pixels over the threshold. */
        final double MAX_VALUE = 255.0;
        Imgproc.adaptiveThreshold(blueChannelImage, thresholdOutput, MAX_VALUE, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, BLOCK_SIZE, CONSTANT);
        outputImages.add(thresholdOutput.clone());

        
        // Start blurring
        
        
        Mat blurredInput = new Mat();
        Imgproc.blur(image, blurredInput, new Size(7, 7));
        outputImages.add(blurredInput.clone());

        colorPlaneSplit = colorPlaneExtraction.processImage(blurredInput);

        /* Use the blue channel. Expect that at index 0 */
        blueChannelImage = colorPlaneSplit.get(0);
        outputImages.add(blueChannelImage.clone());

        Imgproc.adaptiveThreshold(blueChannelImage, thresholdOutput, MAX_VALUE, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, BLOCK_SIZE, CONSTANT);
        outputImages.add(thresholdOutput.clone());
        
        return outputImages;
    }

}
