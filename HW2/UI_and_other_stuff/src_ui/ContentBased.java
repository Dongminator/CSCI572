package cs572_HW2;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class ContentBased {
	//this class does content based term boost
	private Directory dirIndex;
	private DirectoryReader dr;
	private String queryField;
	
	public ContentBased(String path){
		try{
			dirIndex = FSDirectory.open(new File(path).toPath());
			dr = DirectoryReader.open(dirIndex);
			queryField = "text";
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void unaryTermBoost(String keyword, BoostStruct bs){
		try{
			TermsEnum termEnum = MultiFields.getTerms(dr, queryField).iterator();
			BytesRef bytesRef = new BytesRef(keyword.getBytes());
			if (termEnum.seekExact(bytesRef)) {
				PostingsEnum docsEnum = termEnum.postings(null);
				if (docsEnum != null) {
					int doc;
					while ((doc = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
						bs.addScore(doc, 0.5);
					}
				}
			}
			bs.setChanged(true);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void tfidfTermBoost(String keyword, BoostStruct bs){
		try{
			int numDocs = dr.numDocs();
			double[] scores = new double[numDocs];
			double[] tf = new double[numDocs];
			double idf;
			double[] norm = new double[numDocs];

			TermsEnum termEnum = MultiFields.getTerms(dr, queryField).iterator();
			BytesRef bytesRef = new BytesRef(keyword.getBytes());

			//compute tf
			if (termEnum.seekExact(bytesRef)) {
				PostingsEnum docsEnum = termEnum.postings(null);
				if (docsEnum != null) {
					int doc;
					while ((doc = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
						tf[doc] = 3 * Math.log(docsEnum.freq() + 1);
					}
				}
			}

			//compute idf
			int docfreq = dr.docFreq(new Term(queryField, bytesRef));
			idf = Math.log((double)numDocs / ((double)docfreq + 1)) + 1;

			//compute norm using the approximate term counts to speed up
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
				int numTerms = value.length();
				norm[i] = 1.0/(Math.sqrt(numTerms));
			}
			
			//compute the scores using tf, idf and norm
			//do normalization
			double max = -1;
			for(int i=0;i<numDocs;i++){
				scores[i] = tf[i]*idf*norm[i];
				max = scores[i]>max?scores[i]:max;
			}
			if(max>0){
				for(int i=0;i<numDocs;i++)
					scores[i] /= max;
			}
			
			//update scores in the boost struct
			for(int i=0;i<numDocs;i++)
				bs.addScore(i, scores[i]);
			
			bs.setChanged(true);
		}
		catch(IOException e){
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
