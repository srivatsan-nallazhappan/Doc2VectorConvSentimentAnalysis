package com.ml.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import opennlp.tools.ngram.NGramGenerator;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

/**
 * Hello world!
 *
 */
public class Word2Vector 
{
	private static TokenizerModel tokenmodel = null;
	private static SentenceModel sentencemodel = null;
	private static HashSet<String> mstopwords = new HashSet<String> ();
	private static HashMap<String,String> abbrevations = new HashMap<String,String> ();
	public static HashMap<String,Integer> wordstoInteger = new HashMap<String,Integer> ();
	
	public static boolean dictionaryAvailable = true;
	public static String dictionaryPath = "resources/dictionary.txt";
	private static int featurecounter = 1;
	public static boolean prefixResourcePathSet = false;
	public static boolean stopwords = true;
	public static boolean stemming = true;


	public static void setTokenizerModel () throws InvalidFormatException, IOException
	{
		InputStream is = null;
		if ( prefixResourcePathSet )
		  is = new FileInputStream("src/main/resources/en-token.bin");
		else
		 is = Word2Vector.class.getClassLoader().getResourceAsStream("resources/en-token.bin");
		tokenmodel = new TokenizerModel(is);
	}
	
	public static void setAbbrevationsModel ()
	{
		  String strLine = new String();
		  try {
				InputStream is = null;
				if ( prefixResourcePathSet )
				  is = new FileInputStream("src/main/resources/abbrevations.txt");
				else
				  is = Word2Vector.class.getClassLoader().getResourceAsStream("resources/abbrevations.txt");
			  InputStreamReader isr = new InputStreamReader(is);
			  BufferedReader br = new BufferedReader(isr);
			  while ((strLine = br.readLine()) != null)
			  {
				 String[] tokens = strLine.split("=");
				 abbrevations.put(tokens[0], tokens[1]);
			  }
		  }
		  catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(strLine);
			}
	}

	public static void setDictionaryModel (String path)
	{
		  String strLine = new String();
		  try {
				InputStream is = null;
				if ( prefixResourcePathSet )
				  is = new FileInputStream(path);
				else
				  is = Word2Vector.class.getClassLoader().getResourceAsStream(path);
			  InputStreamReader isr = new InputStreamReader(is);
			  BufferedReader br = new BufferedReader(isr);
			  while ((strLine = br.readLine()) != null)
			  {
				  if (strLine.equals("")) continue;
				 String[] tokens = strLine.split("=");
				 wordstoInteger.put(tokens[0], Integer.valueOf(tokens[1]));
				 featurecounter=Integer.valueOf(tokens[1]);
			  }
		  }
		  catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(strLine);
			}
	}

	
	private static void setSentenceModel () throws InvalidFormatException, IOException
	{
		InputStream is = null;
		if ( prefixResourcePathSet )
		  is = new FileInputStream("src/main/resources/en-sent.bin");
		else
		  is = Word2Vector.class.getClassLoader().getResourceAsStream("resources/en-sent.bin");
		sentencemodel = new SentenceModel(is);
	}

	public static String[] segmentSentence(String str)
	{
		SentenceDetectorME sdetector = new SentenceDetectorME(sentencemodel);
		String sentences[] = sdetector.sentDetect(str);
		return sentences;
	}
	
	public static String toLowerCase(String str)
	{
		return  str.toLowerCase();
	}

	
	private static void setStopWordsModel()
	{
		  String strLine = new String();
		  try {
				InputStream is = null;
				if ( prefixResourcePathSet )
				  is = new FileInputStream("src/main/resources/stopwords.txt");
				else
				   is = Word2Vector.class.getClassLoader().getResourceAsStream("resources/stopwords.txt");

			  InputStreamReader isr = new InputStreamReader(is);
			  BufferedReader br = new BufferedReader(isr);
		  while ((strLine = br.readLine()) != null)
		  {
			 mstopwords.add(strLine);
		  }
		  }
		  catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(strLine);
			}
	}
	
	
	public static String[] tokenize(String str)
	{
		Tokenizer tokenizer = new TokenizerME(tokenmodel);
		String tokens[] = tokenizer.tokenize(str);
		return tokens;
	}
	
	public static ArrayList<String> handleNegation(String[] tokens)
	{
		ArrayList<String> newArr = new ArrayList<String>();
		for(int i=0; i<tokens.length;i++)
		{
			if(tokens[i].equals("not") )
			{
				if ( i < tokens.length-2 )
					newArr.add("not_" + PorterStemmer.completeStem(tokens[i+1]));
				i++;
			}
			else
			{
				newArr.add(tokens[i]);
			}
		}
		return newArr;
	}
	
	public static ArrayList<String> removeStopWords(ArrayList<String> strTokens)
	{
		if ( stopwords == false ) return strTokens;
		ArrayList<String> newArr = new ArrayList<String>();
		for(String str:strTokens )
		{
			if(!mstopwords.contains(str))
			{
				newArr.add(str);
			}
		}
		return newArr;
	}
	
	public static ArrayList<String> applyStemming(ArrayList<String> strTokens, boolean excludeNot)
	{
		if ( stemming == false ) return strTokens;
		ArrayList<String> newArr = new ArrayList<String>();
		for(String str:strTokens )
		{
			if ( str.equals(" ")) continue;
			if( excludeNot && str.contains("not") )
			{
				newArr.add(str);
			}
			else
			{
				newArr.add(PorterStemmer.completeStem(str));
			}
		}
		return newArr;
	}

	
	public static String applyContractions(String inputString)
	{
		    inputString = inputString.replaceAll("n't", " not");
		    inputString = inputString.replaceAll("'re", " are");
		    inputString = inputString.replaceAll("'m", " am");
		    inputString = inputString.replaceAll("'ll", " will");
		    inputString = inputString.replaceAll("'ve", " have");
		    inputString = inputString.replaceAll("'d", " would");
		    inputString = inputString.replaceAll("'s", "s");

		    return inputString;
	}
	
	public static String expandAbbrevations(String inputString)
	{
		for (Entry<String, String> entry : abbrevations.entrySet()) 
		{
		    inputString = inputString.replaceAll(entry.getKey() ,  entry.getValue());
		}
		return inputString;
	}

	
	public static String removeNonAlphaCharacters(String instring)
	{
		return instring.replaceAll("[^a-zA-Z ]", "");
	}
	
	public static String readFile(String file)  {
	    try {

	    BufferedReader reader = new BufferedReader(new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        reader.close();
	        return stringBuilder.toString();

	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    	
	    }
	    return "";
	}
	
	public static void initialiazeNLPModels()
	{
		try
		{
	    	setStopWordsModel();
	    	setTokenizerModel();
	    	setSentenceModel();
	    	setAbbrevationsModel();
	    	
		}
		catch (Exception e)
		{
			System.out.println("Error while intializing models ");
			e.printStackTrace();
			System.exit(1);
		}
	    System.out.println("NLPModels are initialised " );

	}
	
	public static void initialiazeModels()
	{
		initialiazeNLPModels();
   		setDictionaryModel(dictionaryPath);
	}



    public static void main( String[] args ) throws IOException
    {
    	prefixResourcePathSet=true;
    	initialiazeNLPModels();
    	dictionaryAvailable = Boolean.valueOf(args[3]);
    	dictionaryPath = String.valueOf(args[4]);
    	if ( dictionaryAvailable )
       		setDictionaryModel(dictionaryPath);
    	int ngrams = Integer.valueOf(args[5]);
    	stopwords = Boolean.valueOf(args[6]);
    	stemming = Boolean.valueOf(args[7]);

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
					String content = readFile(listOfFiles[i].getAbsolutePath());
					String featureStr = extractlibSVM(content,ngrams);
					svmformatList.get(a).add(featureStr);
					bw.write(a + " " + featureStr);
					bw.newLine();
			      } else if (listOfFiles[i].isDirectory()) {
			        System.out.println("Directory " + listOfFiles[i].getName());
			      }
			}
		}
		bw.close();
    	if ( !dictionaryAvailable )
    	{
			File fout2 = new File(dictionaryPath);
			FileOutputStream fos2 = new FileOutputStream(fout2);
			BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(fos2));
	    	TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>();
	
			for (Entry<String, Integer> entry : wordstoInteger.entrySet()) 
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
    
    public static int[] getIndices(String featureStr)
    {
    	String fstr = featureStr.trim();
    	String[] tk = featureStr.split(" ");
    	int[] ret = new int[tk.length];
    	for ( int i=0; i<tk.length; i++ )
    	{
    		String[] kv = tk[i].split(":");
    		ret[i]=Integer.valueOf(kv[0]);
    	}
    	return ret;
    }
    
    public static double[] getValues(String featureStr)
    {
    	String fstr = featureStr.trim();
    	String[] tk = fstr.split(" ");
    	double[] ret = new double[tk.length];
    	for ( int i=0; i<tk.length; i++ )
    	{
    		String[] kv = tk[i].split(":");
    		ret[i]=Double.valueOf(kv[1]);
    	}
    	return ret;
    }
    
    public static String extractNaiveBayesFormat(String svmformat )
    {
    	/*SVM format is sorted, use the sorted information to our advantage to form bayes format*/
    	String nbformat = "";
    	String[] tokens = svmformat.split(" ");
    	int svmpos=0; //svmformat initial index position
    	String svmindex = "";
    	String svmvalue = "";
    	if ( svmpos<tokens.length &&  !tokens[svmpos].trim().equals("") )
    	{
    		svmindex = tokens[svmpos].split(":")[0];
    		svmvalue = tokens[svmpos].split(":")[1];
    	}
    	for ( int i=1; i<=featurecounter; i++)
    	{
    		if ( svmindex.equals(String.valueOf(i) ) )
    		{
    			Double d_svmvalue = Double.valueOf(svmvalue);
    			nbformat = nbformat + " " + d_svmvalue;
    			svmpos++;
    	    	if ( svmpos<tokens.length &&  !tokens[svmpos].trim().equals("") )
    	    	{
    	    		svmindex = tokens[svmpos].split(":")[0];
    	    		svmvalue = tokens[svmpos].split(":")[1];
    	    	}
    	    	else
    	    	{
    	    		//reached end of line
    	    		svmindex = "";
    	    		svmvalue = "";
    	    	}
    		}
    		else
    		{
    			nbformat = nbformat + " " + "0.0";
    		}
    		
    	}
    	
    	return nbformat;
    }
    
    public static String extractlibSVM(String content, int ngrams )
    {
    	String[] sentences = segmentSentence(content);
    	HashMap<String, Integer> termfreq = new HashMap<String, Integer>();
    	for ( String sentence: sentences )
    	{
    		//System.out.println("The sentence is " + sentence);
    		String lowercase = toLowerCase(sentence);
    		//System.out.println("Lower case sentence is " + lowercase);

    		String contracted = applyContractions(lowercase);
    		//System.out.println("Contracted sentence is " + contracted);

    		String abbrevated = expandAbbrevations(contracted);
    		//System.out.println("Expanded abbrevated sentence is " + abbrevated);

    		String resultStr = removeNonAlphaCharacters(abbrevated);
    		//System.out.println("Removed Non-alpha sentence " + resultStr);

    		String[] tokenized=tokenize(resultStr);
    		
	    	//System.out.println("=====One gram words=====");
    		ArrayList<String> negatedtokens = handleNegation(tokenized);
    		
	    	ArrayList<String> withoutstoptokens = removeStopWords(negatedtokens);
	    	ArrayList<String> onegram = applyStemming(withoutstoptokens, true);
	    	addToTermFrequency(termfreq,onegram);

	    	//two gram words
	    	ArrayList<String> stemtokens2 = applyStemming(negatedtokens, true);
	    	if ( ngrams >= 2 )
	    	{
		    	ArrayList<String> twograms = (ArrayList<String>) NGramGenerator.generate(stemtokens2, 2, "-");
		    	addToTermFrequency(termfreq,twograms);
	    	}
	    	if ( ngrams == 3 )
	    	{
		    	ArrayList<String> threegrams = (ArrayList<String>) NGramGenerator.generate(stemtokens2, 3, "-");
		    	addToTermFrequency(termfreq,threegrams);
	    	}
    	}
    	
    	if ( !dictionaryAvailable )
    		addToDictionary(termfreq);
    	String featureStr = "";
    	TreeMap<Integer, Integer> hshMap = new TreeMap<Integer, Integer>();
		for (Entry<String, Integer> entry : termfreq.entrySet()) 
		{
			Integer strhash = wordstoInteger.get(entry.getKey());
			if ( strhash == null )
			{
				//System.out.println("WARN: Word " + entry.getKey() + " , is not in vocabolary list ");
				continue;
			}
			hshMap.put(strhash, entry.getValue());
		}
		
		for (Entry<Integer, Integer> entry : hshMap.entrySet()) 
			featureStr = featureStr + entry.getKey() +":" + entry.getValue() + " " ;

    	return featureStr;

    }
    
    public static void addToDictionary(HashMap<String, Integer> terms)
    {
		for (Entry<String, Integer> entry : terms.entrySet()) 
		{
			String term = entry.getKey();
			if ( wordstoInteger.get(term) == null )
			{
				wordstoInteger.put(term, featurecounter);
				featurecounter++;
			}
		}
    }
    
    private static void addToTermFrequency(HashMap<String, Integer> termfreq, ArrayList<String> ngramList )
    {
    	for ( String ss: ngramList )
    	{
    		Integer cnt = termfreq.get(ss);
    		if ( cnt == null )
    			cnt = 1;
    		else
    			cnt = cnt +1;
    		termfreq.put(ss,cnt);
    	}
    }

    
    
}
