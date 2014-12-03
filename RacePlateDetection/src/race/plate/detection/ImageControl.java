package race.plate.detection;

import java.util.List;
import java.util.Stack;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import race.plate.detection.imageprocessing.IProcessingAlgorithm;

public class ImageControl {
    private Mat inputImage;
    /**
     * Image that has had processing performed upon it. All processing will be
     * performed on this instance of the image.
     */
    private Stack<Mat> outputImageStack;

    public ImageControl() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        outputImageStack = new Stack<Mat>();
    }

    /**
     * Loads an image and resets the output image to this image. Expecting it to
     * be a path into the classpath.
     * 
     * @param resource
     */
    public void loadImageAsResource(String resource) {
        String path = getClass().getResource(resource).getPath();
        if (path.startsWith("/")) {
            // Need to remove any leading /
            path = path.substring(1);
        }

        loadMatImage(path);
    }

    /**
     * Get the input image.
     * 
     * @return
     */
    public Mat getInputImage() {
        return inputImage;
    }

    /**
     * Returns a clone of the output image.
     * 
     * @return a clone of the output image
     */
    public Mat[] getOutputImageSequence() {
        Mat[] outputImages = new Mat[outputImageStack.size()];
        outputImageStack.copyInto(outputImages);
        return outputImages;
    }
    
    /**
     * Peeks the output image stack for the latest image.
     * @return
     */
    public Mat getLatestOutputImage() {
        return outputImageStack.peek();
    }

    public void addOutputImage(Mat outputImage) {
        outputImageStack.push(outputImage);
    }
    
    public void applyProcessing(IProcessingAlgorithm algorithm) {
        List<Mat> imageSequence = algorithm.processImage(outputImageStack.peek());
        outputImageStack.addAll(imageSequence);
    }

    private void loadMatImage(String filePath) {
        // Note this call won't load the images alpha values see the OpenCV api
        inputImage = Highgui.imread(filePath);
        outputImageStack.push(inputImage.clone());
        if (inputImage.nativeObj == 0)
            System.out.println("Warning file " + filePath + " Couldn't be loaded");
    }
}
