import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;


/**
 * 
 * @author Donglin Pu
 *
 */
public class LinkBased {

	public static void main (String[] args) {
		
//		JsonSolrIndex jsonSolrIndex = new JsonSolrIndex();
//		jsonSolrIndex.creatDoc();
		
		
		UpdateSolrWithFile updateSolr = new UpdateSolrWithFile();
		updateSolr.updateTime();
		updateSolr.updateGeo();
//		
		LinkBased linkbase = new LinkBased(host_port+"collection1");
		
		Hashtable<String, Float> geoScore = linkbase.getGeoScore();
		System.out.println("Total documents that have geo score: " + geoScore.size());
//		linkbase.writeRelevancyToFile(linkbase.geoBasedGraph, GRAPH_NAME_GEO);
		linkbase.updateScore(geoScore, "linkbased_geo");
		
//		
		Hashtable<String, Float> timeScore = linkbase.getTimeScore();
		System.out.println("Total documents that have TIME score: " + timeScore.size());
//		linkbase.writeRelevancyToFile(linkbase.timeBasedGraph, GRAPH_NAME_TIME);
		linkbase.updateScore(timeScore, "linkbased_time");
		
//		linkbase.helperGetCores();
	}
	
	private static final int CONFIG_MAX_GEOPOINT_PER_DOC_TO_COMPARE = 6;
	
	private static final String FIELD_GUN_RELATED_DATE = "Gun_date"; // TODO need to insert this date first.
	
	private static final int CONFIG_LINK_BASE_DAYS_DIFF = 1; // If two documents are different by 1 day, add an edge between the two documents in the time based graph.
	private static final float CONFIG_LINK_BASE_GEO_DIFF = 0.5f; // If two documents are different by 1 day, add an edge between the two documents in the time based graph.
	
	
	private static final String GRAPH_NAME_TIME = "LinkBased_time";
	private static final String GRAPH_NAME_GEO = "LinkBased_geo";
	
	private SolrServer server;
	private long totalDocs;
	
	private static final String host_port = "http://localhost:8983/solr/";
	private String url;
	
