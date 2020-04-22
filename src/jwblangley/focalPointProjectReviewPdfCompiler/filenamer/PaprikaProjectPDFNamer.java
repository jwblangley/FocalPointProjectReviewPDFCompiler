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

public class PaprikaProjectPDFNamer implements PDFNamer {

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

        List<String> listedManagers = new ArrayList<>();
        while (projectManagerMatcher.find()) {
          listedManagers.add(projectManagerMatcher.group(1));
        }

        if (listedManagers.isEmpty()) {
          // No match for project managers
          return FALLBACK_NAME;
        }

        String projectManager = mode(listedManagers);

        // If all succeeds - return result
        return String.format("%s-%s-%s.pdf", projectManager, projectCodeMatch, dateMatch);

      } else {
        // No match for project code or date
        return FALLBACK_NAME;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return FALLBACK_NAME;
    }
  }

  private <T> T mode(List<T> items) {
    Map<T, Integer> countMap = new HashMap<>();

    int max = 0;
    T result = null;
    for (T item : items) {
      if (!countMap.containsKey(item)) {
        countMap.put(item, 1);
      } else {
        countMap.put(item, countMap.get(item) + 1);
      }

      if (countMap.get(item) > max) {
        max = countMap.get(item);
        result = item;
      }
    }
    return result;
  }
}
