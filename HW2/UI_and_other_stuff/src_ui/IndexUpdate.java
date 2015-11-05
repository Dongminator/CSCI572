package cs572_HW2;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexUpdate {
	//this class does index updating and restoring
	
	public static void update(String path, BoostStruct bs) {
		try {
			if(!bs.isChanged()){
				System.out.println("No boost has been made yet!");
				return;
			}
			bs.setChanged(false);
			bs.normalizeScores();
			Directory dirIndex = FSDirectory.open(new File(path).toPath());
			DirectoryReader dr = DirectoryReader.open(dirIndex);
			StandardAnalyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig conf = new IndexWriterConfig(analyzer);
			IndexWriter writer = new IndexWriter(dirIndex, conf);
			for (int i = 0; i < dr.numDocs(); i++) {
				Document doc = dr.document(i);
				writer.updateNumericDocValue(new Term("myID", doc.get("myID")),"boost", (long)(bs.getScore(i)*100));
			}
			
			bs.setBoosted(true);
			bs.clearScores();
			writer.close();
			dr.close();
			dirIndex.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void restore(String path, BoostStruct bs) {
		bs.setBoosted(false);
	}
}
