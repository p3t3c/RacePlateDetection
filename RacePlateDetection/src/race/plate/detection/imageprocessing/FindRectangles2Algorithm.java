package race.plate.detection.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class FindRectangles2Algorithm implements IProcessingAlgorithm {
    private static final int THRESHOLD = 200;
    private static final int DILATION_ITERATIONS = 1;

    @Override
    public List<Mat> processImage(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();
        // List<MatOfPoint> contours = new ArrayList<>();
        Mat grey = new Mat();
        Mat edgeEnhanced = new Mat();

        Imgproc.cvtColor(image, grey, Imgproc.COLOR_BGR2GRAY);
        outputImages.add(grey.clone());
        System.out.println(grey.channels() + ", " + grey.cols() + ", " + CvType.typeToString(grey.type()));
        // ddepth of -1 keeps the depth the same as the input
        // Imgproc.Sobel(grey, edgeEnhanced, -1, 0, 1);
        // Imgproc.Sobel(grey, edgeEnhanced, -1, 2, 2, 5 , 1.0, 1.0);
        // outputImages.add(edgeEnhanced.clone());

//      Imgproc.blur(grey, grey, new Size(6, 6));
//      outputImages.add(grey.clone());
      
        Mat thresholdOutput = new Mat();
        // Imgproc.threshold(edgeEnhanced, thresholdOutput, THRESHOLD, 255,
        // Imgproc.THRESH_OTSU);
        final int c = 2;
        Imgproc.adaptiveThreshold(grey, thresholdOutput, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 3, c);
        outputImages.add(thresholdOutput.clone());

        Imgproc.adaptiveThreshold(grey, thresholdOutput, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, c);
        outputImages.add(thresholdOutput.clone());

        Imgproc.adaptiveThreshold(grey, thresholdOutput, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 23, c);
        outputImages.add(thresholdOutput.clone());

        Imgproc.adaptiveThreshold(grey, thresholdOutput, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 47, c);
        outputImages.add(thresholdOutput.clone());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(thresholdOutput, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourDrawing = thresholdOutput.clone();
        Imgproc.cvtColor(contourDrawing, contourDrawing, Imgproc.COLOR_GRAY2BGR);
        Imgproc.drawContours(contourDrawing, contours, -1, new Scalar(0, 255, 0), 1);
        outputImages.add(contourDrawing);

        DetectRectanglesInContours.findRectangles(thresholdOutput, contours, outputImages);
        // Mat morphOutput = new Mat();
        // Mat morphKernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
        // new Size(8,8));
        // Imgproc.morphologyEx(thresholdOutput, morphOutput,
        // Imgproc.MORPH_CLOSE, morphKernal);
        // outputImages.add(morphOutput.clone());

        return outputImages;
    }


}
