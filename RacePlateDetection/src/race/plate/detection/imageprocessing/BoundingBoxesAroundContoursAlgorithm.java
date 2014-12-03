package race.plate.detection.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class BoundingBoxesAroundContoursAlgorithm implements IProcessingAlgorithm {
    private static final int THRESHOLD = 150;

    @Override
    public List<Mat> processImage(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();

        // Convert image to gray and blur it
        Mat grey = new Mat();
        Imgproc.cvtColor(image, grey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grey, grey, new Size(3, 3));

        outputImages.add(grey.clone());

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // / Detect edges using Threshold
        Mat thresholdOutput = new Mat();
        Imgproc.threshold(grey, thresholdOutput, THRESHOLD, 255, Imgproc.THRESH_BINARY);
        
        outputImages.add(thresholdOutput.clone());
        
        // / Find contours
        Imgproc.findContours(thresholdOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        // / Approximate contours to polygons + get bounding rects and circles
        List<MatOfPoint2f> contoursPolys = new ArrayList<>();
        List<Rect> boundRect = new ArrayList<>();

        for (MatOfPoint matOfPoint : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f();
            MatOfPoint2f contourPoly2f = new MatOfPoint2f();
            MatOfPoint contourPoly = new MatOfPoint();
            matOfPoint.convertTo(contour2f, CvType.CV_32FC2);

            Imgproc.approxPolyDP(contour2f, contourPoly2f, 3, true);
            contoursPolys.add(contourPoly2f);

            contourPoly2f.convertTo(contourPoly, CvType.CV_32S);
            boundRect.add(Imgproc.boundingRect(contourPoly));
        }

        // / Draw polygonal contour + bonding rects + circles
        Mat drawing = image.clone();
        Scalar color = new Scalar(0, 255, 0);
        // -1 means draw all contours
        Imgproc.drawContours(drawing, contours, -1, color, 1);
        for (Rect rect : boundRect) {
            Core.rectangle(drawing, rect.tl(), rect.br(), color);
        }
        // for( int i = 0; i< contours.size(); i++ )
        // {
        // rectangle( drawing, boundRect[i].tl(), boundRect[i].br(), color, 2,
        // 8, 0 );
        // }

        outputImages.add(drawing);

        return outputImages;
    }
}
