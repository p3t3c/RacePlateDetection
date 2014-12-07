package race.plate.detection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import race.plate.detection.util.MatFXConversionUtil;

public class FXMainController {
    private static final double ZOOM_SCALE_FACTOR = 1.1;
    // private static final String DEFAULT_IMAGE_FILE_NAME = "/pic5.png";
    // private static final String DEFAULT_IMAGE_FILE_NAME = "/lena.png";
    private static final String DEFAULT_IMAGE_FILE_NAME = "/BoxPlate1.jpg";

    @FXML
    private Button executeTarget;

    @FXML
    private ImageView inputImageView;

    @FXML
    private Button loadTarget;

    @FXML
    private ScrollPane inputImageScrollPane;

    private ImageControl imageControl;

    @FXML
    void initialize() {
        assert executeTarget != null : "fx:id=\"executeTarget\" was not injected: check your FXML file 'imageProcessing.fxml'.";
        assert inputImageView != null : "fx:id=\"inputImageView\" was not injected: check your FXML file 'imageProcessing.fxml'.";
        imageControl = new ImageControl();
        imageControl.loadImageAsResource(DEFAULT_IMAGE_FILE_NAME);

    }

    @FXML
    protected void handleExecuteButtonAction(ActionEvent action) {
        System.out.println("pressed");
    }

    @FXML
    void handleLoadButtonAction(ActionEvent event) {
        Image i = MatFXConversionUtil.convertToFxImage(imageControl.getInputImage());
        assert i != null : "Image conversion didn't work";
        inputImageView.setImage(i);
        inputImageView.setFitWidth(i.getWidth());
        inputImageView.setFitWidth(i.getHeight());
        // inputImageView.setViewport(new Rectangle2D(0, 0, i.getWidth(),
        // i.getHeight()));
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
     * Currently this isn't used.
     * Was attempting to get the zoom point to be on the mouse pointer rather than the center of the view port.
     * Giving up on it for the moment because it isn't really the hightest priority.
     * @param scrollEvent
     */
    private void centerScrollPaneOnMouse(ScrollEvent scrollEvent) {
        ScrollPane scrollPane = (ScrollPane) scrollEvent.getSource();
        System.out.println(scrollPane.getBoundsInLocal());
        System.out.println(scrollEvent.getX() +", " + scrollEvent.getY());
        
        System.out.println(scrollPane.getVmin() + " - " +scrollPane.getVmax());
        System.out.println(scrollPane.getHmin() + " - " +scrollPane.getHmax());
        
        Bounds scrollPaneBounds = scrollPane.getBoundsInLocal();
        double scrollCenterX =  scrollPaneBounds.getWidth() / 2; 
        double scrollCenterY =  scrollPaneBounds.getHeight() / 2;
        
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