	/**
	 * @param index_path: solr index url
	 */
	public LinkBased (String index_path) {
		url = host_port + "collection1";
		if (index_path.length() > 10) {
			url = index_path;
		}
		server = new HttpSolrServer( url );
	}
	
	
	/**
	 * Query Solr to get some documents.
	 * @param queryStartPos
	 * @return SolrDocumentList
	 */
	private SolrDocumentList querySolrIndex (int queryStartPos) {
		// http://lucene.apache.org/solr/5_3_1/solr-solrj/org/apache/solr/client/solrj/SolrQuery.html
		SolrQuery query = new SolrQuery();
	    query.setQuery( "*" );
		
	    query.setStart(queryStartPos);
	    QueryResponse rsp;
	    
	    try {
			rsp = server.query( query );
			return rsp.getResults();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Calculate the GEO graph. 
	 * @return
	 */
	public Hashtable<String, Float> getGeoScore () {
		int queryStartPos = 0;
		SolrDocumentList docs = querySolrIndex(queryStartPos);
		totalDocs = docs.getNumFound();
		System.out.println("GetGeoScore: Doc Size: " + docs.size() + " || Num Found: " + totalDocs);

		while (queryStartPos < totalDocs) {
			// http://lucene.apache.org/solr/4_3_1/solr-solrj/org/apache/solr/common/SolrDocument.html
			ListIterator<SolrDocument> docListIterator = docs.listIterator();

			while (docListIterator.hasNext()) {
				queryStartPos++;
				SolrDocument sd = docListIterator.next();
				String docId = (String)sd.getFieldValue("id");
				
				// Build geo graph - add all documents to graph first.
				Object geoLat = sd.getFieldValue("Geographic_LATITUDE");
				Object geoLon = sd.getFieldValue("Geographic_LONGITUDE");
				Object geoName = sd.getFieldValue("Geographic_NAME");
				if (geoName == null) {
					geoName = "";
				} 
				String name = (String)geoName;
				List<LatLon> locations = new ArrayList<>();
				if ( !name.equals("North America") && geoLat != null && geoLon != null) {
					locations.add(new LatLon((float)geoLat, (float)geoLon));
				}
				for (int i = 0; i < CONFIG_MAX_GEOPOINT_PER_DOC_TO_COMPARE; i ++) {
					Object optLat = sd.getFieldValue("Optional_LATITUDE" + i);
					Object optLon = sd.getFieldValue("Optional_LONGITUDE" + i);
					Object optName = sd.getFieldValue("Optional_NAME" + i);
					if (optLat != null && optLon != null) {
						locations.add(new LatLon((float)geoLat, (float)geoLon));
					} else {
						break;
					}
				}
				
				GeoDoc geoDoc = new GeoDoc(locations, docId);
				addToGeoGraph(geoDoc, docId);
			}
			docs = querySolrIndex(queryStartPos);
		}
		System.out.println("Final position: " + queryStartPos);
		System.out.println("INFO: calculating geo score...");
		
		// After graph is done, compute the geo graph and return the Hashtable<String, Float>
		return calcScore(geoBasedGraph);
	}
	
	
	/**
	 * Calculate the TIME graph. 
	 * @return
	 */
	public Hashtable<String, Float> getTimeScore () {
		int queryStartPos = 0;
		SolrDocumentList docs = querySolrIndex(queryStartPos);
		totalDocs = docs.getNumFound();
		System.out.println("Doc Size: " + docs.size() + " || Num Found: " + totalDocs);

		while (queryStartPos < totalDocs) {
			// http://lucene.apache.org/solr/4_3_1/solr-solrj/org/apache/solr/common/SolrDocument.html
			ListIterator<SolrDocument> docListIterator = docs.listIterator();

			while (docListIterator.hasNext()) {
				queryStartPos++;
				SolrDocument sd = docListIterator.next();
				String docId = (String)sd.getFieldValue("id");
				
				// Get time related fields.
				Object docGunRelatedDate = sd.getFieldValue(FIELD_GUN_RELATED_DATE);
				if (docGunRelatedDate != null) {
					addToTimeGraph( (Date)docGunRelatedDate, docId);
				} else {
//					System.out.println("INFO: Time field does not exist for this document.");
				}
			}
			docs = querySolrIndex(queryStartPos);
		}
		System.out.println("Final position: " + queryStartPos);
		System.out.println("INFO: calculating TIME score...");
		
		// Compute the geo graph
		return calcScore(timeBasedGraph);
	}
	
	
	
	public void testGraph () {
		List<LatLon> loc1 = new ArrayList<>();
		loc1.add(new LatLon(0,0));
		loc1.add(new LatLon(0,1));
		loc1.add(new LatLon(1,0));
		loc1.add(new LatLon(1,1));
		loc1.add(new LatLon(2,0));
		loc1.add(new LatLon(3,0));
		GeoDoc doc1 = new GeoDoc(loc1, "doc1");
		addToGeoGraph(doc1, "doc1");
		
		List<LatLon> loc2 = new ArrayList<>();
		loc2.add(new LatLon(9,9));
		GeoDoc doc2 = new GeoDoc(loc2, "doc2");
		addToGeoGraph(doc2, "doc2");
		
		List<LatLon> loc3 = new ArrayList<>();
		loc3.add(new LatLon(2,8));
		loc3.add(new LatLon(2,7));
		GeoDoc doc3 = new GeoDoc(loc3, "doc3");
		addToGeoGraph(doc3, "doc3");
		
		List<LatLon> loc4 = new ArrayList<>();
		loc4.add(new LatLon(2,5));
		loc4.add(new LatLon(2,4));
		GeoDoc doc4 = new GeoDoc(loc4, "doc4");
		addToGeoGraph(doc4, "doc4");
		
		List<LatLon> loc5 = new ArrayList<>();
		loc5.add(new LatLon(3,2));
		loc5.add(new LatLon(3,3));
		GeoDoc doc5 = new GeoDoc(loc5, "doc5");
		addToGeoGraph(doc5, "doc5");
		
		List<LatLon> loc6 = new ArrayList<>();
		loc6.add(new LatLon(3,6));
		loc6.add(new LatLon(4,6));
		GeoDoc doc6 = new GeoDoc(loc6, "doc6");
		addToGeoGraph(doc6, "doc6");
		
		List<LatLon> loc7 = new ArrayList<>();
		loc7.add(new LatLon(9,8));
		GeoDoc doc7 = new GeoDoc(loc7, "doc7");
		addToGeoGraph(doc7, "doc7");
		
//		geoBasedGraph.printGraph();
		calcScore(geoBasedGraph);
	}
	
	/**
	 * Step 1: addToGeoGraph: add all documents to graph. 
	 * Step 2: calcScore (geoBasedGraph, geoDocScore);
	 */
	public Graph geoBasedGraph = new Graph(GRAPH_NAME_GEO); // undirected graph
	private Hashtable<String, GeoDoc> geoDocScore = new Hashtable<>();
	private Hashtable<String, String> geoDocSubId = new Hashtable<>();
	private int geoDocCount = 0;
	
	
	public Graph gunTypeBasedGraph = new Graph("LinkBased_gunType"); // undirected graph
	private Hashtable<String, GeoDoc> gunTypeDocScore = new Hashtable<>();
	
	/**
	 * This is the core link based algorithm. It calculates the score of each document in the graph.
	 */
	private Hashtable<String, Float> calcScore (Graph g) {
		int iterationNumber = 50;
		float dampingFactor = 0.85f;
		
		// first get all documents with link
		Iterator<Map.Entry<String, List<String>>> it = g.graph.entrySet().iterator();
		Hashtable<String, Integer> outlinks = new Hashtable<>();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();
			List<String> linkedDoc = entry.getValue();
			if (linkedDoc.size() > 0) {
				outlinks.put(entry.getKey(), linkedDoc.size());
			}
		}
		
		if (outlinks.size() > 0) {
			Hashtable<String, Float> score = new Hashtable<>();
			float initScore = (float)1/outlinks.size();
			for (int i = 0; i < iterationNumber; i++) {
				if (i == 0) {
					Iterator<Map.Entry<String, Integer>> outlinksIterator = outlinks.entrySet().iterator();
					while (outlinksIterator.hasNext()) {
						Map.Entry<String, Integer> outlinksEntry = outlinksIterator.next();
						score.put(outlinksEntry.getKey(), initScore);
					}
				}
				
				Iterator<Map.Entry<String, Float>> scoreIterator = score.entrySet().iterator();

				Hashtable<String, Float> tempScore = new Hashtable<>();
				while (scoreIterator.hasNext()) {
					Map.Entry<String, Float> currScoreEntry = scoreIterator.next();
					float newScore = 0;
					List<String> neighbors = g.graph.get(currScoreEntry.getKey());
					for (String nei : neighbors) {
						if ( score.get(nei) != null ) {
							newScore = newScore + score.get(nei)/outlinks.get(nei);
						}
					}
					// With damping factor
					tempScore.put(currScoreEntry.getKey(), (1-dampingFactor)/outlinks.size() + dampingFactor*newScore);
					// Without damping factor
//					tempScore.put(currScoreEntry.getKey(), newScore);
				}
				
				// copy all temp score to score
				Iterator<Map.Entry<String, Float>> tempIt = tempScore.entrySet().iterator();
				while (tempIt.hasNext()) {
					Map.Entry<String, Float> tempEntry = tempIt.next();
					score.put(tempEntry.getKey(), tempEntry.getValue());
				}
			}
			
			helperPrintDocScore (score); // score contains the final score for this field.
			System.out.println("INFO: a hashtable of Document IDs and Scores is returned.");
			return score;
		} else {
			System.out.println("INFO: no document in the graph or no document has any outlink. An empty hashtable is returned.");
			return new Hashtable<>();
		}
	}
	
	private static void helperPrintDocScore (Hashtable<String, Float> scores) {
		Iterator<Map.Entry<String, Float>> it = scores.entrySet().iterator();
		float sum = 0;
		while (it.hasNext()) {
			Map.Entry<String, Float> entry = it.next();
			System.out.println(entry.getKey() + " " + entry.getValue());
			sum += entry.getValue();
		}
		System.out.println("sum: " + sum);
	}
	
	
	
	/**
	 * @author Donglin Pu
	 * @param locations
	 * @param docId
	 * 
	 * This method will build the graph.
	 */
	public void addToGeoGraph (GeoDoc doc, String docId) {
		geoDocSubId.put(docId, "Doc" + geoDocCount);
		geoDocCount++;
		
		geoBasedGraph.addVertex(docId);
		geoDocScore.put(docId, doc);
		
		/*
		 * locations 0: 1, 1, Loc1 -- primary location always at location 0
		 * lcoations 1: 1, 2, loc2
		 */
		for (int i = 0; i < doc.locations.size() && i < CONFIG_MAX_GEOPOINT_PER_DOC_TO_COMPARE; i++) { // we only want the top 5 points. otherwise complexity too high.
			LatLon currLoc = doc.locations.get(i);
			float thisLat = currLoc.lat;
			float thisLon = currLoc.lon;
			
			// for each existing document
			Set<String> existingDocs = geoBasedGraph.graph.keySet();
			
//			outerDocsLoop:
			for (String s : existingDocs) {
				if (s != docId && !geoBasedGraph.adjacent(docId, s)) {
					GeoDoc docToCompare = geoDocScore.get(s);
					List<LatLon> docToCompareLatLon = docToCompare.locations;
					for (LatLon existing : docToCompareLatLon) {
						float existingLat = existing.lat;
						float existingLon = existing.lon;
						
						double pointsDis = Math.pow(existingLon - thisLon, 2) + Math.pow(existingLat - thisLat, 2) ;
						if ( pointsDis <= Math.pow(CONFIG_LINK_BASE_GEO_DIFF, 2)    ) {
							geoBasedGraph.addEdge(docId, s);
//							break outerDocsLoop;
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Build the time based graph.
	 */
	public Graph timeBasedGraph = new Graph(GRAPH_NAME_TIME); // undirected graph
	private Hashtable<String, Date> timeDocScore = new Hashtable<>();

	private Hashtable<String, String> timeDocSubId = new Hashtable<>();
	private int timeDocCount = 0;
	
	private void addToTimeGraph (Date date, String docId) {
		timeDocSubId.put(docId, "Doc" + timeDocCount);
		timeDocCount++;
		
		timeBasedGraph.addVertex(docId);
		timeDocScore.put(docId, date);

		// for each existing document
		Set<String> existingDocs = timeBasedGraph.graph.keySet();

		for (String s : existingDocs) {
			if (s != docId && !timeBasedGraph.adjacent(docId, s)) {
				Date docToCompare = timeDocScore.get(s);
				if ( compareDate(date, docToCompare, CONFIG_LINK_BASE_DAYS_DIFF) ) {
					timeBasedGraph.addEdge(docId, s);
				}
			}
		}
	}
	
	
	private boolean compareDate(Date d1, Date d2, int threshold){
		int daysDiff = (int)((d1.getTime() - d2.getTime()) / (1000*60*60*24l));
		return (Math.abs(daysDiff) <= threshold) ? true : false;
	}
		
	public static void addToGunTypeGraph () {
		
	}
	
	public void helperGetCores () {
		server = new HttpSolrServer( host_port );
		
		CoreAdminRequest request = new CoreAdminRequest();
		request.setAction(CoreAdminAction.STATUS);
		CoreAdminResponse cores;
		try {
			cores = request.process(server);
			// List of the cores
			for (int i = 0; i < cores.getCoreStatus().size(); i++) {
				System.out.println(cores.getCoreStatus().getName(i));
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateScore (Hashtable<String, Float> scores, String field) {
		Iterator<Map.Entry<String, Float>> scoreIt = scores.entrySet().iterator();
		
		int numDoc = 0;
		while (scoreIt.hasNext()) {
			numDoc++;
			Map.Entry<String, Float> score = scoreIt.next();
			
			String docID = score.getKey();
			Float docScore = score.getValue();
//			System.out.println(docID + " " + docScore);
			
			sendUpdateRequest (docID, docScore, field);
		}
		
		System.out.println("Toal: " + numDoc);
	}
	
	private void sendUpdateRequest (String docID, float score, String field) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		
		HttpPost request = new HttpPost(host_port + "update?commit=true");
		StringEntity params;
		
		try {
			String finalParams = "[{\"id\":\"" + docID +  " \",\"" + field + "\":{\"set\":\"" + score + "\"}}] ";
			params = new StringEntity(finalParams);
			request.addHeader("content-type", "application/json");
			
			request.setEntity(params);
	        HttpResponse response = httpClient.execute(request);
//	        System.out.println(response.getStatusLine().getStatusCode());
//	        System.out.println(response.getStatusLine().getReasonPhrase());
	        
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	private void updateTest (Hashtable<String, Float> score) {
//		SolrDocumentList docs = querySolrIndex(0);
//		totalDocs = docs.getNumFound();
//		System.out.println("Doc Size: " + docs.size() + " || Num Found: " + totalDocs);
//
//		// http://lucene.apache.org/solr/4_3_1/solr-solrj/org/apache/solr/common/SolrDocument.html
//		ListIterator<SolrDocument> docListIterator = docs.listIterator();
//
//		Collection<SolrInputDocument> updateDocs = new ArrayList<SolrInputDocument>();
//
//	    
//		while (docListIterator.hasNext()) {
//			SolrDocument sd = docListIterator.next();
//			String docId = (String)sd.getFieldValue("id");
//			sd.addField("", "");
//
//			SolrInputDocument inputDoc = ClientUtils.toSolrInputDocument(sd);
//			
//			
//			inputDoc.setField("linkbased_geo", 4.4);
//			inputDoc.addField("linkbased_time", 5.6);
//			
//			System.out.println(docId);
//			
//			// curl 'localhost:8983/solr/update?commit=true' 
//			// -H 'Content-type:application/json' 
//			// -d 
//			// '[{"id":"http://aberdeen-ca.americanlisted.com/phones/samsung-galaxy-note-edge-smn915-latest-model-32gb-charcoal-black_32263319.html","segment":{"set":"123"}}]'
//		}
//	}
	
	/*
	 * Write to input.txt for D3 visualization.
	 * Option 0: geo graph
	 * Option 1: time graph
	 */
	public void writeRelevancyToFile (Graph g, String option) {
		Charset charset = Charset.forName("US-ASCII");
		Path path = FileSystems.getDefault().getPath("", option + ".txt");
		
		Iterator<Map.Entry<String, List<String>>> graphIterator = g.graph.entrySet().iterator();
		
		BufferedWriter writer;
		try {
			writer = Files.newBufferedWriter(path, charset);
			
			while (graphIterator.hasNext()) {
				Map.Entry<String, List<String>> graphEntry = graphIterator.next();
				String docId = graphEntry.getKey();
				
				
				if (option.equals(GRAPH_NAME_GEO)) {
					String geoSubString = geoDocSubId.get(docId);
					
					List<String> linkedDocs = graphEntry.getValue();
					
					if (linkedDocs.size() > 0) {
						writer.write(geoSubString, 0, geoSubString.length());
						for (String s : linkedDocs) {
							geoSubString = geoDocSubId.get(s);
							writer.write(" " + geoSubString, 0, geoSubString.length() + 1);
						}
						writer.newLine();
					}
					

				} else if (option.equals(GRAPH_NAME_TIME)) {
					String timeSubString = timeDocSubId.get(docId);
					writer.write(timeSubString, 0, timeSubString.length());
					
					List<String> linkedDocs = graphEntry.getValue();
					
					for (String s : linkedDocs) {
						timeSubString = timeDocSubId.get(s);
						writer.write(" " + timeSubString, 0, timeSubString.length() + 1);
					}
					writer.newLine();
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

/**
 * 
 * @author Donglin
 * This object stores Lat, Lon, Score of a document. 
 *
 */
class GeoDoc {
	List<LatLon> locations;
	String docID;
	float score;
	public GeoDoc (List<LatLon> locations, String docID) {
		this.locations = locations;
		this.docID = docID;
		score = 0;
	}
}


class LatLon {
	float lat, lon;
	public LatLon (float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
	}
}


/**
 * 
 * @author Donglin Pu
 * 
 * The graph uses adjacency list. 
 * Vertices are Document IDs. 
 * Each vertex has a score/value/relevancy. 
 *
 */
class Graph {
	
	Hashtable<String, List<String>> graph;
	String name; // E.g. GeoBasedGraph
	
	public Graph(String name) {
		this.name = name;
		graph = new Hashtable<>();
	}
	
	public boolean adjacent (String docID1, String docID2) {
		if (graph.containsKey(docID1)) {
			return graph.get(docID1).contains(docID2);
		} else {
			return false;
		}
	}
	
	public void addVertex (String docID){
		if ( !graph.containsKey(docID) ) {
			List<String> docIDlist = new ArrayList<String>();
			graph.put(docID, docIDlist);
		}
	}
	public void removeVertex (String docID) {
		
	}
	
	public void addEdge (String docID1, String docID2) {
		if ( graph.containsKey(docID1) ) {
			if ( !graph.get(docID1).contains(docID2)) {
				graph.get(docID1).add(docID2);
			}
		} else {
			System.out.println("docID1 does not exist in graph. Add it first.");
		}
		if ( graph.containsKey(docID2) ) {
			if ( !graph.get(docID2).contains(docID1)) {
				graph.get(docID2).add(docID1);
			}
		}
	}
	public void removeEdge (String docID1, String docID2) {
		
	}
	
	/*
	 * Get the neighbors of a vertex
	 */
	public List<String> neighbors (String docID) {
		if ( graph.contains(docID)) {
			return graph.get(docID);
		} else {
			return null;	
		}
	}
	
	public float getVertexScore (String docID) {
		return 0;
	}
	public void setVertexScore (String docID, float score) {
	}
	
	public void printGraph () {
		Iterator<Map.Entry<String, List<String>>> it = graph.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();
			System.out.print(entry.getKey() + ": ");
			List<String> linkedDoc = entry.getValue();
			for (String doc : linkedDoc) {
				System.out.print(doc + " ");
			}
			System.out.println();
		}
	}
}
