package jwblangley.focalPointProjectReviewPdfCompiler.filenamer;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;

public class NoOverwritePDFNamer implements PDFNamer {

  private final PDFNamer baseNamer;
  private final File parentDirectory;

  public NoOverwritePDFNamer(PDFNamer baseNamer, File parentDirectory) {
    this.baseNamer = baseNamer;
    this.parentDirectory = parentDirectory;
  }

  public NoOverwritePDFNamer(PDFNamer baseNamer) {
    this(baseNamer, new File("").getAbsoluteFile());
  }

  @Override
  public String namePDF(PDDocument document) {
    int copyId = 0;
    final String pdfName = baseNamer.namePDF(document);
    final String documentName = pdfName.substring(0, pdfName.indexOf('.'));

    File file = new File(parentDirectory, pdfName);
    String resultName = pdfName;

    while (file.exists()) {
      copyId++;
      resultName = String.format("%s-%d.pdf", documentName, copyId);
      file = new File(parentDirectory, resultName);
    }
    return resultName;
  }
}
