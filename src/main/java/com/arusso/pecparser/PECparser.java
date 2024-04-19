package com.arusso.pecparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import app.tozzi.mail.pec.exception.PECParserException;
import app.tozzi.mail.pec.model.Allegato;
import app.tozzi.mail.pec.model.Messaggio;
import app.tozzi.mail.pec.parser.PECMessageParser;

import javax.activation.DataSource;

public class PECparser {
    public static void main(String[] args) {
        if (args.length < 3) {
         System.out.println("Uso: PECparser <inputFilePath> <outputFilePath> <attachDirPath>");
         return;
         }

        String emlInputPath = args[0]; //"C:\\dev\\pectest\\postacertdoubleatt.eml";  //"C:\\dev\\pectest\\0104prova.txt"
        String txtOutputPath = args[1];//"C:\\dev\\pectest\\1904243prova.txt";
        boolean hasAttachments = false;
        String contenuto;

        //Parsing del file .eml
        try {

            File emlInputFile = new File(emlInputPath);
            Properties prop = System.getProperties();
            PECMessageParser parser = PECMessageParser.getInstance(prop);
            Messaggio message = parser.parse(emlInputFile);

            String corpo = message.getBusta().getCorpoTesto();
            String oggetto = message.getBusta().getOggetto();
            List<String> mittenti = message.getBusta().getMittenti();
            String mittente = mittenti.get(0);
            List<Allegato> allegati = message.getBusta().getAllegati();

            if (!allegati.isEmpty()) {
                hasAttachments = true;
                for (Allegato allegato : allegati) {
                    String fileName = allegato.getNome();
                    DataSource dataSource = allegato.getDataSource();

                    // Crezione file in cui salvare l'allegato
                    String outputDirectoryPath = args[2]; //"C:\\dev\\pectest";
                    File outputFile = new File(outputDirectoryPath, fileName);
                    try (InputStream is = dataSource.getInputStream();
                         FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        // Lettura dati da InputStream e scrittura nel FileOutputStream
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    System.out.println("Allegato salvato: " + outputFile.getAbsolutePath());
                }
            }
            //Creazione file con info su sender, subject, attachments e body
            contenuto = "Sender: " + mittente + "\n" + "Subject: " + oggetto + "\n" + "Attachments: " + hasAttachments + "\n" + "Body: " + corpo.strip();
            Path outputPath = Files.write(Paths.get(txtOutputPath), contenuto.getBytes());

            String outputPathToString = outputPath.toString();

            if (!outputPathToString.isEmpty()) {

                System.out.println("Dettagli email estratti con successo! Il tuo nuovo file è disponibile in: " + outputPathToString);
            }

        } catch (IOException | PECParserException e) {
            System.err.println("Si è verificato un errore: " + e.getMessage());
        }

    }

}