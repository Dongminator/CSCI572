package cs572_HW2;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexPreProcess {

	public static void main(String[] args) throws IOException {
		// pre-process the index
		// to each document, add a new NumericDocValuesField with the name "boost" and the value 1
		// also add a new field named "myID" and the value equals to document id
		Directory dirIndex = FSDirectory.open(new File("/home/zjy/cs572/index").toPath());
		DirectoryReader dr = DirectoryReader.open(dirIndex);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(dirIndex, conf);
		for (int i = 0; i < dr.numDocs(); i++) {
			Document doc = dr.document(i);
			System.out.println("updating document " + i + ":");
			doc.add(new NumericDocValuesField("boost", 1));
			doc.add(new StringField("myID", Integer.toString(i), Field.Store.YES));
			writer.updateDocument(new Term("id", doc.getField("id").stringValue()),doc);
		}
		System.out.println("DONE!");
		
		writer.close();
		dr.close();
		dirIndex.close();
	}

}
