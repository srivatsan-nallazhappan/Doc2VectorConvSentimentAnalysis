package com.ml.converter;

import java.util.ArrayList;
import java.util.List;

public class PorterStemmer {

	 public static String completeStem(String i) {
		 try {
		 	Stemmer pa = new Stemmer();
	            String s1 = pa.step1(i);
	            String s2 = pa.step2(s1);
	            String s3= pa.step3(s2);
	            String s4= pa.step4(s3);
	            String s5= pa.step5(s4);
	            return s5;
		 }
		 catch (Exception e)
		 {
			// System.out.println("Exception for string " + i);
		 }
		 return i;

	    }
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<String> str =new ArrayList<String>();
		str.add("Extremely");
		str.add("Happiness");
		str.add("Hopping");
		str.add("Filing");
		str.add("plastered");
		str.add("conditional");
		str.add("feudalism");
		str.add("sensitiviti");
		str.add("triplicate");
		str.add("electriciti");
		str.add("allowance");
		str.add("cease");
		str.add("formalize");str.add("positive");
		str.add("s");
		for( String s:str)
		{
			System.out.println(completeStem(s));
		}
	}

}
