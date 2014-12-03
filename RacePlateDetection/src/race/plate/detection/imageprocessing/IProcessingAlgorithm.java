package race.plate.detection.imageprocessing;

import java.util.List;

import org.opencv.core.Mat;

public interface IProcessingAlgorithm {

    /**
     * Perform processing on the image. Mutations will occur to the image passed
     * in.
     * 
     * @param image
     *            on which processing is made. Shall not be modified
     * @return the processed Image sequence. The last image in the list shall be the final image produced by the algoithm           
     */
    List<Mat> processImage(Mat image);

}
