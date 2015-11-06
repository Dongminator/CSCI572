import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class JsonSolrIndex {

	
	Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

    private static String url = "http://localhost:9999/solr/";
    private static HttpSolrServer solrCore;
    
	public void creatDoc () {
		
		String inputFilePath = "/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/out1";
		
		File folder = new File(inputFilePath);
		
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        } else {
	            parseFile(inputFilePath+"/"+fileEntry.getName());
	        }
	    }
		solrCore = new HttpSolrServer(url);
        try {
			solrCore.add(docs);
			solrCore.commit();
		} catch (SolrServerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void parseFile (String fileName) {
		System.out.println(fileName);
		JSONParser parser = new JSONParser();
		JSONObject a;
		try {
			a = (JSONObject) parser.parse(new FileReader(fileName));
			
			JSONArray geonames_addresses = (JSONArray) a.get("geonames_address");
			
			String docID = fileName;
			
			if (geonames_addresses != null) {
				JSONObject geonames_addresses_geo0 = (JSONObject) geonames_addresses.get(0);
				JSONObject geonames_addresses_geo0_item = (JSONObject) geonames_addresses_geo0.get("geo");
				
				double lat = (double) geonames_addresses_geo0_item.get("lat");
				double lon = (double) geonames_addresses_geo0_item.get("lon");
				
				SolrInputDocument doc = new SolrInputDocument();

				System.out.println(docID + " " + lat + " " + lon);
				doc.addField("Geographic_LATITUDE", lat);
				doc.addField("Geographic_LONGITUDE", lon);
				doc.addField("id", docID);
				
				docs.add(doc);
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}
}
