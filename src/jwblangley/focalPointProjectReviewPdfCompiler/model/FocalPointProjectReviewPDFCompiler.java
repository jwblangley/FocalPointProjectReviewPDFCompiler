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

  private static final String PROJECT_CODE_EX = "Project\\s*?Review\\s*?(.+?)\\s";
  private static final String PROJECT_CODE_LABEL = "Project code";

  // N.B: might be left blank
  private static final String ESTIMATED_PROJECT_VALUE_EX = "Project\\s*?Manager:\\s+?(.*)$";
  private static final String ESTIMATED_PROJECT_VALUE_LABEL = "Estimated total";

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

    assert pageQueue.size() >= 2;

    PDFMergerUtility merger = new PDFMergerUtility();
    PDDocument resultDoc = new PDDocument();

    // Extract information from first two pages
    Map<String, String> extractedInformation = new HashMap<>();
    PDFTextStripper textStripper = new PDFTextStripper();

    // Extract information from first page
    PDDocument firstPage = pageQueue.poll();
    String firstPageContent = textStripper.getText(firstPage);

    System.out.println(firstPageContent);
    System.out.println("---------------------1/2----------------------------");

    // Project code
    Matcher projectCodeMatcher = Pattern.compile(PROJECT_CODE_EX).matcher(firstPageContent);
    // TODO: error handle
    projectCodeMatcher.find();
    String projectCodeMatch = projectCodeMatcher.group(1);
    extractedInformation.put(PROJECT_CODE_LABEL, projectCodeMatch);

    // Project code
    Matcher estimatedProjectValueMatcher = Pattern.compile(ESTIMATED_PROJECT_VALUE_EX, Pattern.MULTILINE).matcher(firstPageContent);
    // TODO: error handle
    estimatedProjectValueMatcher.find();
    String estimatedProjectValueMatch = estimatedProjectValueMatcher.group(1);
    extractedInformation.put(ESTIMATED_PROJECT_VALUE_LABEL, estimatedProjectValueMatch);

    PDDocument secondPage = pageQueue.poll();
    String secondPageContent = textStripper.getText(secondPage);

    System.out.println(secondPageContent);
    System.out.println("---------------------2/2----------------------------");


    // Replace first
//    PDDocument filledPVPage = fillProjectReviewPage(projectReviewPage, extractedInformation);
//    merger.appendDocument(resultDoc, filledPVPage);
//    filledPVPage.close();
    firstPage.close();



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
    return projectReviewPage;
  }


}
