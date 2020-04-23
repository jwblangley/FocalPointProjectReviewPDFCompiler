package jwblangley.focalPointProjectReviewPdfCompiler.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.NoOverwritePDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.PDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.StringPDFNamer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class FocalPointProjectReviewPDFCompiler {

  private static final String END_OF_SECTION_TOKEN = "Sub Total Project";

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
    List<PDDocument> pages = splitter.split(focalPointDocument);

    Queue<PDDocument> pageQueue = new LinkedList<>();

    for (PDDocument page : pages) {
      pageQueue.add(page);

      if (endOfSectionAt(page)) {
        compileSection(pageQueue, projectReviewPage, outputDirectory);
        assert pageQueue.isEmpty(): "compileSection must consume all pages on the page queue";
      }
    }

    assert pageQueue.isEmpty(): "Not all pages were consumed from the queue";

    // Close documents
    focalPointDocument.close();
    projectReviewPage.close();
  }

  private static boolean endOfSectionAt(PDDocument page) throws IOException {
    PDFTextStripper textStripper = new PDFTextStripper();
    String pageContent = textStripper.getText(page);

    Matcher endTokenMatcher = Pattern.compile(END_OF_SECTION_TOKEN).matcher(pageContent);
    return endTokenMatcher.find();
  }

  private static void compileSection(
      Queue<PDDocument> pageQueue,
      PDDocument projectReviewPage,
      File outputDirectory) throws IOException {

    PDFMergerUtility merger = new PDFMergerUtility();
    PDDocument resultDoc = new PDDocument();

    // First page will always be existing project review page TODO: check
    PDDocument frontPage = pageQueue.poll();

    // Extract information from page
    Map<String, String> extractedInformation = new HashMap<>();

    // TODO

    // Replace first page
    PDDocument filledPVPage = fillProjectReviewPage(projectReviewPage, extractedInformation);
//    merger.appendDocument(resultDoc, filledPVPage);
//    filledPVPage.close();
    frontPage.close();

    // Read from pageQueue
    while (!pageQueue.isEmpty()) {
      PDDocument page = pageQueue.poll();
      merger.appendDocument(resultDoc, page);
      page.close();
    }

    String resultName = "testOutput.pdf";
    PDFNamer pdfNamer = new NoOverwritePDFNamer(new StringPDFNamer(resultName), outputDirectory);

    File resultFile = new File(outputDirectory, pdfNamer.namePDF(resultDoc));

    resultDoc.save(resultFile.getPath());
    resultDoc.close();
  }

  private static PDDocument fillProjectReviewPage(PDDocument projectReviewPage, Map<String, String> formFields) {
    // TODO
    return null;
  }


}
