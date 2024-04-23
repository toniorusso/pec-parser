package com.arusso.pecparser;

import app.tozzi.mail.pec.exception.PECParserException;
import app.tozzi.mail.pec.model.Allegato;
import app.tozzi.mail.pec.model.Busta;
import app.tozzi.mail.pec.model.Messaggio;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Unit test for simple App.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class PECparserTest {

    @Test
    public void testMainWithFewArguments() {
        // Setup to capture System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        // Execute with fewer than needed arguments
        String[] args = {}; // empty argument list to simulate the error
        PECparser.main(args);

        // Assert the expected output
        String output = baos.toString();
        assertTrue(output.contains("Usage: PECparser <inputFilePath> <outputFilePath> <attachDirPath>"));

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testParseEML() throws IOException, URISyntaxException, PECParserException {
        String inputPath = Paths.get("src/test/resources/postacert.eml").toString();

        Messaggio messaggio = PECparser.parseEML(inputPath);

        assertEquals("antoniorusso95@pec.it", messaggio.getBusta().getMittenti().get(0));
        assertEquals("Estrazione per test junit", messaggio.getBusta().getOggetto());
        assertEquals("Body da estrarre per validare test conjunit", messaggio.getBusta().getCorpoTesto().strip());
    }

    @Test
    public void testSaveAttachmentsTrueRoot() throws IOException, PECParserException {
        String inputPath = Paths.get("src/test/resources/postacert.eml").toString();

        Messaggio messaggio = PECparser.parseEML(inputPath);

        assertTrue(PECparser.saveAttachments(messaggio, "src/test/resources"));
    }
    @Test
    public void testSaveAttachmentsFalseRoot() throws IOException, PECParserException {
        String inputPath = Paths.get("src/test/resources/postacertnoattach.eml").toString();

        Messaggio messaggio = PECparser.parseEML(inputPath);

        assertFalse(PECparser.saveAttachments(messaggio, "src/test/resources"));
    }

    @Test
    public void testSaveEmailDetails() throws Exception {
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // Prepara il messaggio e la busta mock
            Messaggio message = mock(Messaggio.class);
            Busta busta = mock(Busta.class);
            when(message.getBusta()).thenReturn(busta);
            when(busta.getMittenti()).thenReturn(List.of("mittente@example.com"));
            when(busta.getOggetto()).thenReturn("Oggetto Test");
            when(busta.getCorpoTesto()).thenReturn("Corpo del messaggio");

            // Path da usare
            Path outputPath = Paths.get("fakepath/output.txt");

            // Esegui il metodo da testare
            PECparser.saveEmailDetails(message, true, outputPath.toString());

            // Verifica che Files.write sia stato chiamato con i parametri corretti
            String expectedContent = "Sender: mittente@example.com\nSubject: Oggetto Test\nAttachments: true\nBody: Corpo del messaggio";
            mockedFiles.verify(() -> Files.write(eq(outputPath), eq(expectedContent.getBytes())), times(1));
        }
    }
}

