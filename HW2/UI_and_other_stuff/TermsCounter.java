package cs572_HW2;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class TermsCounter {

	public static void main(String[] args) throws IOException {
		Directory dirIndex = FSDirectory.open(new File("/home/zjy/cs572/index").toPath());
		DirectoryReader dr = DirectoryReader.open(dirIndex);

		Fields fields = MultiFields.getFields(dr);
		for (String field : fields) {

			TermsEnum termEnum = MultiFields.getTerms(dr, field).iterator();
			BytesRef bytesRef;
			while ((bytesRef = termEnum.next()) != null) {
				if (termEnum.seekExact(bytesRef)) {

					PostingsEnum docsEnum = termEnum.postings(null);

					if (docsEnum != null) {
						int doc;
						while ((doc = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
							System.out.println(bytesRef.utf8ToString() + " in doc " + doc + ": " + docsEnum.freq());
						}
					}
				}
			}
		}

		for (String field : fields) {
			TermsEnum termEnum = MultiFields.getTerms(dr, field).iterator();
			BytesRef bytesRef;
			while ((bytesRef = termEnum.next()) != null) {
				int freq = dr.docFreq(new Term(field, bytesRef));

				System.out.println(bytesRef.utf8ToString() + " in " + freq + " documents");

			}
		}

	}

}
