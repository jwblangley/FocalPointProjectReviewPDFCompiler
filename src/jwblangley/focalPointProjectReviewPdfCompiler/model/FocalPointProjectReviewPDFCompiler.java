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
  private static final String INDICATES_PROBLEM = "X";


  // First page expressions and labels
  private static final String PROJECT_CODE_EX = "Project\\s*?Review\\s*?(.+?)\\s";
  private static final String PROJECT_CODE_LABEL = "Project code";

  private static final String DATE_EX = "Project\\s*?Director\\s*?by\\s*(\\d\\d/\\d\\d/\\d\\d\\d\\d)";
  private static final String DATE_LABEL = "By date";

  // N.B: Estimated project value might be left blank
  private static final String EPV_AND_PorO_EX
      = "Project\\s*?Manager:\\s*?$\\s*(.*?)$?\\s*.+?$\\s*(.+)?$\\s*Project\\s*?title";
  private static final String ESTIMATED_PROJECT_VALUE_LABEL = "Estimated total";
  private static final String PROFIT_OR_OVERRUN_LABEL = "Profit or Overrun";

  private static final String INVOICED_TO_DATE_AND_CURRENT_PROJECT_POSITION_EX
      = "Project\\s*?title:\\s*?$\\s*(.*?)$\\s*(.*?)$";
  private static final String INVOICED_TO_DATE_LABEL = "Total invoiced";
  private static final String CURRENT_PROJECT_POSITION_LABEL = "Current position";

  // Internal Project Review Items
  private static final String INTERNAL_PROJECT_REVIEW_EX
      = "Project\\s*?title:\\s*?$\\s*(.+?)$\\s*(.+?)$\\s*(.+?)$\\s*(.+?)$\\s*Confidential";
  private static final String TOTAL_BUDGET_LABEL = "Total budget";
  private static final String TOTAL_COMMITTED_LABEL = "Total committed";
  private static final String BUDGET_POSITION_VALUE_LABEL = "Budget position value";
  private static final String BUDGET_POSITION_TEXT_LABEL = "Budget position text";


  // Second page expressions and labels
  private static final String PROJECT_TITLE_EX = "Project:\\s*(.+?)\\s*Director";
  private static final String PROJECT_TITLE_LABEL = "Project title";

  private static final String PROJECT_DIRECTOR_EX = "Director:\\s*(.+?)\\s*Project\\s*Manager";
  private static final String PROJECT_DIRECTOR_LABEL = "Project Director";

  private static final String PROJECT_MANAGER_EX = "Manager:\\s*(.+?)\\s*?$";
  private static final String PROJECT_MANAGER_LABEL = "Project manager";

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
    PDDocument firstPage = pageQueue.poll();
    PDDocument secondPage = pageQueue.poll();
    Map<String, String> extractionMap = new HashMap<>();
    boolean allExtractionSuccess = extractInformationIntoMap(firstPage, secondPage, extractionMap);

    // Replace first page with project review page (filled if success)
    if (allExtractionSuccess) {
      PDDocument filledPVPage = fillProjectReviewPage(projectReviewPage, extractionMap);
      merger.appendDocument(resultDoc, filledPVPage);
      filledPVPage.close();
    } else {
      merger.appendDocument(resultDoc, projectReviewPage);
    }

    // Append second page
    merger.appendDocument(resultDoc, secondPage);

    // Close pages
    firstPage.close();
    secondPage.close();

    // Read any remaining pages from the page queue
    while (!pageQueue.isEmpty()) {
      PDDocument page = pageQueue.poll();
      merger.appendDocument(resultDoc, page);
      page.close();
    }

    final String gap = " ";
    String projectManager = extractionMap.get(PROJECT_MANAGER_LABEL);
    projectManager = projectManager == null ? gap : projectManager;

    String projectCode = extractionMap.get(PROJECT_CODE_LABEL);
    projectCode = projectCode == null ? gap : projectCode;

    String date = extractionMap.get(DATE_LABEL);
    date = date == null ? gap : date.replaceAll("/", "_");

    String showProblem = allExtractionSuccess ? "" : INDICATES_PROBLEM;

    String resultName = String.format("%s-%s-%s%s.pdf", projectManager, projectCode, date, showProblem);
    PDFNamer pdfNamer = new NoOverwritePDFNamer(new StringPDFNamer(resultName), outputDirectory);

    File resultFile = new File(outputDirectory, pdfNamer.namePDF(resultDoc));

    resultDoc.save(resultFile.getPath());
    resultDoc.close();
  }

  private static boolean extractInformationIntoMap(
      PDDocument firstPage, PDDocument secondPage, Map<String, String> extractionMap)
      throws IOException {

    boolean allSucceed = true;

    PDFTextStripper textStripper = new PDFTextStripper();

    // Extract information from first page
    String firstPageContent = textStripper.getText(firstPage);

    final boolean internalProjectReview = firstPageContent.startsWith("Internal");

    // Project code
    Matcher projectCodeMatcher = Pattern.compile(PROJECT_CODE_EX).matcher(firstPageContent);
    if (!projectCodeMatcher.find()) {
      allSucceed = false;
    } else {
      String projectCodeMatch = projectCodeMatcher.group(1);
      extractionMap.put(PROJECT_CODE_LABEL, projectCodeMatch);
    }

    // Date
    Matcher dateMatcher = Pattern.compile(DATE_EX).matcher(firstPageContent);
    if (!dateMatcher.find()) {
      allSucceed = false;
    } else {
      String dateMatch = dateMatcher.group(1);
      extractionMap.put(DATE_LABEL, dateMatch);
    }

    if (!internalProjectReview) {

      // Estimated Project Value and Profit or Overrun
      Matcher epvAndPorOMatcher = Pattern.compile(EPV_AND_PorO_EX, Pattern.MULTILINE)
          .matcher(firstPageContent);

      if (!epvAndPorOMatcher.find()) {
        allSucceed = false;
      } else {
        String estimatedProjectValueMatch = epvAndPorOMatcher.group(1);
        String profitOrOverrunMatch = epvAndPorOMatcher.group(2);
        extractionMap.put(ESTIMATED_PROJECT_VALUE_LABEL, estimatedProjectValueMatch);
        extractionMap.put(PROFIT_OR_OVERRUN_LABEL, profitOrOverrunMatch);
      }

      // Invoiced to date and current project position
      Matcher invoicedToDateAndCurrentProjectPositionMatcher
          = Pattern.compile(INVOICED_TO_DATE_AND_CURRENT_PROJECT_POSITION_EX, Pattern.MULTILINE)
          .matcher(firstPageContent);

      if (!invoicedToDateAndCurrentProjectPositionMatcher.find()) {
        allSucceed = false;
      } else {
        String invoicedToDate = invoicedToDateAndCurrentProjectPositionMatcher.group(1);
        String currentProjectPosition = invoicedToDateAndCurrentProjectPositionMatcher.group(2);
        extractionMap.put(INVOICED_TO_DATE_LABEL, invoicedToDate);
        extractionMap.put(CURRENT_PROJECT_POSITION_LABEL, currentProjectPosition);
      }

    } else {
      // Internal Project Review
      Matcher internalProjectReviewMatcher
          = Pattern.compile(INTERNAL_PROJECT_REVIEW_EX, Pattern.MULTILINE).matcher(firstPageContent);

      if (!internalProjectReviewMatcher.find()) {
        allSucceed = false;
      } else {
        String budgetPositionTextMatch = internalProjectReviewMatcher.group(1);
        String totalBudgetMatch = internalProjectReviewMatcher.group(2);
        String totalCommittedMatch = internalProjectReviewMatcher.group(3);
        String budgetPositionValueMatch = internalProjectReviewMatcher.group(4);

        extractionMap.put(BUDGET_POSITION_TEXT_LABEL, budgetPositionTextMatch);
        extractionMap.put(TOTAL_BUDGET_LABEL, totalBudgetMatch);
        extractionMap.put(TOTAL_COMMITTED_LABEL, totalCommittedMatch);
        extractionMap.put(BUDGET_POSITION_VALUE_LABEL, budgetPositionValueMatch);
      }

    }

    // Extract information from second page
    String secondPageContent = textStripper.getText(secondPage);

    // Project Title
    Matcher projectTitleMatcher = Pattern.compile(PROJECT_TITLE_EX).matcher(secondPageContent);
    if (!projectTitleMatcher.find()) {
      allSucceed = false;
    } else {
      String projectTitleMatch = projectTitleMatcher.group(1);
      extractionMap.put(PROJECT_TITLE_LABEL, projectTitleMatch);
    }

    // Project Director
    Matcher projectDirectorMatcher = Pattern.compile(PROJECT_DIRECTOR_EX).matcher(secondPageContent);
    if (!projectDirectorMatcher.find()) {
      allSucceed = false;
    } else {
      String projectDirectorMatch = projectDirectorMatcher.group(1);
      extractionMap.put(PROJECT_DIRECTOR_LABEL, projectDirectorMatch);
    }

    // Project Manager
    Matcher projectManagerMatcher = Pattern.compile(PROJECT_MANAGER_EX, Pattern.MULTILINE).matcher(secondPageContent);
    if (!projectManagerMatcher.find()) {
      allSucceed = false;
    } else {
      String projectManagerMatch = projectManagerMatcher.group(1);
      extractionMap.put(PROJECT_MANAGER_LABEL, projectManagerMatch);
    }

    return allSucceed;
  }

  private static PDDocument fillProjectReviewPage(PDDocument projectReviewPage, Map<String, String> formFields) {
    // TODO
    PDDocument filledProjectReviewPage = new PDDocument();
    filledProjectReviewPage.addPage(projectReviewPage.getPage(0));
    return filledProjectReviewPage;
  }


}
