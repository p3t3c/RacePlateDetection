package race.plate.detection;

import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPaneBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import org.opencv.core.Mat;

import com.sun.javafx.accessible.utils.ProviderOptions;

import race.plate.detection.imageprocessing.BoundingBoxesAroundContoursAlgorithm;
import race.plate.detection.imageprocessing.FindRectangles2Algorithm;
import race.plate.detection.imageprocessing.FindRectanglesAlgorithm;
import race.plate.detection.util.MatFXConversionUtil;

public class OldFXMain extends Application {
    // private static final String DEFAULT_IMAGE_FILE_NAME = "/pic5.png";
    // private static final String DEFAULT_IMAGE_FILE_NAME = "/lena.png";
    private static final String DEFAULT_IMAGE_FILE_NAME = "/BoxPlate1.jpg";
    private ImageView outputImageView;
    private ImageView inputImageView;
    private ImageControl imageControl;
    private Mat[] outputImages;
    private int currentOutputImageIndex = 0;
    private StringProperty currentIndexProperty;

    @Override
    public void start(Stage primaryStage) {
        try {
            imageControl = new ImageControl();
            imageControl.loadImageAsResource(DEFAULT_IMAGE_FILE_NAME);
            outputImages = imageControl.getOutputImageSequence();
            currentOutputImageIndex = 0;

            Scene scene = buildScene();
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Scene buildScene() {
        Scene scene = new Scene(buildRootNode());
        return scene;
    }

    private Parent buildRootNode() {
        BorderPane root = new BorderPane();
        root.setCenter(buildImagePanels());
        root.setBottom(buildButtonPanel());
        return root;
    }

    private Node buildButtonPanel() {
        HBox buttonPanel = new HBox();
        Button button = new Button("Do Stuff");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                // imageControl.applyProcessing(new
                // FrontalFaceDetectionAlgorithm());
                imageControl.applyProcessing(new FindRectanglesAlgorithm());
                // imageControl.applyProcessing(new
                // BoundingBoxesAroundContoursAlgorithm());
                outputImages = imageControl.getOutputImageSequence();
                currentOutputImageIndex = outputImages.length - 1;
                updateOutputImage();
            }
        });
        buttonPanel.getChildren().add(button);
        return buttonPanel;
    }

    private void updateOutputImage() {
        System.out.println(currentOutputImageIndex);
        outputImageView.setImage(MatFXConversionUtil.convertToFxImage(outputImages[currentOutputImageIndex]));
        currentIndexProperty.setValue("" + currentOutputImageIndex);
    }

    private Node buildImagePanels() {
        HBox root = new HBox();

        root.getChildren().add(buildInputImage());

        StackPane stackPane = new StackPane();

        ImageView outputImageView = buildOutputImage();
        stackPane.getChildren().add(outputImageView);

        Label currentIndexText = new Label();
        currentIndexText.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        currentIndexProperty = currentIndexText.textProperty();
        StackPane.setAlignment(currentIndexText, Pos.TOP_RIGHT);
        stackPane.getChildren().add(currentIndexText);

        root.getChildren().add(stackPane);

        outputImageView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    currentOutputImageIndex++;
                    currentOutputImageIndex = currentOutputImageIndex % outputImages.length;
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    currentOutputImageIndex--;
                    if (currentOutputImageIndex < 0) {
                        currentOutputImageIndex = outputImages.length - 1;
                    }
                }
                updateOutputImage();
            }
        });

        // Doesn't work SccrollPanes still suck apparently
        // ScrollPane scrollPaneNode = new ScrollPane();
        // scrollPaneNode.setFitToHeight(true);
        // scrollPaneNode.setFitToWidth(true);
        // scrollPaneNode.setContent(root);
        return root;
    }

    private ImageView buildOutputImage() {
        Image outputImage = MatFXConversionUtil.convertToFxImage(imageControl.getLatestOutputImage());
        currentOutputImageIndex = 0;
        outputImageView = new ImageView(outputImage);
        outputImageView.setFitWidth(600);
        outputImageView.setPreserveRatio(true);
        
        return outputImageView;
    }

    private Node buildInputImage() {
        Image inputImage = MatFXConversionUtil.convertToFxImage(imageControl.getInputImage());
        inputImageView = new ImageView(inputImage);
        
//        inputImageView.setFitWidth(600);
        inputImageView.setPreserveRatio(true);
        
        ScrollPane scrollPaneNode = new ScrollPane();
//        scrollPaneNode.setFitToHeight(true);
        scrollPaneNode.setFitToWidth(true);
        scrollPaneNode.setContent(inputImageView);
        scrollPaneNode.setPrefWidth(600);
        HBox.setHgrow(scrollPaneNode, Priority.ALWAYS);
        

        return scrollPaneNode;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
