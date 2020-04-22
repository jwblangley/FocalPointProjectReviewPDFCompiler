package jwblangley.focalPointProjectReviewPdfCompiler.filenamer;

import org.apache.pdfbox.pdmodel.PDDocument;

public class StringPDFNamer implements PDFNamer {

  private final String name;

  public StringPDFNamer(String name) {
    this.name = name;
  }

  @Override
  public String namePDF(PDDocument document) {
    return name;
  }
}
