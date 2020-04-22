package jwblangley.focalPointProjectReviewPdfCompiler.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.NoOverwritePDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.PDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.FocalPointProjectReviewPDFNamer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

public class FocalPointProjectReviewPDFCompiler {

  public static void compilePDF(File suffix, File document) throws IOException {
    compilePDF(suffix, document, new File("").getAbsoluteFile());
  }

  public static void compilePDF(
      File focalPointDocumentFile,
      File projectReviewPageFile,
      File outputDirectory) throws IOException {

    PDDocument focalPointDocument = PDDocument.load(focalPointDocumentFile);
    PDDocument projectReviewPage = PDDocument.load(projectReviewPageFile);

    Splitter splitter = new Splitter();
    PDFNamer pdfNamer = new NoOverwritePDFNamer(new FocalPointProjectReviewPDFNamer(), outputDirectory);

    List<PDDocument> pages = splitter.split(focalPointDocument);

    Queue<PDDocument> pageQueue = new LinkedList<>();

    for (PDDocument page : pages) {
      pageQueue.add(page);

      if (endOfSectionAt(page)) {
        compileSection(pageQueue, projectReviewPage);
        assert pageQueue.isEmpty(): "compileSection must consume all pages on the page queue";
      }
    }

    assert pageQueue.isEmpty(): "Not all pages were consumed from the queue";

    // Close documents
    focalPointDocument.close();
    projectReviewPage.close();
  }

  private static boolean endOfSectionAt(PDDocument page) {
    // TODO
    return false;
  }

  private static void compileSection(Queue<PDDocument> pageQueue, PDDocument projectReviewPage) {
    // TODO
//    PDFMergerUtility merger = new PDFMergerUtility();
//
//    PDDocument resultDoc = new PDDocument();
//
//    merger.appendDocument(resultDoc, page);
//    merger.appendDocument(resultDoc, suffixPage);
//
//    File resultFile = new File(outputDirectory, pdfNamer.namePDF(resultDoc));
//
//    resultDoc.save(resultFile.getPath());
//    resultDoc.close();
//    page.close();
  }


}
