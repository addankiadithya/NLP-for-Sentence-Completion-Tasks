import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.morph.WordnetStemmer;

public class GREBackoffModelMSWebService_RootWord {
	private static Scanner reader2;
	private static Scanner reader3;

	public static void main(String[] args) throws IOException {
		System.setProperty("wordnet.database.dir",
				"C:/Program Files (x86)/WordNet/2.1/dict");
		ArrayList<String> testAnswerArray = new ArrayList<String>();
		HashMap<Integer, String> answerInttoStringMap = new HashMap<Integer, String>();
		answerInttoStringMap.put(1, "a");
		answerInttoStringMap.put(2, "b");
		answerInttoStringMap.put(3, "c");
		answerInttoStringMap.put(4, "d");
		answerInttoStringMap.put(5, "e");
		File folder = new File(
				"testing_data/Holmes.machine_format.questions_s.txt");
		int optionCount = 1;
		int maxAnswerNumber = 0;
		double maxSentenceProbability = 0;
		if (folder.isFile()) {
			try {
				reader2 = new Scanner(folder);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			String s = "http://weblm.research.microsoft.com/weblm/rest.svc/bing-body/apr10/4/jp?u=0be45235-bfa5-4e20-88ae-c098e79547fb";
			URL url = new URL(s);
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			while (reader2.hasNextLine()) {
				if (optionCount == 6) {
					testAnswerArray.add(answerInttoStringMap
							.get(maxAnswerNumber));
					optionCount = 1;
					maxSentenceProbability = 0;
					maxAnswerNumber = 0;
				}
				String line = reader2.nextLine();
				line = line.substring(line.indexOf(")") + 1, line.length());
				line = line.replaceAll("[^A-Za-z\\[\\] ] ", "");
				line = line.replaceAll("\\s+", " ");
				line = line.trim();
				int targetWordIndex = 0;
				String[] words = line.split(" ");
				for (int i = 0; i < words.length; i++) {
					if (words[i].startsWith("[")) {
						targetWordIndex = i;
					}
				}
				line = line.replaceAll("[^A-Za-z ]", "");
				words = line.split(" ");
				// System.out.println(words[targetWordIndex]);
				List<String> rootWords = new ArrayList<String>();
				
				rootWords = getRootWord(words[targetWordIndex]);
				ArrayList<String> ngramArray = new ArrayList<String>();
				ArrayList<String> rootWordNgramArray = new ArrayList<String>();
				accumulateNGrams(targetWordIndex, words, ngramArray);
				if (rootWords.size() == 1
						&& rootWords.get(0).equalsIgnoreCase(
								words[targetWordIndex])) {
				} else {
					for (int index = 0; index < rootWords.size(); index++) {
						words[targetWordIndex] = rootWords.get(index);
						accumulateNGrams(targetWordIndex, words,
								rootWordNgramArray);
					}
				}
				String perSentenceArray = ngramArray.get(0);
				for (int i = 1; i < (ngramArray.size()); i++) {
					perSentenceArray = perSentenceArray + "\n"
							+ ngramArray.get(i);
				}
				for (int i = 0; i < (rootWordNgramArray.size()); i++) {
					perSentenceArray = perSentenceArray + "\n"
							+ rootWordNgramArray.get(i);
				}
				try {
					Map<String, Object> params = new LinkedHashMap<>();
					params.put("p", perSentenceArray);
					StringBuilder postData = new StringBuilder();
					for (Map.Entry<String, Object> param : params.entrySet()) {
						if (postData.length() != 0)
							postData.append('&');
						postData.append(param.getKey());
						postData.append('=');
						postData.append(param.getValue());
					}
					byte[] postDataBytes = postData.toString()
							.getBytes("UTF-8");
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length",
							String.valueOf(postDataBytes.length));
					conn.setDoOutput(true);
					conn.getOutputStream().write(postDataBytes);
				} catch (IOException e) {
					e.printStackTrace();
					conn.disconnect();
				}
				DataInputStream input = null;
				String str = null;
				List<Double> probabilities = new ArrayList<Double>();
				List<Double> rootWordProbabilities = new ArrayList<Double>();
				try {
					input = new DataInputStream(conn.getInputStream());
					int counter = 0;
					while (null != ((str = input.readLine()))) {
						counter++;
						if (counter <= ngramArray.size())
							probabilities.add(Double.parseDouble(str));
						else
							rootWordProbabilities.add(Double.parseDouble(str));
					}
					input.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				if (ngramArray.get(0).equals(""))
					probabilities.set(0, 0.0);
				double sentenceProbability = 0;
				int jointprobabilityno = 0;
				int rootWordJointProbabilityNo = 0;
				int conditionalprobabilityno = 0;
				int rootWordConditionalProbabilityNo = 0;
				Double[] conditionalProbablities = new Double[20];
				Double[] rootWordConditionalProbabilities = new Double[20];
				calculateConditionalProbabilities(probabilities,
						jointprobabilityno, conditionalprobabilityno,
						conditionalProbablities);
				Double bigramprobabiltiessum = getProbabilities(18, 19,
						conditionalProbablities);
				Double trigramprobabiltiessum = getProbabilities(12, 17,
						conditionalProbablities);
				Double fourgramprobabiltiessum = getProbabilities(0, 11,
						conditionalProbablities);
				Double rootWordBigramProbabiltiesSum = 0.0;
				Double rootWordTrigramProbabiltiesSum = 0.0;
				Double rootWordFourgramProbabiltiesSum = 0.0;
				if (rootWordNgramArray.size() > 0) {
					calculateConditionalProbabilities(rootWordProbabilities,
							rootWordJointProbabilityNo,
							rootWordConditionalProbabilityNo,
							rootWordConditionalProbabilities);
					rootWordBigramProbabiltiesSum = +getProbabilities(18, 19,
							rootWordConditionalProbabilities);
					rootWordTrigramProbabiltiesSum = getProbabilities(12, 17,
							rootWordConditionalProbabilities);
					rootWordFourgramProbabiltiesSum = getProbabilities(0, 11,
							rootWordConditionalProbabilities);
				}

				if (fourgramprobabiltiessum > 0) {
					sentenceProbability = fourgramprobabiltiessum
							+ rootWordFourgramProbabiltiesSum;
				} else if (trigramprobabiltiessum > 0) {
					sentenceProbability = trigramprobabiltiessum
							+ rootWordTrigramProbabiltiesSum;
				} else {
					sentenceProbability = bigramprobabiltiessum
							+ rootWordBigramProbabiltiesSum;
				}
				if (maxSentenceProbability < sentenceProbability) {
					maxSentenceProbability = sentenceProbability;
					maxAnswerNumber = optionCount;
				}
				optionCount++;
			}
			testAnswerArray.add(answerInttoStringMap.get(maxAnswerNumber));
		}
		ArrayList<String> testActualAnswerArray = new ArrayList<String>();
		folder = new File("testing_data/Holmes.machine_format.answers_s.txt");
		if (folder.isFile()) {
			try {
				reader3 = new Scanner(folder);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			while (reader3.hasNextLine()) {
				String line = reader3.nextLine();
				line = line.substring(line.indexOf(")") - 1, line.indexOf(")"));
				testActualAnswerArray.add(line);
			}
		}
		// System.out.println("testanswers " + testAnswerArray.toString());
		// System.out.println("testActualanswers "
		// + testActualAnswerArray.toString());
		int testErrorCount = 0;
		for (int i = 0; i < (testActualAnswerArray.size()); i++) {
			if (!testAnswerArray.get(i).equalsIgnoreCase(
					testActualAnswerArray.get(i))) {
				testErrorCount += 1;
			}
		}
		System.out.println("Test Answer Prediction Accuracy: "
				+ (double) (testActualAnswerArray.size() - testErrorCount)
				* 100 / testActualAnswerArray.size() + "%");
	}

	private static void calculateConditionalProbabilities(
			List<Double> probabilities, int jointprobabilityno,
			int conditionalprobabilityno, Double[] conditionalProbablities) {
		for (int k = 4; k >= 1; k--) {
			for (int j = 1; j < (k + 1); j++) {
				for (int i = 0; i < (k - j); i++) {
					Double probability1 = (double) 0;
					Double probability2 = (double) 0;
					Double probability3 = (double) 0;
					Double probability4 = (double) 0;
					probability1 = probabilities.get(jointprobabilityno);
					jointprobabilityno++;
					probability2 = probabilities.get(jointprobabilityno);
					jointprobabilityno++;
					probability3 = probabilities.get(jointprobabilityno);
					jointprobabilityno++;
					probability4 = probabilities.get(jointprobabilityno);
					jointprobabilityno++;
					if (probability1 == 0 || probability2 == 0) {
						conditionalProbablities[conditionalprobabilityno] = 0.0;
					} else {
						conditionalProbablities[conditionalprobabilityno] = Math
								.pow(10.0, probability1)
								/ Math.pow(10.0, probability2);
					}
					conditionalprobabilityno++;
					if (probability3 == 0 || probability4 == 0) {
						conditionalProbablities[conditionalprobabilityno] = 0.0;
					} else {
						conditionalProbablities[conditionalprobabilityno] = Math
								.pow(10.0, probability3)
								/ Math.pow(10.0, probability4);
					}
					conditionalprobabilityno++;
				}
			}
		}
	}

	private static void accumulateNGrams(int targetWordIndex, String[] words,
			ArrayList<String> ngramArray) {
		for (int k = 4; k > 1; k--) {
			for (int j = 1; j < (k + 1); j++) {
				for (int i = 0; i < (k - j); i++) {
					String word = getNgram(targetWordIndex - j + 1,
							targetWordIndex + k - j + 1, words);
					ngramArray.add(word);
					word = getNgram(targetWordIndex + i + 1, targetWordIndex
							+ k - j + 1, words);
					ngramArray.add(word);
					word = getNgram(targetWordIndex + j - k, targetWordIndex
							+ j, words);
					ngramArray.add(word);
					word = getNgram(targetWordIndex + j - k, targetWordIndex
							- i, words);
					ngramArray.add(word);
				}
			}
		}
	}

	private static List<String> getRootWord(String word) throws IOException {
		String path = "C:/Program Files (x86)/WordNet/2.1/dict";
		URL url = new URL("file", null, path);
		IDictionary dictionary = new Dictionary(url);
		dictionary.open();
		WordnetStemmer wordnetStemmer = new WordnetStemmer(dictionary);
		System.out.println("Word is : "+ word);
		List<String> rootword= new ArrayList<String>();
		try{
		rootword = wordnetStemmer.findStems(word, null);
		}
		catch(Exception e){
		rootword.add(" ");
		}
		return rootword;
	}

	private static String getNgram(int start, int end, String[] words) {
		String ngram = new String();
		if (((start >= 0) && (end - 1) < (words.length))) {
			for (int i = start; i < (end); i++) {
				if (!ngram.equalsIgnoreCase(""))
					ngram += " " + words[i];
				else
					ngram += words[i];
			}
		}
		return ngram;
	}

	private static Double getProbabilities(int start, int end,
			Double[] probabilities) {
		Double sum = 0.0;
		for (int i = start; i <= (end); i++) {
			sum += probabilities[i];
		}
		return sum;
	}
}
