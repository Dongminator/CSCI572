package cs572_HW2;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class MyQuery {
	//this class does index query
	private Directory dirIndex;
	private DirectoryReader dr;
	private StandardAnalyzer analyzer;
	private IndexSearcher searcher;
	private String field;
	private QueryParser parser;
	
	public MyQuery(String path){
		try{
			dirIndex = FSDirectory.open(new File(path).toPath());
			dr = DirectoryReader.open(dirIndex);
			analyzer = new StandardAnalyzer();
			searcher = new IndexSearcher(dr);
			field = "text";
			parser=new QueryParser(field,analyzer);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void doBaseQuery(String queryString){
		try {
			Query baseQuery=parser.parse(queryString);
			System.out.println("Parsed Query: "+baseQuery.toString());
			TopDocs tds = searcher.search(baseQuery, 10);
			System.out.println("Total Hits:"+tds.totalHits);
			for(int i=0;i<tds.scoreDocs.length;i++){
				String id = dr.document(tds.scoreDocs[i].doc).getField("id").stringValue();
				System.out.println(tds.scoreDocs[i].score+"\t"+id);
			}
			System.out.println("");
		} 
		catch(ParseException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void doBoostedQuery(String queryString){
		try {
			Query baseQuery=parser.parse(queryString);
			FunctionQuery boostQuery = new FunctionQuery(new LongFieldSource("boost"));
			Query q = new CustomScoreQuery(baseQuery, boostQuery);
			System.out.println("Parsed Query: "+q.toString());
			TopDocs tds = searcher.search(q, 10);
			System.out.println("Total Hits:"+tds.totalHits);
			for(int i=0;i<tds.scoreDocs.length;i++){
				String id = dr.document(tds.scoreDocs[i].doc).getField("id").stringValue();
				System.out.println(String.format("%.8f",tds.scoreDocs[i].score)+"\t"+id);
			}
			System.out.println("");
		} 
		catch(ParseException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try{
			dr.close();
			dirIndex.close();
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
}
