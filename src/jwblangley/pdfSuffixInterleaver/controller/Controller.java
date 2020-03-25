package jwblangley.pdfSuffixInterleaver.controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jwblangley.pdfSuffixInterleaver.view.ViewLayout;

public class Controller extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    Scene scene = new Scene(ViewLayout.layout(stage));

    stage.setTitle("PDF Suffix Interleaver");
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }
}
