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
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;

public class FocalPointProjectReviewPDFCompiler {

  private static final String END_OF_SECTION_TOKEN = "Sub Total Project";
  private static final String INDICATES_PROBLEM = "___CHECK___-";
  private static final String GAP = "___";

  // Define labels for identification for use in file name
  private static final String PROJECT_CODE_LABEL = "Project code";
  private static final String DATE_LABEL = "Date";
  private static final String PROJECT_MANAGER_LABEL = "Project Manager";

  // Value matcher
  private static final String LABEL_VALUE_MATCHER = "^(.+?)\\s=\\s(.*?)$";

  public static boolean compilePDF(
      File focalPointDocumentFile,
      File projectReviewPageFile,
      File outputDirectory) throws IOException {

    boolean allSucceed = true;

    PDDocument focalPointDocument = PDDocument.load(focalPointDocumentFile);
    PDDocument projectReviewPage = PDDocument.load(projectReviewPageFile);

    Splitter splitter = new Splitter();
    List<PDDocument> pages = splitter.split(focalPointDocument);

    Queue<PDDocument> pageQueue = new LinkedList<>();

    for (PDDocument page : pages) {
      pageQueue.add(page);

      if (endOfSectionAt(page)) {
        // N.B: Order of the operands is important as we always want to compileSection
        allSucceed = compileSection(pageQueue, projectReviewPage, outputDirectory) && allSucceed;
        assert pageQueue.isEmpty() : "compileSection must consume all pages on the page queue";
      }
    }

    assert pageQueue.isEmpty() : "Not all pages were consumed from the queue";

    // Close documents
    focalPointDocument.close();
    projectReviewPage.close();

    return allSucceed;
  }

  private static boolean endOfSectionAt(PDDocument page) throws IOException {
    PDFTextStripper textStripper = new PDFTextStripper();
    String pageContent = textStripper.getText(page);

    Matcher endTokenMatcher = Pattern.compile(END_OF_SECTION_TOKEN).matcher(pageContent);
    return endTokenMatcher.find();
  }

  private static boolean compileSection(
      Queue<PDDocument> pageQueue,
      PDDocument projectReviewPage,
      File outputDirectory) throws IOException {

    assert pageQueue.size() >= 1;

    PDFMergerUtility merger = new PDFMergerUtility();
    PDDocument resultDoc = new PDDocument();

    // Extract information from first page
    PDDocument firstPage = pageQueue.poll();
    Map<String, String> extractionMap = new HashMap<>();
    extractInformationIntoMap(firstPage, extractionMap);

    // Replace first page with project review page
    PDDocument filledPVPage = fillProjectReviewPage(projectReviewPage, extractionMap);
    merger.appendDocument(resultDoc, filledPVPage);
    filledPVPage.close();

    // Close first page
    firstPage.close();

    // Read any remaining pages from the page queue
    while (!pageQueue.isEmpty()) {
      PDDocument page = pageQueue.poll();
      merger.appendDocument(resultDoc, page);
      page.close();
    }

    // Name the document
    // Use extracted information needed for filename with safe fallover
    String projectManager = extractionMap.get(PROJECT_MANAGER_LABEL);
    String projectCode = extractionMap.get(PROJECT_CODE_LABEL);
    String date = extractionMap.get(DATE_LABEL);

    boolean fileNameExtractedInfoSuccess = true;

    if (projectManager == null) {
      projectManager = GAP;
      fileNameExtractedInfoSuccess = false;
    } else {
      // Format projectManager to remove forward spaces ' '
      date = date.replaceAll(" ", "-");
    }

    if (projectCode == null) {
      projectCode = GAP;
      fileNameExtractedInfoSuccess = false;
    }

    if (date == null) {
      date = GAP;
      fileNameExtractedInfoSuccess = false;
    } else {
      // Format date to remove forward slashes '/'
      date = date.replaceAll("/", "-");
    }

    String showProblem = fileNameExtractedInfoSuccess ? "" : INDICATES_PROBLEM;

    String resultName = String.format("%s%s_%s_%s.pdf",
        showProblem, projectManager, projectCode, date);

    PDFNamer pdfNamer = new NoOverwritePDFNamer(new StringPDFNamer(resultName), outputDirectory);

    File resultFile = new File(outputDirectory, pdfNamer.namePDF(resultDoc));

    resultDoc.save(resultFile.getPath());
    resultDoc.close();

    return fileNameExtractedInfoSuccess;
  }

  private static void extractInformationIntoMap(
      PDDocument infoPage, Map<String, String> extractionMap)
      throws IOException {

    PDFTextStripper textStripper = new PDFTextStripper();

    // Extract information from first page
    String infoPageContent = textStripper.getText(infoPage);

    Matcher labelValueMatcher = Pattern
        .compile(LABEL_VALUE_MATCHER, Pattern.MULTILINE)
        .matcher(infoPageContent);

    while(labelValueMatcher.find()) {
      String label = labelValueMatcher.group(1);
      String value = labelValueMatcher.group(2);
      System.out.println(value);
      extractionMap.put(label, value);
    }
  }

  private static PDDocument fillProjectReviewPage(
      PDDocument projectReviewPage, Map<String, String> formFields) throws IOException {

    // Clone projectReviewPage into filledProjectReviewPage
    PDDocument filledProjectReviewPage = new PDDocument();
    PDFMergerUtility merger = new PDFMergerUtility();
    merger.appendDocument(filledProjectReviewPage, projectReviewPage);

    // Extract form
    PDDocumentCatalog docCatalog = filledProjectReviewPage.getDocumentCatalog();
    PDAcroForm acroForm = docCatalog.getAcroForm();

    // Populate form fields
    for (PDField field : acroForm.getFields()) {
      String fieldName = field.getFullyQualifiedName();
      String fieldValue = formFields.get(fieldName);

      if (fieldValue != null) {
        field.setValue(fieldValue);
      }
    }

    return filledProjectReviewPage;
  }


}
