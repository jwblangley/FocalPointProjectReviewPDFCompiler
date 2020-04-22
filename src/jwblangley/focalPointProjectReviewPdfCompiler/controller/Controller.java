package jwblangley.focalPointProjectReviewPdfCompiler.controller;

import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jwblangley.focalPointProjectReviewPdfCompiler.view.ViewLayout;

public class Controller extends Application {

  private static final String VERSION = "v0.0.0";

  private ViewLayout layout;

  private File focalPointDocument;
  private File projectReviewPage;
  private File outputDirectory;

  public void setFocalPointDocument(File focalPointDocument) {
    this.focalPointDocument = focalPointDocument;
  }

  public void setProjectReviewPage(File projectReviewPage) {
    this.projectReviewPage = projectReviewPage;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void runPDFCompiler() {
    if (focalPointDocument == null) {
      layout.reportStatus("Please select a FocalPoint output pdf", false);
      return;
    }
    if (projectReviewPage == null) {
      layout.reportStatus("Please select a project review page pdf", false);
      return;
    }
    if (outputDirectory == null) {
      layout.reportStatus("Please select an output directory", false);
      return;
    }
    layout.reportStatus("Working...", true);
    new Thread(() -> {
      // TODO: Run model

      Platform.runLater(() -> layout.reportStatus("Process complete", true));
    }).start();

  }

  @Override
  public void start(Stage stage) throws Exception {
    layout = new ViewLayout(this);
    Scene scene = new Scene(layout.layout(stage));

    stage.setTitle("FocalPoint Project Review PDF Compiler - " + VERSION);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }
}
