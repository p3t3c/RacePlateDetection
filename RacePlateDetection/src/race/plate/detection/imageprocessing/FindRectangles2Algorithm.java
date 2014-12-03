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

        findRectangles(thresholdOutput, contours, outputImages);
        // Mat morphOutput = new Mat();
        // Mat morphKernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
        // new Size(8,8));
        // Imgproc.morphologyEx(thresholdOutput, morphOutput,
        // Imgproc.MORPH_CLOSE, morphKernal);
        // outputImages.add(morphOutput.clone());

        return outputImages;
    }

    private void findRectangles(Mat inputImage, List<MatOfPoint> contours, List<Mat> outputImages) {
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f approxfc2 = new MatOfPoint2f();
            // // approximate contour with accuracy proportional
            // // to the contour perimeter
            MatOfPoint2f mop2f = new MatOfPoint2f();
            contours.get(i).convertTo(mop2f, CvType.CV_32FC2);
            Imgproc.approxPolyDP(mop2f, approxfc2, Imgproc.arcLength(mop2f, true) * 0.02, true);
            //
            // // square contours should have 4 vertices after
            // // approximation
            // // relatively large area (to filter out noisy contours)
            // // and be convex.
            // // Note: absolute value of an area is used because
            // // area may be positive or negative - in accordance with
            // the
            // // contour orientation

            MatOfPoint approx = new MatOfPoint();
            approxfc2.convertTo(approx, CvType.CV_32S);
            // if (approx.size() == 4 &&
            // Math.abs(Imgproc.contourArea(new MatOfPoint2f(approx))) >
            // 1000 && Imgproc.isContourConvex(new MatOfPoint(approx)))
            // {
            Point[] approxArray = approx.toArray();
            if (approxArray.length == 4 && Math.abs(Imgproc.contourArea(approxfc2)) > 1000 && Imgproc.isContourConvex(approx)) {
                double maxCosine = 0;

                for (int j = 2; j < 5; j++) {
                    // find the maximum cosine of the angle between
                    // joint edges
                    double cosine = Math.abs(FindRectanglesAlgorithm.angle(approxArray[j % 4], approxArray[j - 2], approxArray[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }

                // if cosines of all angles are small
                // (all angles are ~90 degree) then write quandrange
                // vertices to resultant sequence
                if (maxCosine < 0.3) {
                    Mat detectedImage = new Mat();
                    Imgproc.cvtColor(inputImage, detectedImage, Imgproc.COLOR_GRAY2BGR);
                    List<MatOfPoint> ffs = new ArrayList<>();
                    ffs.add(approx);
                    Core.polylines(detectedImage, ffs, true, new Scalar(0, 255, 0));
                    outputImages.add(detectedImage);
                    // Here is where the real output is
                    // squares.push_back(approx);
                }
            }
        }
    }

}
