package com.ml.converterTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.ml.converter.Word2Vector;


public class Word2VectorTest {

	public static void main( String[] args ) throws IOException
	    {
			Word2Vector w2v = new Word2Vector();
			w2v.prefixResourcePathSet=true;
	    	w2v.initialiazeNLPModels();
	    	w2v.dictionaryAvailable = Boolean.valueOf(args[3]);
	    	w2v.dictionaryPath = String.valueOf(args[4]);
	    	if ( w2v.dictionaryAvailable )
	    		w2v.setDictionaryModel(w2v.dictionaryPath);
	    	int ngrams = Integer.valueOf(args[5]);
	    	w2v.stopwords = Boolean.valueOf(args[6]);
	    	w2v.stemming = Boolean.valueOf(args[7]);

			File fout = new File(args[2]);
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			ArrayList<ArrayList<String>> svmformatList = new ArrayList<ArrayList<String>> ();
			svmformatList.add(new ArrayList<String>() );
			svmformatList.add(new ArrayList<String>() );

			for ( int a=0; a<2; a++ )
			{
			  File folder = new File(args[a]);
			  File[] listOfFiles = folder.listFiles();
			  
			   for (int i = 0; i < listOfFiles.length; i++)
			   {
				      if (listOfFiles[i].isFile()) {
						String content = w2v.readFile(listOfFiles[i].getAbsolutePath());
						String featureStr = w2v.extractlibSVM(content,ngrams);
						svmformatList.get(a).add(featureStr);
						bw.write(a + " " + featureStr);
						bw.newLine();
				      } else if (listOfFiles[i].isDirectory()) {
				        System.out.println("Directory " + listOfFiles[i].getName());
				      }
				}
			}
			bw.close();
	    	if ( !w2v.dictionaryAvailable )
	    	{
				File fout2 = new File(w2v.dictionaryPath);
				FileOutputStream fos2 = new FileOutputStream(fout2);
				BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(fos2));
		    	TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>();
		
				for (Entry<String, Integer> entry : w2v.wordstoInteger.entrySet()) 
				{
					treeMap.put(entry.getValue(), entry.getKey());
				}
				
				for (Entry<Integer, String> entry : treeMap.entrySet()) 
				{
					bw2.write(entry.getValue() + "=" + entry.getKey());
					bw2.newLine();
				}
				bw2.close();
	    	}
	    	
			System.out.println("Naive Bayes file is generated successfully " + (args[2]));
	    }

}
