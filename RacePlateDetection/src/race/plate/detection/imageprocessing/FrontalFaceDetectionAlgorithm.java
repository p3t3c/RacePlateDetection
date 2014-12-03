package race.plate.detection.imageprocessing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Taken from the OpenCV tutorial HelloOpenCV and modified to work with the FX
 * application and supporting classes
 * 
 * @author pete
 *
 */
public class FrontalFaceDetectionAlgorithm implements IProcessingAlgorithm {

    public FrontalFaceDetectionAlgorithm() {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public List<Mat> processImage(Mat inputImage) {
        System.out.println("\nRunning DetectFaceDemo");
        // In this case the output will be drawn over the top of the original
        // image
        Mat outputImage = inputImage.clone();

        // Create a face detector from the cascade file in the resources
        // directory.
        CascadeClassifier faceDetector = new CascadeClassifier(getClass().getResource("/opencv/lbpcascade_frontalface.xml").getPath().substring(1));
        System.out.println("\nClassider Loaded");

        // Detect faces in the image.
        // MatOfRect is a special container class for Rect.
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(inputImage, faceDetections);

        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {
            Core.rectangle(outputImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
        }

        ArrayList<Mat> outputList = new ArrayList<>();
        outputList.add(outputImage);
        return outputList;

    }

}
