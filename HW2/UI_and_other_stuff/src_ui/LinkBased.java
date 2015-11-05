package cs572_HW2;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LinkBased {
	// this class does link based relevancy boost
	private Directory dirIndex;
	private DirectoryReader dr;
	private String geoBoostFieldName;
	private String timeBoostFieldName;
	private String gunTypeBoostFieldName;

	public LinkBased(String path) {
		try {
			dirIndex = FSDirectory.open(new File(path).toPath());
			dr = DirectoryReader.open(dirIndex);
			geoBoostFieldName = "linkbased_geo";
			timeBoostFieldName = "linkbased_time";
			gunTypeBoostFieldName = "linkbased_guntype";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void geoBoost(BoostStruct bs) {
		try {
			for (int i = 0; i < dr.numDocs(); i++) {
				Document doc = dr.document(i);
				List<IndexableField> list = doc.getFields();
				Iterator<IndexableField> it = list.iterator();
				while (it.hasNext()) {
					Field field = (Field) it.next();
					if(field.name().equals(geoBoostFieldName))
						bs.addScore(i, (double)field.numericValue());
				}
			}
			bs.setChanged(true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void timeBoost(BoostStruct bs) {
		try {
			for (int i = 0; i < dr.numDocs(); i++) {
				Document doc = dr.document(i);
				List<IndexableField> list = doc.getFields();
				Iterator<IndexableField> it = list.iterator();
				while (it.hasNext()) {
					Field field = (Field) it.next();
					if(field.name().equals(timeBoostFieldName))
						bs.addScore(i, (double)field.numericValue());
				}
			}
			bs.setChanged(true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void gunTypeBoost(BoostStruct bs) {
		try {
			for (int i = 0; i < dr.numDocs(); i++) {
				Document doc = dr.document(i);
				List<IndexableField> list = doc.getFields();
				Iterator<IndexableField> it = list.iterator();
				while (it.hasNext()) {
					Field field = (Field) it.next();
					if(field.name().equals(gunTypeBoostFieldName))
						bs.addScore(i, (double)field.numericValue());
				}
			}
			bs.setChanged(true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void close() {
		try {
			dr.close();
			dirIndex.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
