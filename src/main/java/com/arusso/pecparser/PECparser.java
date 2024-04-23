package com.arusso.pecparser;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;

import app.tozzi.mail.pec.exception.PECParserException;
import app.tozzi.mail.pec.model.Allegato;
import app.tozzi.mail.pec.model.Messaggio;
import app.tozzi.mail.pec.parser.PECMessageParser;

import javax.activation.DataSource;

public class PECparser {

    //private static final String OUTPUT_DIRECTORY_PATH = "C:\\dev\\pectest";

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: PECparser <inputFilePath> <outputFilePath> <attachDirPath>");
            return;
        }

        String emlInputPath = args[0];
        String txtOutputPath = args[1];
        String outputAttachDirPath = args[2];

        try {
            Messaggio message = parseEML(emlInputPath);
            boolean hasAttachments = saveAttachments(message, outputAttachDirPath);
            saveEmailDetails(message, hasAttachments, txtOutputPath);
            System.out.println("Email details successfully extracted! Your new file is available at: " + txtOutputPath);
        } catch (IOException | PECParserException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    public static Messaggio parseEML(String emlInputPath) throws IOException, PECParserException {
        File emlInputFile = new File(emlInputPath);
        Properties prop = System.getProperties();
        PECMessageParser parser = PECMessageParser.getInstance(prop);
        return parser.parse(emlInputFile);
    }

    public static boolean saveAttachments(Messaggio message, String outputDirPath) throws IOException {
        List<Allegato> allegati = message.getBusta().getAllegati();
        if (allegati.isEmpty()) {
            return false;
        }

        for (Allegato allegato : allegati) {
            DataSource dataSource = allegato.getDataSource();
            File outputFile = new File(outputDirPath, allegato.getNome());

            try (InputStream is = dataSource.getInputStream(); FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("Attachment saved: " + outputFile.getAbsolutePath());
        }
        return true;
    }

    public static void saveEmailDetails(Messaggio message, boolean hasAttachments, String txtOutputPath) throws IOException {
        String mittente = message.getBusta().getMittenti().get(0);
        String oggetto = message.getBusta().getOggetto();
        String corpo = message.getBusta().getCorpoTesto().strip();

        String contenuto = String.format("Sender: %s\nSubject: %s\nAttachments: %s\nBody: %s",
                mittente, oggetto, hasAttachments, corpo);

        Files.write(Paths.get(txtOutputPath), contenuto.getBytes());
    }

}


//"C:\\dev\\pectest\\postacertdoubleatt.eml";  //"C:\\dev\\pectest\\0104prova.txt"