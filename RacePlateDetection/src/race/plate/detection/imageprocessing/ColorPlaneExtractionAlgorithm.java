package race.plate.detection.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

/**
 * Input: A Color image of at least 3 channels
 * <p>
 * Output: Grey scale images of each of the color channels. Only the color
 * channels are processed alpha channel is not included.
 * <p>
 * Channels will be output in the order of the input image. Normally images are
 * BGR. So the blue channel will be at index 0 of the output image list.
 * 
 * @author pete
 *
 */
public class ColorPlaneExtractionAlgorithm implements IProcessingAlgorithm {

    private static final int NUM_CHANNELS_TO_PROCESS = 3;

    @Override
    public List<Mat> processImage(Mat image) {
        List<Mat> outputImages = new ArrayList<Mat>();

        Mat gray0 = new Mat(image.size(), CvType.CV_8U); // This will be the
                                                         // result image for the
                                                         // iteration
        // Mix channels call just wants lists.
        List<Mat> tempImgList = new ArrayList<Mat>(1);
        List<Mat> grayImgList = new ArrayList<Mat>(1);

        for (int colorChannel = 0; colorChannel < NUM_CHANNELS_TO_PROCESS; colorChannel++) {
            /*
             * The open CV mixChannels() function needs an array of channel
             * pairs. These pairs map the from channel and indicate the channel
             * to put the value onto. Note: It was attempted to try this without
             * a loop but there were strange results.
             */
            MatOfInt fromTo = new MatOfInt(colorChannel, 0);

            tempImgList.add(image);
            grayImgList.add(gray0);

            // The real work happens here
            Core.mixChannels(tempImgList, grayImgList, fromTo);
            outputImages.add(gray0.clone()); // Clone the image to the output
                                             // list.

            // Preparation for next iteration
            tempImgList.clear();
            grayImgList.clear();
        }
        return outputImages;
    }

}
