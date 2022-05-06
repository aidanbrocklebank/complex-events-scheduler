package uk.ac.cam.agb67.dissertation.ui;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Read XML file into Java classes.
 * Adapter from: https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
 * @author jahic
 *
 */

public class ReadXmlDomParser
{
	// Path to the XML file.
	private String pathToTheFile;
	private Document xmlDocument;
	
	public ReadXmlDomParser(String filePath)
	{
		pathToTheFile = filePath;
		
		// Instantiate the Factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try 
		{
			// optional, but recommended
			// process XML securely, avoid attacks like XML External Entities (XXE)
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			
			// parse XML file
			DocumentBuilder db = dbf.newDocumentBuilder();
				
			xmlDocument = db.parse(new File(pathToTheFile));
				
			// optional, but recommended
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			xmlDocument.getDocumentElement().normalize();
		}
		 
		catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
	    }
	}
	
	public final NodeList getElementsByTagName(String tagName)
	{
        // get <staff>
        return xmlDocument.getElementsByTagName(tagName);
	}
}