package race.plate.detection.imageprocessing;

import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class FindRectanglesAlgorithm implements IProcessingAlgorithm {

    private static final int thresh = 200;
    private static final int N = 11;
    private static final int DILATION_ITERATIONS = 1;

    @Override
    public List<Mat> processImage(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();
        outputImages.addAll(findSquares(image));

        return outputImages;
    }

    private List<Mat> findSquares(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat pyramid = new Mat();
        Mat tempImg = new Mat();
        Mat gray0 = new Mat(image.size(), CvType.CV_8U);
        Mat gray = new Mat();

        Imgproc.pyrDown(image, pyramid, new Size(image.cols() / 2, image.rows() / 2));
        outputImages.add(pyramid.clone());
        Imgproc.pyrUp(pyramid, tempImg, image.size());

        // Imgproc.blur(image, tempImg, new Size(6, 6));

        outputImages.add(tempImg.clone());

        // find squares in every color plane of the image
        for (int c = 0; c < 3; c++) {
            MatOfInt fromTo = new MatOfInt(c, 0);

            List<Mat> tempImgList = new ArrayList<Mat>(1);
            List<Mat> gray0List = new ArrayList<Mat>(1);
            tempImgList.add(tempImg);
            gray0List.add(gray0);
            Core.mixChannels(tempImgList, gray0List, fromTo);

            outputImages.add(gray0.clone());

            // // try several threshold levels
            for (int l = 0; l < N; l++) {
                // hack: use Canny instead of zero threshold level.
                // Canny helps to catch squares with gradient shading
                if (l == 0) {
                    // apply Canny. Take the upper threshold from slider
                    // and set the lower to 0 (which forces edges merging)
                    Imgproc.Canny(gray0, gray, (double) 0, (double) thresh, 5, false);
                    outputImages.add(gray.clone());
                    // dilate canny output to remove potential
                    // holes between edge segments
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), DILATION_ITERATIONS);
                    outputImages.add(gray.clone());
                } else {
                    // apply threshold if l!=0:
                    Imgproc.threshold(gray0, gray, (l + 1) * 255 / N, 255, Imgproc.THRESH_BINARY);
                    // gray = gray0 >= (l + 1) * 255 / N; // The c++ way I think
                    // the threshold call above does the same as the >= operator
                    outputImages.add(gray.clone());
                }

                // find contours and store them all as a list
                Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                Mat contourDrawing = gray.clone();
                Imgproc.cvtColor(contourDrawing, contourDrawing, Imgproc.COLOR_GRAY2BGR);
                Imgproc.drawContours(contourDrawing, contours, -1, new Scalar(0,255,0), 1);
                outputImages.add(contourDrawing);

                System.out.println("Contours size = " + contours.size());
                // // test each contour

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
                            double cosine = Math.abs(angle(approxArray[j % 4], approxArray[j - 2], approxArray[j - 1]));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        // if cosines of all angles are small
                        // (all angles are ~90 degree) then write quandrange
                        // vertices to resultant sequence
                        if (maxCosine < 0.3) {
                            Mat detectedImage = new Mat();
                            Imgproc.cvtColor(gray, detectedImage, Imgproc.COLOR_GRAY2BGR);
                            List<MatOfPoint> ffs = new ArrayList<>();
                            ffs.add(approx);
                            Core.polylines(detectedImage, ffs, true, new Scalar(0, 255, 0));
                            outputImages.add(detectedImage);
                            // Here is where the real output is
                            // squares.push_back(approx);
                        }
                    }
                }
                contours.clear();
            }
        }

        return outputImages;
    }

    /**
     * helper function: finds a cosine of angle between vectors from pt0->pt1
     * and from pt0->pt2 <br>
     * Although I think the dot product should also be able to do this, its close but I dont get the cosine part
     **/
    static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }
}
