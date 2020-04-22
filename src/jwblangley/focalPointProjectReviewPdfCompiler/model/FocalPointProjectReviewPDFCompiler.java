package jwblangley.focalPointProjectReviewPdfCompiler.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.NoOverwritePDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.PDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.FocalPointProjectReviewPDFNamer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

public class FocalPointProjectReviewPDFCompiler {

  public static void compilePDF(
      File focalPointDocumentFile,
      File projectReviewPageFile,
      File outputDirectory) throws IOException {

    PDDocument focalPointDocument = PDDocument.load(focalPointDocumentFile);
    PDDocument projectReviewPage = PDDocument.load(projectReviewPageFile);

    // TODO:

    Splitter splitter = new Splitter();
    PDFNamer pdfNamer = new NoOverwritePDFNamer(new FocalPointProjectReviewPDFNamer(), outputDirectory);

    List<PDDocument> pages = splitter.split(mainDoc);

    for (PDDocument page : pages) {
      PDFMergerUtility merger = new PDFMergerUtility();

      PDDocument resultDoc = new PDDocument();

      merger.appendDocument(resultDoc, page);
      merger.appendDocument(resultDoc, suffixPage);

      File resultFile = new File(outputDirectory, pdfNamer.namePDF(resultDoc));

      resultDoc.save(resultFile.getPath());
      resultDoc.close();
      page.close();
    }

    // Close documents
    suffixPage.close();
    mainDoc.close();
  }

  public static void compilePDF(File suffix, File document) throws IOException {
    compilePDF(suffix, document, new File("").getAbsoluteFile());
  }

}