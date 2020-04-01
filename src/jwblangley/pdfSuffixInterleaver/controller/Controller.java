package jwblangley.pdfSuffixInterleaver.controller;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jwblangley.pdfSuffixInterleaver.model.PDFSuffixInterleaver;
import jwblangley.pdfSuffixInterleaver.view.ViewLayout;

public class Controller extends Application {

  private static final String VERSION = "v2.2.0";

  private ViewLayout layout;

  private File documentPdf;
  private File suffixPdf;
  private File outputDirectory;

  public void setDocumentPdf(File documentPdf) {
    this.documentPdf = documentPdf;
  }

  public void setSuffixPdf(File suffixPdf) {
    this.suffixPdf = suffixPdf;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void runInterleaver() {
    if (suffixPdf == null) {
      layout.reportStatus("Please select a project review pdf", false);
      return;
    }
    if (documentPdf == null) {
      layout.reportStatus("Please select a Paprika pdf", false);
      return;
    }
    if (outputDirectory == null) {
      layout.reportStatus("Please select an output directory", false);
      return;
    }
    layout.reportStatus("Working...", true);
    new Thread(() -> {
      try {
        PDFSuffixInterleaver
            .interleaveSuffixSeparateAndSave(suffixPdf, documentPdf, outputDirectory);
        Platform.runLater(() -> layout.reportStatus("Process complete", true));
      } catch (IOException e) {
        e.printStackTrace();
        Platform.runLater(() -> layout.reportStatus("An error occurred", false));
      }
    }).start();

  }

  @Override
  public void start(Stage stage) throws Exception {
    layout = new ViewLayout(this);
    Scene scene = new Scene(layout.layout(stage));

    stage.setTitle("Project Review PDF Compiler - " + VERSION);
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }
}
