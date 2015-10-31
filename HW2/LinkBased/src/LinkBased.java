import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LinkBased {

	
	
	public static void main (String[] args) {
		
		testGraph();
		
		
//		String url = "http://localhost:8983/solr/collection1";
//		
//		SolrServer server = new HttpSolrServer( url );
//		
//		// http://lucene.apache.org/solr/5_3_1/solr-solrj/org/apache/solr/client/solrj/SolrQuery.html
//		SolrQuery query = new SolrQuery();
//	    query.setQuery( "*" );
//	    
//	    
//	    QueryResponse rsp;
//		try {
//			rsp = server.query( query );
//			SolrDocumentList docs = rsp.getResults();
//			System.out.println(docs.size());
//			System.out.println(docs.getNumFound());
//			
//			// http://lucene.apache.org/solr/4_3_1/solr-solrj/org/apache/solr/common/SolrDocument.html
//			SolrDocument sd = docs.get(0);
//			
//			Collection<String> c = sd.getFieldNames();
//			
//			for (String s : c) {
//				System.out.println(s);
//				System.out.println(sd.getFieldValue(s));
//				
//				// here, call geoBasedGraph
//				// input: List<LatLonName> locations, Stirng docId
//				
//				System.out.println();
//			}
//			
//		} catch (SolrServerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public static void testGraph () {
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
		
//		geoBasedGraph.printGraph();
		calcScore(geoBasedGraph, geoDocScore);
	}
	
	/**
	 * Step 1: addToGeoGraph: add all documents to graph. 
	 * Step 2: calcScore (geoBasedGraph, geoDocScore);
	 */
	static Graph geoBasedGraph = new Graph("LinkBased_geo"); // undirected graph
	static Hashtable<String, GeoDoc> geoDocScore = new Hashtable<>();
	
	/**
	 * @author Donglin Pu
	 * This is the core link based algorithm. It calculates the score of each document in the graph.
	 */
	public static void calcScore (Graph g, Hashtable<String, GeoDoc> docWithScore) {
		int iterationNumber = 40;
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
	public static void addToGeoGraph (GeoDoc doc, String docId) {
		int maxPointsToCompare = 6;
		geoBasedGraph.addVertex(docId);
		geoDocScore.put(docId, doc);
		
		/*
		 * locations 0: 1, 1, Loc1 -- primary location always at location 0
		 * lcoations 1: 1, 2, loc2
		 */
		
		float closeThreshold = 2; // if two locations within 10 distance, connect them
		
		for (int i = 0; i < doc.locations.size() && i < maxPointsToCompare; i++) { // we only want the top 5 points. otherwise complexity too high.
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
//						System.out.println(docId + " " + thisLat + ":" + thisLon + " - " + s + " " +existingLat + ":" + existingLon + " = " + pointsDis);
						if ( pointsDis <= Math.pow(closeThreshold, 2)    ) {
							geoBasedGraph.addEdge(docId, s);
//							break outerDocsLoop;
						}
					}
				}
			}
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
	
	public void updateScore (float score) {
		this.score = score;
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
