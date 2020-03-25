package jwblangley.pdfSuffixInterleaver.view;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jwblangley.pdfSuffixInterleaver.controller.Controller;

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

    BorderPane rootNode = new BorderPane();

    HBox buttonLayout = new HBox(5);
    buttonLayout.setPrefSize(WIDTH, HEIGHT);

    selectSuffixButton = new Button("Select suffix file");
    selectSuffixButton.setPrefSize(WIDTH / 3, HEIGHT);
    selectSuffixButton.setFont(font);
    selectSuffixButton.wrapTextProperty().setValue(true);
    selectSuffixButton.setOnAction(this::handleSelectButton);

    selectDocumentButton = new Button("Select document file");
    selectDocumentButton.setPrefSize(WIDTH / 3, HEIGHT);
    selectDocumentButton.setFont(font);
    selectDocumentButton.wrapTextProperty().setValue(true);
    selectDocumentButton.setOnAction(this::handleSelectButton);

    Button goButton = new Button("Go!");
    goButton.setPrefSize(WIDTH / 3, HEIGHT);
    goButton.setFont(font);
    goButton.setOnAction(e -> controller.runInterleaver());

    buttonLayout.getChildren().addAll(selectSuffixButton, selectDocumentButton, goButton);

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

  private void handleSelectButton(ActionEvent e) {
    // File chooser
    FileChooser fc = new FileChooser();
    fc.setTitle("Image file to reveal from");
    FileChooser.ExtensionFilter pdfFilter
        = new ExtensionFilter("PDF Documents", "*.pdf");
    fc.getExtensionFilters().add(pdfFilter);


    Button source = (Button) e.getSource();
    File chosenFile = fc.showOpenDialog(source.getScene().getWindow());

    if (source == selectSuffixButton) {
      controller.setSuffixPdf(chosenFile);
    } else if (source == selectDocumentButton) {
      controller.setDocumentPdf(chosenFile);
    } else {
      throw new UnsupportedOperationException("Unexpected Button Press");
    }
  }

}
