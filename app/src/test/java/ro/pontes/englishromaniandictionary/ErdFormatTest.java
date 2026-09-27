package ro.pontes.englishromaniandictionary;

import org.junit.Test;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Arrays;
import static org.junit.Assert.*;

public class ErdFormatTest {
    @Test public void legacyBomAndLineEndings() throws Exception {
        List<ErdFormat.Entry> rows = ErdFormat.read(new StringReader("\uFEFFcat-=-pisică\r\ndog-=-câine\n\n"));
        assertEquals(2, rows.size());
        assertEquals("pisică", rows.get(0).explanation);
    }
    @Test public void roundTripPreservesDiacriticsAndQuotes() throws Exception {
        String text = "it's-=-este\nșarpe-=-snake\n";
        assertEquals(text, ErdFormat.write(ErdFormat.read(new StringReader(text))));
    }
    @Test(expected=IOException.class) public void malformedLineRejectsWholeFile() throws Exception {
        ErdFormat.read(new StringReader("cat-=-pisică\nbroken"));
    }
    @Test(expected=IOException.class) public void emptyFileRejected() throws Exception {
        ErdFormat.read(new StringReader(" \n"));
    }
    @Test(expected=IOException.class) public void emptyFieldRejected() throws Exception {
        ErdFormat.read(new StringReader("cat-=-"));
    }
    @Test(expected=IOException.class) public void ambiguousDelimiterRejected() throws Exception {
        ErdFormat.read(new StringReader("cat-=-one-=-two"));
    }
    @Test(expected=IOException.class) public void lossyExportRejected() throws Exception {
        ErdFormat.write(Arrays.asList(new ErdFormat.Entry("cat", "one\ntwo")));
    }
    @Test(expected=IOException.class) public void oversizedInputRejected() throws Exception {
        char[] chars = new char[ErdFormat.MAX_CHARS + 1];
        Arrays.fill(chars, 'x');
        ErdFormat.read(new StringReader(new String(chars)));
    }
}
