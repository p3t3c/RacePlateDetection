package race.plate.detection;

import org.opencv.core.Mat;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import race.plate.detection.imageprocessing.FindRectanglesAlgorithm;
import race.plate.detection.util.MatFXConversionUtil;

public class FXMainController {
    private static final double ZOOM_SCALE_FACTOR = 1.1;
    // private static final String DEFAULT_IMAGE_FILE_NAME = "/pic5.png";
    // private static final String DEFAULT_IMAGE_FILE_NAME = "/lena.png";
    private static final String DEFAULT_IMAGE_FILE_NAME = "/BoxPlate1.jpg";

    @FXML
    private Button executeTarget;

    @FXML
    private Button loadTarget;

    @FXML
    private ImageView inputImageView;

    @FXML
    private ScrollPane inputImageScrollPane;

    @FXML
    private ImageView outputImageView;

    @FXML
    private ScrollPane outputImageScrollPane;

    @FXML
    private Tooltip outputImageToolTip;

    private ImageControl imageControl;

    private SimpleIntegerProperty outputImageIndex;
    private Mat[] outputImages;

    @FXML
    void initialize() {
        assert executeTarget != null : "fx:id=\"executeTarget\" was not injected: check your FXML file 'imageProcessing.fxml'.";
        assert inputImageView != null : "fx:id=\"inputImageView\" was not injected: check your FXML file 'imageProcessing.fxml'.";
        imageControl = new ImageControl();
        imageControl.loadImageAsResource(DEFAULT_IMAGE_FILE_NAME);

        outputImageIndex = new SimpleIntegerProperty(this, "OutputImageIndex", 0);
        outputImageToolTip.textProperty().bind(outputImageIndex.asString());

        outputImageIndex.addListener(new ChangeListener<Number>() {
            // TODO Write this in a cleaner way
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Image i = MatFXConversionUtil.convertToFxImage(outputImages[newValue.intValue()]);
                outputImageView.setImage(i);
                outputImageView.setFitWidth(outputImageScrollPane.getWidth());
            }
        });
    }

    @FXML
    protected void handleExecuteButtonAction(ActionEvent action) {
        imageControl.applyProcessing(new FindRectanglesAlgorithm());
        outputImages = imageControl.getOutputImageSequence();
        outputImageIndex.set(outputImages.length - 1);
    }

    @FXML
    void handleLoadButtonAction(ActionEvent event) {
        Image i = MatFXConversionUtil.convertToFxImage(imageControl.getInputImage());
        assert i != null : "Image conversion didn't work";
        inputImageView.setImage(i);
        inputImageView.setFitWidth(inputImageScrollPane.getWidth());
    }

    @FXML
    void handleOutputImageScroll(ScrollEvent scrollEvent) {
        zoomImageView(outputImageView, scrollEvent);
        if (scrollEvent.isAltDown()) {
            // TODO there is probably a better way to wrap up the roll over with
            // a set of bindings. Probably use a customer binding override the
            // computerValue
            if (scrollEvent.getDeltaY() >= 0.0) {
                if (outputImageIndex.get() == outputImages.length - 1)
                    outputImageIndex.set(0); // Roll over to first
                else
                    outputImageIndex.set(outputImageIndex.get() + 1); // Go to
                                                                      // next
                                                                      // index

            } else {
                if (outputImageIndex.get() == 0)
                    outputImageIndex.set(outputImages.length - 1);
                else
                    outputImageIndex.set(outputImageIndex.get() - 1);
            }
            System.out.println(outputImageIndex);
        }
    }

    @FXML
    void handleInputImageScroll(ScrollEvent scrollEvent) {
        zoomImageView(inputImageView, scrollEvent);
    }

    /**
     * Scroll a given imageView based on the deltaY of the ScrollEvent. Zoom
     * occurs when the control key is depressed and the scroll wheel is used.
     * Can't use JavaFX Zoom functions on a PC as it seems vague what
     * combination a user must done.
     * 
     * @param imageView
     * @param scrollEvent
     */
    private void zoomImageView(ImageView imageView, ScrollEvent scrollEvent) {
        if (scrollEvent.isControlDown()) {
            double scrollDelta = scrollEvent.getDeltaY();
            double zoomScale = ZOOM_SCALE_FACTOR;
            if (scrollDelta <= 0.0) {
                // invert the scale factor to zoom the other way.
                zoomScale = 1 / zoomScale;
            }

            imageView.fitWidthProperty().setValue(imageView.fitWidthProperty().get() * zoomScale);
            /*
             * Expecting that preseverRatio is set. In case it isn't then do the
             * scaling of the height here.
             */
            if (!imageView.isPreserveRatio()) {
                imageView.fitHeightProperty().setValue(imageView.fitHeightProperty().get() * zoomScale);
            }
        }
    }

    /**
     * Currently this isn't used. Was attempting to get the zoom point to be on
     * the mouse pointer rather than the center of the view port. Giving up on
     * it for the moment because it isn't really the hightest priority.
     * 
     * @param scrollEvent
     */
    private void centerScrollPaneOnMouse(ScrollEvent scrollEvent) {
        ScrollPane scrollPane = (ScrollPane) scrollEvent.getSource();
        System.out.println(scrollPane.getBoundsInLocal());
        System.out.println(scrollEvent.getX() + ", " + scrollEvent.getY());

        System.out.println(scrollPane.getVmin() + " - " + scrollPane.getVmax());
        System.out.println(scrollPane.getHmin() + " - " + scrollPane.getHmax());

        Bounds scrollPaneBounds = scrollPane.getBoundsInLocal();
        double scrollCenterX = scrollPaneBounds.getWidth() / 2;
        double scrollCenterY = scrollPaneBounds.getHeight() / 2;

        double deltaX = scrollEvent.getX() - scrollCenterX;
        double deltaY = scrollEvent.getY() - scrollCenterY;

        System.out.println(deltaX + ", " + deltaY);

        // Normalise the deltas to the height and width of the bounds
        deltaX = deltaX / scrollPaneBounds.getWidth();
        deltaY = deltaY / scrollPaneBounds.getHeight();

        System.out.println("normalised " + deltaX + ", " + deltaY);

        scrollPane.setHvalue(scrollPane.getHvalue() + deltaX);
        scrollPane.setVvalue(scrollPane.getVvalue() + deltaY);

    }

}
