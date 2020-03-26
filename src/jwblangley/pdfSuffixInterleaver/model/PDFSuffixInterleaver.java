package jwblangley.pdfSuffixInterleaver.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import jwblangley.pdfSuffixInterleaver.filenamer.NoOverwritePDFNamer;
import jwblangley.pdfSuffixInterleaver.filenamer.PDFNamer;
import jwblangley.pdfSuffixInterleaver.filenamer.PaprikaProjectCodePDFNamer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFSuffixInterleaver {

  public static void interleaveSuffixSeparateAndSave(File suffix, File document) throws IOException {
    PDDocument mainDoc = PDDocument.load(document);
    PDDocument suffixPage = PDDocument.load(suffix);

    Splitter splitter = new Splitter();
    PDFNamer pdfNamer = new NoOverwritePDFNamer(new PaprikaProjectCodePDFNamer());

    List<PDDocument> pages = splitter.split(mainDoc);

    for (PDDocument page : pages) {
      PDFMergerUtility merger = new PDFMergerUtility();

      PDDocument resultDoc = new PDDocument();

      merger.appendDocument(resultDoc, page);
      merger.appendDocument(resultDoc, suffixPage);

      resultDoc.save(pdfNamer.namePDF(resultDoc));
    }

    // Close documents
    mainDoc.close();
    suffixPage.close();
  }

}
