package jwblangley.focalPointProjectReviewPdfCompiler.filenamer;

import org.apache.pdfbox.pdmodel.PDDocument;

public interface PDFNamer {

  String namePDF(PDDocument document);
}
