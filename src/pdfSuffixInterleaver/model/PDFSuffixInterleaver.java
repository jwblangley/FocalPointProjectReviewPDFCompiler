package pdfSuffixInterleaver.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFSuffixInterleaver {

  public static void interleaveSuffixSeparateAndSave(File suffix, File document) throws IOException {
    PDDocument mainDoc = PDDocument.load(document);
    PDDocument suffixPage = PDDocument.load(suffix);

    Splitter splitter = new Splitter();

    List<PDDocument> pages = splitter.split(mainDoc);

    for (int i = 0; i < pages.size(); i++) {
      PDDocument page = pages.get(i);
      PDFMergerUtility merger = new PDFMergerUtility();

      PDDocument resultDoc = new PDDocument();

      merger.appendDocument(resultDoc, page);
      merger.appendDocument(resultDoc, suffixPage);

      resultDoc.save((i + 1) + ".pdf");
      resultDoc.close();
    }

    // Close documents
    mainDoc.close();
    suffixPage.close();

  }

}
