package jwblangley.pdfSuffixInterleaver.filenamer;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PaprikaProjectCodePDFNamer implements PDFNamer {

  private static final String FALLBACK_NAME = "paprika-file.pdf";
  private static final String PROJECT_CODE_PATTERN = "Project Director\\s*(\\S*)/";

  @Override
  public String namePDF(PDDocument document) {
    try {
      PDFTextStripper textStripper = new PDFTextStripper();
      String pdfContent = textStripper.getText(document);

      Pattern pattern = Pattern.compile(PROJECT_CODE_PATTERN);

      Matcher matcher = pattern.matcher(pdfContent);
      if (matcher.find()) {
        String match = matcher.group(1);
        return match + ".pdf";
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
