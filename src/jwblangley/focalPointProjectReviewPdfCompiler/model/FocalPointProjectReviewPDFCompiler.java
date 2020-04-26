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

  // First page expressions and labels
  private static final String PROJECT_CODE_EX = "Project\\s*?Review\\s*?(.+?)\\s";
  private static final String PROJECT_CODE_LABEL = "Project code";

  private static final String DATE_EX = "Project\\s*?Director\\s*?by\\s*(\\d\\d/\\d\\d/\\d\\d\\d\\d)";
  private static final String DATE_LABEL = "By date";

  // N.B: might be left blank
  private static final String EPV_AND_PorO_EX
      = "Project\\s*?Manager:\\s*?$\\s*(.*?)$?\\s*.+?$\\s*(.+)?$\\s*Project\\s*?title";
  private static final String ESTIMATED_PROJECT_VALUE_LABEL = "Estimated total";
  private static final String PROFIT_OR_OVERRUN_LABEL = "Profit or Overrun";

  private static final String INVOICED_TO_DATE_AND_CURRENT_PROJECT_POSITION_EX
      = "Project\\s*?title:\\s*?$\\s*(.*?)$\\s*(.*?)$";
  private static final String INVOICED_TO_DATE_LABEL = "Total invoiced";
  private static final String CURRENT_PROJECT_POSITION_LABEL = "Current position";

  // Second page expressions and labels
  private static final String PROJECT_TITLE_EX = "Project:\\s*(.+?)\\s*Director";
  private static final String PROJECT_TITLE_LABEL = "Project title";

  private static final String PROJECT_DIRECTOR_EX = "Director:\\s*(.+?)\\s*Project\\s*Manager";
  private static final String PROJECT_DIRECTOR_LABEL = "Project Director";

  private static final String PROJECT_MANAGER_EX = "Manager:\\s*(.+?)\\s*?$";
  private static final String PROJECT_MANAGER_LABEL = "Project manager";

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

    boolean internalProjectReview = firstPageContent.startsWith("Internal");

    // Project code
    Matcher projectCodeMatcher = Pattern.compile(PROJECT_CODE_EX).matcher(firstPageContent);
    // TODO: error handle
    projectCodeMatcher.find();
    String projectCodeMatch = projectCodeMatcher.group(1);
    extractedInformation.put(PROJECT_CODE_LABEL, projectCodeMatch);

    // Date
    Matcher dateMatcher = Pattern.compile(DATE_EX).matcher(firstPageContent);
    // TODO: error handle
    dateMatcher.find();
    String dateMatch = dateMatcher.group(1);
    extractedInformation.put(DATE_LABEL, dateMatch);

    // Estimated Project Value
    Matcher epvAndPorOMatcher = Pattern.compile(EPV_AND_PorO_EX, Pattern.MULTILINE).matcher(firstPageContent);
    // TODO: error handle
    epvAndPorOMatcher.find();
    String estimatedProjectValueMatch = epvAndPorOMatcher.group(1);
    String profitOrOverrunMatch = epvAndPorOMatcher.group(2);
    extractedInformation.put(ESTIMATED_PROJECT_VALUE_LABEL, estimatedProjectValueMatch);
    extractedInformation.put(PROFIT_OR_OVERRUN_LABEL, profitOrOverrunMatch);

    // Invoiced to date and current project position
    Matcher invoicedToDateAndCurrentProjectPositionMatcher
        = Pattern.compile(INVOICED_TO_DATE_AND_CURRENT_PROJECT_POSITION_EX, Pattern.MULTILINE).matcher(firstPageContent);
    // TODO: error handle
    invoicedToDateAndCurrentProjectPositionMatcher.find();
    String invoicedToDate = invoicedToDateAndCurrentProjectPositionMatcher.group(1);
    String currentProjectPosition = invoicedToDateAndCurrentProjectPositionMatcher.group(2);
    extractedInformation.put(INVOICED_TO_DATE_LABEL, invoicedToDate);
    extractedInformation.put(CURRENT_PROJECT_POSITION_LABEL, currentProjectPosition);

    // Extract information from second page
    PDDocument secondPage = pageQueue.poll();
    String secondPageContent = textStripper.getText(secondPage);

    // Project Title
    Matcher projectTitleMatcher = Pattern.compile(PROJECT_TITLE_EX).matcher(secondPageContent);
    // TODO: error handle
    projectTitleMatcher.find();
    String projectTitleMatch = projectTitleMatcher.group(1);
    extractedInformation.put(PROJECT_TITLE_LABEL, projectTitleMatch);

    // Project Director
    Matcher projectDirectorMatcher = Pattern.compile(PROJECT_DIRECTOR_EX).matcher(secondPageContent);
    // TODO: error handle
    projectDirectorMatcher.find();
    String projectDirectorMatch = projectDirectorMatcher.group(1);
    extractedInformation.put(PROJECT_DIRECTOR_LABEL, projectDirectorMatch);

    // Project Manager
    Matcher projectManagerMatcher = Pattern.compile(PROJECT_MANAGER_EX, Pattern.MULTILINE).matcher(secondPageContent);
    // TODO: error handle
    projectManagerMatcher.find();
    String projectManagerMatch = projectManagerMatcher.group(1);
    extractedInformation.put(PROJECT_MANAGER_LABEL, projectManagerMatch);


//    for (String key: extractedInformation.keySet()) {
//      System.out.println(key + ": " + extractedInformation.get(key));
//    }

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
