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
import org.opencv.imgproc.Imgproc;

public class DetectRectanglesInContours {
    /**
     * 
     * @param inputImage
     *            Used as the basis to draw the rectangles for the output.
     * @param contours
     * @param outputImages
     */
    static void findRectangles(Mat inputImage, List<MatOfPoint> contours, List<Mat> outputImages) {
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f approxfc2 = new MatOfPoint2f();
            // approximate contour with accuracy proportional
            // to the contour perimeter
            MatOfPoint2f mop2f = new MatOfPoint2f();
            contours.get(i).convertTo(mop2f, CvType.CV_32FC2);
            Imgproc.approxPolyDP(mop2f, approxfc2, Imgproc.arcLength(mop2f, true) * 0.02, true);
            //
            // square contours should have 4 vertices after
            // approximation
            // relatively large area (to filter out noisy contours)
            // and be convex.
            // Note: absolute value of an area is used because
            // area may be positive or negative - in accordance with
            // the contour orientation

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
                    double cosine = Math.abs(angle(approxArray[j % 4], approxArray[j - 2], approxArray[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }

                // if cosines of all angles are small
                // (all angles are ~90 degree) then write quandrange
                // vertices to resultant sequence
                if (maxCosine < 0.3) {
                    Mat detectedImage;
                    if (inputImage.channels() == 3) {
                        detectedImage = inputImage.clone();
                    } else {
                        detectedImage = new Mat();
                        Imgproc.cvtColor(inputImage, detectedImage, Imgproc.COLOR_GRAY2BGR);
                    }

                    List<MatOfPoint> approxImageList = new ArrayList<>(); // List
                                                                          // just
                                                                          // needed
                                                                          // to
                                                                          // call
                                                                          // polylines
                    approxImageList.add(approx);
                    Core.polylines(detectedImage, approxImageList, true, new Scalar(0, 255, 0));
                    outputImages.add(detectedImage);
                    // Here is where the real output is
                    // squares.push_back(approx);
                }
            }
        }
    }

    /**
     * helper function: finds a cosine of angle between vectors from pt0->pt1
     * and from pt0->pt2 <br>
     * Although I think the dot product should also be able to do this, its
     * close but I dont get the cosine part
     **/
    static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }
}
