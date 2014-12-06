package race.plate.detection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import race.plate.detection.util.MatFXConversionUtil;

public class FXMainController  {
    // private static final String DEFAULT_IMAGE_FILE_NAME = "/pic5.png";
//     private static final String DEFAULT_IMAGE_FILE_NAME = "/lena.png";
    private static final String DEFAULT_IMAGE_FILE_NAME = "/BoxPlate1.jpg";
    
    @FXML
    private Button executeTarget;

    @FXML
    private ImageView inputImageView;
    
    @FXML
    private Button loadTarget;

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
    }

   
}
