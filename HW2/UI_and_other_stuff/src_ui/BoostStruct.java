package cs572_HW2;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class BoostStruct {
	//this class stores boost information, flag, score, etc.
	private boolean isBoosted;
	private boolean isChanged;
	private int numDocs;
	private double[] scores;
	
	
	public BoostStruct(String path){
		try{
			isBoosted = false;
			isChanged = false;
			Directory dirIndex = FSDirectory.open(new File(path).toPath());
			DirectoryReader dr = DirectoryReader.open(dirIndex);
			numDocs = dr.numDocs();
			scores = new double[numDocs];
			clearScores();
			dr.close();
			dirIndex.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void addScore(int doc, double score){
		scores[doc] += score;
	}
	
	public double getScore(int doc){
		return scores[doc];
	}
	
	public void normalizeScores(){
		double max = -1;
		for(int i=0;i<numDocs;i++)
			max = scores[i]>max?scores[i]:max;
		if(max>0){
			for(int i=0;i<numDocs;i++)
				scores[i] /= max;
		}
	}
	
	public void setBoosted(boolean bool){
		isBoosted = bool;
	}
	
	public boolean isBoosted(){
		return isBoosted;
	}
	
	public void setChanged(boolean bool){
		isChanged = bool;
	}
	
	public boolean isChanged(){
		return isChanged;
	}
	
	public void clearScores(){
		for(int i=0;i<numDocs;i++)
			scores[i] = 0.1;
	}
	
	public void printScores(){
		//used in debug
		for(int i=0;i<numDocs;i++)
			System.out.println(scores[i]);
	}
}
