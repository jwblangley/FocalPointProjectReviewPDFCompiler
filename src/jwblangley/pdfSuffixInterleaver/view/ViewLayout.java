package jwblangley.pdfSuffixInterleaver.view;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ViewLayout {

  private static final int WIDTH = 500;
  private static final int HEIGHT = 200;

  public static Pane layout(Stage window) {
    HBox layout = new HBox(5);
    layout.setPrefSize(WIDTH, HEIGHT);

    Button selectSuffixButton = new Button("Select suffix file");
    selectSuffixButton.setPrefSize(WIDTH / 2, HEIGHT);
    selectSuffixButton.setOnAction(e -> System.out.println("select suffix"));

    Button selectDocumentButton = new Button("Select document file");
    selectDocumentButton.setPrefSize(WIDTH / 2, HEIGHT);
    selectDocumentButton.setOnAction(e -> System.out.println("select document"));

    layout.getChildren().addAll(selectSuffixButton, selectDocumentButton);

    return layout;
  }

}
