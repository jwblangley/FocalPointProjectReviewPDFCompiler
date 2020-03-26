package jwblangley.pdfSuffixInterleaver.filenamer;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;

public class NoOverwritePDFNamer implements PDFNamer {

  private final PDFNamer baseNamer;

  public NoOverwritePDFNamer(PDFNamer baseNamer) {
    this.baseNamer = baseNamer;
  }

  @Override
  public String namePDF(PDDocument document) {
    int copyId = 0;
    final String pdfName = baseNamer.namePDF(document);
    final String documentName = pdfName.substring(0, pdfName.indexOf('.'));

    File file = new File(pdfName);
    String resultName = pdfName;

    while (file.exists()) {
      copyId++;
      resultName = String.format("%s-%d.pdf", documentName, copyId);
      file = new File(resultName);
    }
    return resultName;
  }
}
