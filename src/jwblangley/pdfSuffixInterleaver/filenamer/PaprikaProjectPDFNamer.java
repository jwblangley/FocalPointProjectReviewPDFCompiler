package jwblangley.pdfSuffixInterleaver.filenamer;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PaprikaProjectPDFNamer implements PDFNamer {

  private static final String FALLBACK_NAME = "paprika-file.pdf";

  private static final String PROJECT_CODE_EX = "Project Director\\s*(\\S*)/";
  private static final String DATE_EX = "Page \\d of \\d\\s+(\\d+\\s+\\S+\\s+\\d+)";

  @Override
  public String namePDF(PDDocument document) {
    // N.B: does not close document
    try {
      PDFTextStripper textStripper = new PDFTextStripper();
      String pdfContent = textStripper.getText(document);

      Pattern projectCodePattern = Pattern.compile(PROJECT_CODE_EX);
      Matcher projectCodeMatcher = projectCodePattern.matcher(pdfContent);

      Pattern datePattern = Pattern.compile(DATE_EX);
      Matcher dateMatcher = datePattern.matcher(pdfContent);

      if (projectCodeMatcher.find() && dateMatcher.find()) {
        String projectCodeMatch = projectCodeMatcher.group(1);
        String dateMatch = dateMatcher.group(1);
        dateMatch = dateMatch.replaceAll(" ", "_");

        return dateMatch + "-" + projectCodeMatch + ".pdf";
      }
      else {
        return FALLBACK_NAME;
      }
    } catch(IOException e) {
      e.printStackTrace();
      return FALLBACK_NAME;
    }
  }
}
