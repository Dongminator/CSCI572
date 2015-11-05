package cs572_HW2;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XMLReader {
	private String indexPath = null;
	private String solrURL = null;
	private String outputPath = null;
	
	public void readXML(String xmlPath){
		Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try{
        	DocumentBuilder db = dbf.newDocumentBuilder();
        	dom = db.parse(xmlPath);
        	Element doc = dom.getDocumentElement();

            indexPath = doc.getElementsByTagName("indexPath").item(0).getFirstChild().getNodeValue();
            solrURL = doc.getElementsByTagName("solrURL").item(0).getFirstChild().getNodeValue();
            outputPath = doc.getElementsByTagName("outputPath").item(0).getFirstChild().getNodeValue();
           
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }    
	}
	
	public String getIndexPath(){
		return indexPath;
	}
	
	public String getSolrURL(){
		return solrURL;
	}
	
	public String getOutputPath(){
		return outputPath;
	}
	
}
