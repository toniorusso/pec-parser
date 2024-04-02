package com.arusso.pecparser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import app.tozzi.mail.pec.exception.PECParserException;
import app.tozzi.mail.pec.model.Messaggio;
import app.tozzi.mail.pec.parser.PECMessageParser;

public class PECparser 
{
    public static void main( String[] args )
    {
    	if (args.length < 2) {
            System.out.println("Uso: PECparser <inputPath> <outputPath>");
            return;
        }
    	
    	String emlInputPath = args[0];      //"C:\\dev\\pectest\\mail_FAHGVXL_attachments\\postacert.eml"  //"C:\\dev\\pectest\\0104prova.txt"
    	String txtOutputPath = args[1];     //"C:\\dev\\pectest\\0104prova.txt"
        
    	try {
    		
    	File emlInputFile = new File(emlInputPath);
        Properties prop = System.getProperties();
        PECMessageParser parser = PECMessageParser.getInstance(prop);
        Messaggio message = parser.parse(emlInputFile);
        
        String corpo = message.getBusta().getCorpoTesto();
        String oggetto = message.getBusta().getOggetto();
        List<String> mittenti = message.getBusta().getMittenti();
        String mittente = mittenti.get(0);
        String contenuto = "Sender: " + mittente + "\n" + "Subject: " + oggetto + "\n" + "Body: " + corpo.strip();
        
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
