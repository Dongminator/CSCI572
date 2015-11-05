package cs572_HW2;

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
//import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.solr.client.solrj.SolrServerException;

public class ReadIndex {

	public static void main(String[] args) throws IOException, ParseException {
		Directory dirIndex = FSDirectory.open(new File("/home/zjy/cs572/index").toPath());
		DirectoryReader dr = DirectoryReader.open(dirIndex);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Document doc = null;
		
		System.out.println("documents count: " + dr.numDocs());
		
		
		//////////////////////////////////////
		//read and output index
		/////////////////////////////////////////
		
		/*for (int i = 0; i < dr.numDocs(); i++) {
			doc = dr.document(i);
			System.out.println("document " + i + ":");
			//System.out.println("<field:boost:value> in document " + i + ":");
			//List<IndexableField> list = doc.getFields();
			//Iterator<IndexableField> it = list.iterator();
			//while (it.hasNext()) {
				//Field field = (Field)it.next();
				//System.out.println("<"+field.name() + ":" + field.boost() + ":" + field.stringValue() + ">");
			//}
			if(true){
				doc.add(new LongField("geoBoost", i, Field.Store.YES));
			}
			System.out.println();
		}*/
		
		//////////////////////////////////
		//update index
		//////////////////////////////////////////
		
		/*IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(dirIndex, conf);
		for (int i = 0; i < dr.numDocs(); i++) {
			doc = dr.document(i);
			System.out.println("updating document " + i + ":");
			doc.add(new NumericDocValuesField("geoBoost", i));
			writer.updateDocument(new Term("id", doc.getField("id").stringValue()),doc);
		}
		
		writer.close();*/
		
		
		////////////////////////////////
		//query index
		/////////////////////////////////////////
		
		String field="text";
		String queryString = "solr";
		IndexSearcher searcher = new IndexSearcher(dr);
		QueryParser parser=new QueryParser(field,analyzer);
		//parser.setAllowLeadingWildcard(true);
		Query baseQuery=parser.parse(queryString);
		FunctionQuery boostQuery = new FunctionQuery(new LongFieldSource("geoBoost"));
		Query q = new CustomScoreQuery(baseQuery, boostQuery);
		System.out.println(baseQuery.toString());
		System.out.println(boostQuery.toString());
		System.out.println(q.toString());
		
		TopDocs tds = searcher.search(baseQuery, 10);
		System.out.println(tds.totalHits);
		for(int i=0;i<tds.scoreDocs.length;i++){
			System.out.print(tds.scoreDocs[i].doc+":"+tds.scoreDocs[i].score+" ");
		}
		
		System.out.println("");
		
		tds = searcher.search(q, 10);
		System.out.println(tds.totalHits);
		for(int i=0;i<tds.scoreDocs.length;i++){
			System.out.print(tds.scoreDocs[i].doc+":"+tds.scoreDocs[i].score+" ");
		}
		
		System.out.println("");
		Explanation explain = searcher.explain(baseQuery, 0);
		printExplanation(explain);
		

		dr.close();
		dirIndex.close();

	}

	private static void printExplanation(Explanation explain) {
		System.out.println(explain.getDescription());
		Explanation[] exp = explain.getDetails();
		for(int i=0;i<exp.length;i++){
			printExplanation(exp[i]);
		}
	}

}
