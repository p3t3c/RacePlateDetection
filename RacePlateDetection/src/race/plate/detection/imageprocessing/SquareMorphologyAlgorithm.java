package race.plate.detection.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Performs a rectangular morphology on the image. 
 * <p>
 * Output: morph'ed image (one image only) in the output list.
 */
public class SquareMorphologyAlgorithm implements IProcessingAlgorithm {

    private int structuringElementSize;

    public SquareMorphologyAlgorithm(int newStructuringElementSize) {
        this.structuringElementSize = newStructuringElementSize;
    }

    @Override
    public List<Mat> processImage(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();
        Mat morphOutput = new Mat();
        
        Mat morphKernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(structuringElementSize, structuringElementSize));
        Imgproc.morphologyEx(image, morphOutput, Imgproc.MORPH_CLOSE, morphKernal);
        
        outputImages.add(morphOutput.clone());
        return outputImages;
    }

}
