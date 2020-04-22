package jwblangley.focalPointProjectReviewPdfCompiler.view;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jwblangley.focalPointProjectReviewPdfCompiler.controller.Controller;

public class ViewLayout {

  private static final int WIDTH = 600;
  private static final int HEIGHT = 150;

  private final Controller controller;

  private Button selectSuffixButton;
  private Button selectDocumentButton;

  private Label statusLabel;

  public ViewLayout(Controller controller) {
    this.controller = controller;
  }

  public Pane layout(Stage window) {
    Font font = new Font(18);

    // TODO:

    BorderPane rootNode = new BorderPane();

    HBox buttonLayout = new HBox(5);
    buttonLayout.setPrefSize(WIDTH, HEIGHT);

    selectDocumentButton = new Button("Select Paprika file");
    selectDocumentButton.setPrefSize(WIDTH / 4, HEIGHT);
    selectDocumentButton.setFont(font);
    selectDocumentButton.wrapTextProperty().setValue(true);
    selectDocumentButton.setTextAlignment(TextAlignment.CENTER);
    selectDocumentButton.setOnAction(this::handleSelectFileButton);

    selectSuffixButton = new Button("Select Project Review file");
    selectSuffixButton.setPrefSize(WIDTH / 4, HEIGHT);
    selectSuffixButton.setFont(font);
    selectSuffixButton.wrapTextProperty().setValue(true);
    selectSuffixButton.setTextAlignment(TextAlignment.CENTER);
    selectSuffixButton.setOnAction(this::handleSelectFileButton);

    Button selectOutputDirButton = new Button("Select Output Directory");
    selectOutputDirButton.setPrefSize(WIDTH / 4, HEIGHT);
    selectOutputDirButton.setFont(font);
    selectOutputDirButton.wrapTextProperty().setValue(true);
    selectOutputDirButton.setTextAlignment(TextAlignment.CENTER);
    selectOutputDirButton.setOnAction(e -> {
      DirectoryChooser dc = new DirectoryChooser();
      dc.setTitle("Select output directory");

      File chosenDirecory = dc.showDialog(window);
      controller.setOutputDirectory(chosenDirecory);
    });

    Button goButton = new Button("Go!");
    goButton.setPrefSize(WIDTH / 4, HEIGHT);
    goButton.setFont(font);
    goButton.setTextAlignment(TextAlignment.CENTER);
    goButton.setOnAction(e -> controller.runPDFCompiler());

    buttonLayout.getChildren().addAll(
        selectDocumentButton,
        selectSuffixButton,
        selectOutputDirButton,
        goButton
    );

    rootNode.setTop(buttonLayout);

    statusLabel = new Label("Select a file to begin");
    statusLabel.setFont(font);
    rootNode.setCenter(statusLabel);

    return rootNode;
  }

  public void reportStatus(String status, boolean successful) {
    statusLabel.setText(status);
    statusLabel.setTextFill(successful ? Color.GREEN : Color.RED);
  }

  private void handleSelectFileButton(ActionEvent e) {
    // File chooser
    Button source = (Button) e.getSource();

    FileChooser fc = new FileChooser();
    fc.setTitle(source == selectSuffixButton
        ? "Select project review file"
        : "Select Paprika file"
    );

    ExtensionFilter pdfFilter = new ExtensionFilter("PDF Documents", "*.pdf");
    fc.getExtensionFilters().add(pdfFilter);

    File chosenFile = fc.showOpenDialog(source.getScene().getWindow());

    if (source == selectSuffixButton) {
      controller.setProjectReviewPage(chosenFile);
    } else if (source == selectDocumentButton) {
      controller.setFocalPointDocument(chosenFile);
    } else {
      throw new UnsupportedOperationException("Unexpected Button Press");
    }
  }

}
