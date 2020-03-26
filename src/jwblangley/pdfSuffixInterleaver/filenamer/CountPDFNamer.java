package jwblangley.pdfSuffixInterleaver.filenamer;

import org.apache.pdfbox.pdmodel.PDDocument;

public class CountPDFNamer implements PDFNamer {

  private int count = 1;

  @Override
  public String namePDF(PDDocument document) {
    return String.format("%d.pdf", count++);
  }
}
