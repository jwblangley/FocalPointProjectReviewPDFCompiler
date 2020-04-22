package jwblangley.focalPointProjectReviewPdfCompiler.filenamer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class FocalPointProjectReviewPDFNamer implements PDFNamer {

  private static final String FALLBACK_NAME = "paprika-file.pdf";

  private static final String PROJECT_CODE_EX = "Project\\s*?Manager\\s*?(\\S*?)/";
  private static final String DATE_EX = "Page \\d+? of \\d+?\\s+?(\\d+?\\s+?\\S+?\\s+?\\d+?)\\s";

  // N.B: %s will be replaced by project code
  // N.B: greedy '.*' is intended
  private static final String PROJECT_MANAGER_EX = "%s.*\\s([A-Z]+?)\\s";

  @Override
  public String namePDF(PDDocument document) {
    // N.B: does not close document
    try {

      // TODO:

      PDFTextStripper textStripper = new PDFTextStripper();
      String pdfContent = textStripper.getText(document);

      Matcher projectCodeMatcher = Pattern.compile(PROJECT_CODE_EX).matcher(pdfContent);
      Matcher dateMatcher = Pattern.compile(DATE_EX).matcher(pdfContent);

      if (projectCodeMatcher.find() && dateMatcher.find()) {
        // Collect results so far
        String projectCodeMatch = projectCodeMatcher.group(1);
        String dateMatch = dateMatcher.group(1);
        dateMatch = dateMatch.replaceAll(" ", "_");

        // Find all listed project managers and choose most frequent
        String codeInjectedProjectManagerEx = String.format(PROJECT_MANAGER_EX, projectCodeMatch);
        Matcher projectManagerMatcher
            = Pattern.compile(codeInjectedProjectManagerEx).matcher(pdfContent);

        // If all succeeds - return result
        return String.format("%s-%s.pdf", projectCodeMatch, dateMatch);

      } else {
        // No match for project code or date
        return FALLBACK_NAME;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return FALLBACK_NAME;
    }
  }
}
