package jwblangley.pdfSuffixInterleaver.controller;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jwblangley.pdfSuffixInterleaver.model.PDFSuffixInterleaver;
import jwblangley.pdfSuffixInterleaver.view.ViewLayout;

public class Controller extends Application {

  private ViewLayout layout;

  private File documentPdf;
  private File suffixPdf;

  public void setDocumentPdf(File documentPdf) {
    this.documentPdf = documentPdf;
  }

  public void setSuffixPdf(File suffixPdf) {
    this.suffixPdf = suffixPdf;
  }

  public void runInterleaver() {
    if (suffixPdf == null) {
      layout.reportStatus("Please select a suffix pdf", false);
      return;
    }
    if (documentPdf == null) {
      layout.reportStatus("Please select a document pdf", false);
      return;
    }
    try {
      layout.reportStatus("Working...", true);
      PDFSuffixInterleaver.interleaveSuffixSeparateAndSave(suffixPdf, documentPdf);
      layout.reportStatus("Process complete", true);
    } catch (IOException e) {
      e.printStackTrace();
      layout.reportStatus("An error occurred", false);
    }
  }

  @Override
  public void start(Stage stage) throws Exception {
    layout = new ViewLayout(this);
    Scene scene = new Scene(layout.layout(stage));

    stage.setTitle("PDF Suffix Interleaver");
    stage.setScene(scene);
    stage.centerOnScreen();
    stage.show();
  }
}
