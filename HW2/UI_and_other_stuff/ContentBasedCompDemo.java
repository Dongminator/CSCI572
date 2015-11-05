package cs572_HW2;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class ContentBasedCompDemo {

	private static StandardAnalyzer analyzer;

	public static void main(String[] args) throws IOException, ParseException {
		String queryString = "solr";
		String queryField = "text";
		Directory dirIndex = FSDirectory.open(new File("/home/zjy/cs572/index").toPath());
		DirectoryReader dr = DirectoryReader.open(dirIndex);

		int numDocs = dr.numDocs();
		double[] scores = new double[numDocs];
		double[] tf = new double[numDocs];
		double idf;
		double[] norm = new double[numDocs];

		TermsEnum termEnum = MultiFields.getTerms(dr, queryField).iterator();
		BytesRef bytesRef = new BytesRef(queryString.getBytes());

		//compute tf
		if (termEnum.seekExact(bytesRef)) {

			PostingsEnum docsEnum = termEnum.postings(null);

			if (docsEnum != null) {
				int doc;
				while ((doc = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
					//System.out.println(bytesRef.utf8ToString() + " in doc " + doc + ": " + docsEnum.freq());
					tf[doc] = 3 * Math.log(docsEnum.freq() + 1);
				}
			}
		}

		//compute idf
		int docfreq = dr.docFreq(new Term(queryField, bytesRef));
		idf = Math.log((double)numDocs / ((double)docfreq + 1)) + 1;

		//compute norm
		for (int i = 0; i < numDocs; i++) {
			Document doc = dr.document(i);
			List<IndexableField> list = doc.getFields();
			Iterator<IndexableField> it = list.iterator();
			String value = "";
			while (it.hasNext()) {
				Field field = (Field) it.next();
				if(field.name().equals(queryField)){
					value += field.stringValue();
				}
			}
			analyzer = new StandardAnalyzer();
			TokenStream stream = analyzer.tokenStream(null, new StringReader(value));
			//CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			int numTerms = 0;
			while (stream.incrementToken()) {
				//System.out.println(cattr.toString());
				numTerms++;
			}
			norm[i] = 1.0/(Math.sqrt(numTerms));
			stream.end();
			stream.close();
		}
		
		//compute the scores using tf, idf and norm
		//do normalization
		double max = -1;
		for(int i=0;i<numDocs;i++){
			scores[i] = tf[i]*idf*norm[i];
			max = scores[i]>max?scores[i]:max;
		}
		if(max>1){
			for(int i=0;i<numDocs;i++)
				scores[i] /= max;
		}
		
		//do query and conduct comparison
		IndexSearcher searcher = new IndexSearcher(dr);
		QueryParser parser=new QueryParser(queryField,analyzer);
		Query baseQuery=parser.parse(queryString);
		System.out.println("Parsed Query: " + baseQuery.toString());

		TopDocs tds = searcher.search(baseQuery, 10);
		System.out.println("document_id\tlucene_score\tmy_score");
		for(int i=0;i<tds.scoreDocs.length;i++){
			System.out.println(tds.scoreDocs[i].doc+"\t"+tds.scoreDocs[i].score+"\t"+String.format("%.8f", scores[tds.scoreDocs[i].doc]));
		}
		
		dr.close();
		dirIndex.close();
		
	}

}
