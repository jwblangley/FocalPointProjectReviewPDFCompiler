package pdfSuffixInterleaver.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ViewLayout extends Application {

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

  @Override
  public void start(Stage stage) throws Exception {

    Scene scene = new Scene(ViewLayout.layout(stage));

    stage.setTitle("PDF Suffix Interleaver");
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }
}
