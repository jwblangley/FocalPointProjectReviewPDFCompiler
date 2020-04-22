package jwblangley.focalPointProjectReviewPdfCompiler.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.NoOverwritePDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.PDFNamer;
import jwblangley.focalPointProjectReviewPdfCompiler.filenamer.PaprikaProjectPDFNamer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFSuffixInterleaver {

  public static void interleaveSuffixSeparateAndSave(File suffix, File document,
      File outputDirectory) throws IOException {
    PDDocument suffixPage = PDDocument.load(suffix);
    PDDocument mainDoc = PDDocument.load(document);

    Splitter splitter = new Splitter();
    PDFNamer pdfNamer = new NoOverwritePDFNamer(new PaprikaProjectPDFNamer(), outputDirectory);

    List<PDDocument> pages = splitter.split(mainDoc);

    for (PDDocument page : pages) {
      PDFMergerUtility merger = new PDFMergerUtility();

      PDDocument resultDoc = new PDDocument();

      merger.appendDocument(resultDoc, page);
      merger.appendDocument(resultDoc, suffixPage);

      File resultFile = new File(outputDirectory, pdfNamer.namePDF(resultDoc));

      resultDoc.save(resultFile.getPath());
      resultDoc.close();
      page.close();
    }

    // Close documents
    suffixPage.close();
    mainDoc.close();
  }

  public static void interleaveSuffixSeparateAndSave(File suffix, File document)
      throws IOException {
    interleaveSuffixSeparateAndSave(suffix, document, new File("").getAbsoluteFile());
  }

}
