import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import Jama.Matrix;
import Jama.SingularValueDecomposition;


public class GRELSAUsingHolmesCorpus {
	private static Scanner reader;
	private static Scanner reader2;
	private static Scanner reader3;
	private static Scanner reader4;

	static int globalCounter = 0;
	static Map<String, Integer> wordlist = new HashMap<String, Integer>();
	static List<Integer> wordCount = new ArrayList<Integer>();
	static int wordlistCount;
	static double[][] TERM_DOCUMENT_MATRIX = null;

	static final int NUM_FACTORS = 300;
	static double[][] termVectors = null;
	static double[] scales = null;
	private static Matrix A;

	public static void main(String[] args) throws IOException {
		
		final File folder = new File("Holmes_Training_Data");
		countDistinctWords(folder);
		TERM_DOCUMENT_MATRIX = new double[wordlist.size()][523];
		System.out.println("in training");
		//System.out.println(wordlist.toString());
		listFilesForFolder(folder);
		wordlistCount = wordlist.size();
		//printArray(TERM_DOCUMENT_MATRIX);
		A = new Matrix(TERM_DOCUMENT_MATRIX);
		buildSVD();
		test();

	}

	private static void test() {
		// TODO Auto-generated method stub
		System.out.println("in testing");

		ArrayList<String> testAnswerArray = new ArrayList<String>();
		HashMap<Integer, String> answerInttoStringMap = new HashMap<Integer, String>();
		answerInttoStringMap.put(1, "a");
		answerInttoStringMap.put(2, "b");
		answerInttoStringMap.put(3, "c");
		answerInttoStringMap.put(4, "d");
		answerInttoStringMap.put(5, "e");
		final File folder2 = new File(
				"testing_data/Holmes.machine_format.questions.txt");

		int optionCount = 1;
		// int answerNumber = 1;
		int maxAnswerNumber = 0;

		double maxSentenceProbability = 0;

		if (folder2.isFile()) {

			try {
				reader2 = new Scanner(folder2);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int lineno = 0;
			while (reader2.hasNextLine()) {
				if (optionCount == 6) {
					if (maxAnswerNumber == 0)
						maxAnswerNumber = 1;
					testAnswerArray.add(answerInttoStringMap
							.get(maxAnswerNumber));
					optionCount = 1;
					maxSentenceProbability = 0;
					//System.out.println(answerInttoStringMap.get(maxAnswerNumber));
					maxAnswerNumber = 0;

				}
				//
				lineno++;
				//System.out.println(lineno);
				String line = reader2.nextLine();
				String line2 = line;
				line = line.substring(line.indexOf(")") + 1, line.length());
				line = line.replaceAll("[^A-Za-z\\[\\] ] ", "");
				// line= line.replaceAll("\\p{Punct}+\\[\\]", "");
				line = line.replaceAll("\\s+", " ");
				line = line.trim();
				int targetWordIndex = 0;
				String[] words = line.split(" ");
				for (int i = 0; i < words.length; i++) {
					if (words[i].startsWith("[")) {
						// System.out.println(words[i]);
						targetWordIndex = i;
					}
				}

				line = line.replaceAll("[^A-Za-z ]", "");
				// line = line.replaceAll("\\s+", " ");
				// reader4.useDelimiter("[^A-Za-z]+");

				words = line.split(" ");
				String term = words[targetWordIndex];
				// String previousWord = null;
				double[] queryVector = new double[NUM_FACTORS];
				Arrays.fill(queryVector, 0.0);
				addTermVector(term, termVectors, queryVector);
				double score = 0;
				if (wordlist.containsKey(term)) {
					for (int i = 0; i < words.length; i++) {

						if (i != targetWordIndex) {

							if (wordlist.containsKey(words[i])) {
								int index = wordlist.get(words[i]);
								score = score
										+ cosine(queryVector,
												termVectors[index], scales);
							/* 
							queryVector- Target word's count(the column of the target word in termVectors) in reduced document space
							 termVector- Remaining word's count ("") in reduced document space
							 */
							}
						}
					}
				}

				double sentenceProbability = score;
				if (maxSentenceProbability < sentenceProbability) {
					maxSentenceProbability = sentenceProbability;

					maxAnswerNumber = optionCount;

				}
				
				//System.out.println(sentenceProbability);

				// answerNumber++;
				optionCount++;
			}
			if (maxAnswerNumber == 0)
				maxAnswerNumber = 1;
			testAnswerArray.add(answerInttoStringMap.get(maxAnswerNumber));

		}

		// answer in testing
		ArrayList<String> testActualAnswerArray = new ArrayList<String>();

		final File folder3 = new File(
				"testing_data/Holmes.machine_format.answers.txt");

		if (folder3.isFile()) {

			try {
				reader3 = new Scanner(folder3);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// int answerNumber = 1;
			while (reader3.hasNextLine()) {
				// double maxSentenceProbability = 0;

				String line = reader3.nextLine();
				line = line.substring(line.indexOf(")") - 1, line.indexOf(")"));
				testActualAnswerArray.add(line);

			}
		}

		// code for counting errors in classification
		System.out.println("testanswers " + testAnswerArray.toString());
		System.out.println("testanswerssize " + testAnswerArray.size());
		System.out.println("testActualanswers "
				+ testActualAnswerArray.toString());
		System.out.println("testActualanswerssize "
				+ testActualAnswerArray.size());
		int testErrorCount = 0;
		int testPresenceErrorCount = 0;
		for (int i = 0; i < (testActualAnswerArray.size()); i++) {
			if (!testAnswerArray.get(i).equalsIgnoreCase(
					testActualAnswerArray.get(i))) {

				testErrorCount += 1;
			}
		}

		System.out.println("Test Answer Prediction Accuracy: "
				+ (double) (testActualAnswerArray.size() - testErrorCount)
				* 100 / testActualAnswerArray.size() + "%");

		System.out.println("-----------------------");
		System.out.println("");
	}

	public static void printArray(double matrix[][]) {
		for (double[] row : matrix)
			System.out.println(Arrays.toString(row));
	}

	public static void buildSVD() {
		//double featureInit = 0.01;
		//double initialLearningRate = 0.005;
		//int annealingRate = 1000;
		//double regularization = 0.00;
		//double minImprovement = 0.0000;
		//int minEpochs = 10;
		//int maxEpochs = 50000;

		
		SingularValueDecomposition s = A.svd();
		
		Matrix U = s.getU();

		scales = s.getSingularValues();
		termVectors = U.getArray();
	
		//printArray(termVectors);
//		System.out.println("\nSCALES");
//		for (int k = 0; k < NUM_FACTORS; ++k)
//			System.out.printf("%d  %4.2f\n", k, scales[k]);
	}

	static void addTermVector(String term, double[][] termVectors,
			double[] queryVector) {
		int i = 0;
		// query vector has the extracted row, word count in all docs (reduced number) after reduction using SVD
		if (wordlist.containsKey(term)) {
			i = wordlist.get(term);

			// query vector contains the count of each word in each document, using global count index for word in place of i
			for (int j = 0; j < NUM_FACTORS; ++j) {
				queryVector[j] += termVectors[i][j];
			}
		}

	}


	static double cosine(double[] xs, double[] ys, double[] scales) {
		double product = 0.0;
		double xsLengthSquared = 0.0;
		double ysLengthSquared = 0.0;
		for (int k = 0; k < xs.length; ++k) {
			double sqrtScale = Math.sqrt(scales[k]);
			double scaledXs = sqrtScale * xs[k];
			double scaledYs = sqrtScale * ys[k];
			xsLengthSquared += scaledXs * scaledXs;
			ysLengthSquared += scaledYs * scaledYs;
			product += scaledXs * scaledYs;
		}
		// product of values/ square root of sum of squares of values in both vectors
		return product / Math.sqrt(xsLengthSquared * ysLengthSquared);
	}

	public static void countDistinctWords(final File folder)
			throws IOException, FileNotFoundException,
			UnsupportedEncodingException {
		File folder2 = new File(
				"testing_data/Holmes.machine_format.questions.txt");
		File[] listOfFiles = folder2.listFiles();
		HashMap<String, Integer> unigramWordCount = new HashMap<String, Integer>();
		HashMap<String, Integer> bigramTotalWordCount = new HashMap<String, Integer>();

		if (folder2.isFile()) {
			reader4 = new Scanner(folder2);

			while (reader4.hasNextLine()) {

				String line = reader4.nextLine();

				String previousWord = null;
				while (reader4.hasNext()) {
					String word = reader4.next();

					if (unigramWordCount.containsKey(word))

						unigramWordCount.put(word,
								unigramWordCount.get(word) + 1);
					else {
						unigramWordCount.put(word, 1);
					}
					if (previousWord != null) {
						String bigram = previousWord + " " + word;

						if (bigramTotalWordCount.containsKey(bigram))

							bigramTotalWordCount.put(bigram,
									bigramTotalWordCount.get(bigram) + 1);
						else {
							bigramTotalWordCount.put(bigram, 1);
						}

					}
					previousWord = word;
				}

			}

		}
		String currentLine = "";
		for (final File fileEntry : folder.listFiles()) {
			int wordsInDocument = 0;
			BufferedReader bReader = new BufferedReader(new FileReader(
					fileEntry));
			while ((currentLine = bReader.readLine()) != null) {
				currentLine = currentLine.replaceAll("[^A-Za-z ]", " ");
				currentLine = currentLine.replaceAll("\\s+", " ");
				currentLine = currentLine.toLowerCase();
				String[] temp = currentLine.split(" ");
				for (int index = 0; index < temp.length; index++) {
					if (temp[index].trim().length() > 3) {
						if (!wordlist.containsKey(temp[index])
								&& unigramWordCount.containsKey(temp[index])) {
							wordsInDocument++;
							wordlist.put(temp[index], globalCounter);
							globalCounter++;
							// global counter is for new words over all the docs. i.e each new occurence
						}
					}
				}
			}
			wordCount.add(wordsInDocument);
			//unique word count in each document
		}
	}

	public static void listFilesForFolder(final File folder)
			throws FileNotFoundException, UnsupportedEncodingException {
		String currentLine = "";
		int counter = -1;
		for (final File fileEntry : folder.listFiles()) {
		// for checking number of occurences of each word in each document
			Map<String, Integer> wordCount = new HashMap<String, Integer>();
			counter++;
			try {
				BufferedReader bReader = new BufferedReader(new FileReader(
						fileEntry));
				while ((currentLine = bReader.readLine()) != null) {
					currentLine = currentLine.replaceAll("[^A-Za-z ]", " ");
					currentLine = currentLine.replaceAll("\\s+", " ");
					currentLine = currentLine.toLowerCase();
					String[] temp = currentLine.split(" ");
					for (int index = 0; index < temp.length; index++) {
						if (temp[index].trim().length() > 3) {
							if (wordCount.containsKey(temp[index])) {
								wordCount.put(temp[index],
										wordCount.get(temp[index]) + 1);
							} else {
								wordCount.put(temp[index], 1);
							}
						}
					}
				}
				Iterator it = wordCount.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					// for each unique word in the document check it against the whole training set(wordlist)
					if (wordlist.containsKey(pairs.getKey())) {
					//documents are columns and word is rows (values will be word counts)
					//
						TERM_DOCUMENT_MATRIX[wordlist.get(pairs.getKey())][counter] = (int) pairs
								.getValue();
					}

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	

}
