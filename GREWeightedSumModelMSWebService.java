import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class GREWeightedSumModelMSWebService {
	private static Scanner reader;
	private static Scanner reader2;
	private static Scanner reader3;
	private static Scanner reader4;

	public static void main(String[] args) throws MalformedURLException,
			ProtocolException {

		File folder = new File("testing_data/Holmes.machine_format.questions.txt");
		HashMap<String, Integer> unigramWordCount = new HashMap<String, Integer>();
		HashMap<String, Integer> bigramTotalWordCount = new HashMap<String, Integer>();

		// System.out.println("in testing");

		ArrayList<String> testAnswerArray = new ArrayList<String>();
		HashMap<Integer, String> answerInttoStringMap = new HashMap<Integer, String>();
		answerInttoStringMap.put(1, "a");
		answerInttoStringMap.put(2, "b");
		answerInttoStringMap.put(3, "c");
		answerInttoStringMap.put(4, "d");
		answerInttoStringMap.put(5, "e");
		

		int optionCount = 1;
		// int answerNumber = 1;
		int maxAnswerNumber = 0;

		double maxSentenceProbability = 0;

		if (folder.isFile()) {

			try {
				reader2 = new Scanner(folder);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String s = "http://weblm.research.microsoft.com/weblm/rest.svc/bing-body/apr10/4/jp?u=0be45235-bfa5-4e20-88ae-c098e79547fb";
			URL url = new URL(s);
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// conn.setRequestProperty("Content-Length",
			// String.valueOf(postDataBytes.length));
			conn.setDoOutput(true);
			int lineno = 0;
			while (reader2.hasNextLine()) {
				if (optionCount == 6) {
					if (maxAnswerNumber == 0)
						maxAnswerNumber = 1;
					testAnswerArray.add(answerInttoStringMap
							.get(maxAnswerNumber));
					optionCount = 1;
					maxSentenceProbability = 0;
				//	System.out.println(answerInttoStringMap
					//		.get(maxAnswerNumber));
					maxAnswerNumber = 0;

				}
				//
				lineno++;
		//		System.out.println(lineno);
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

						targetWordIndex = i;
					}
				}
				line = line.replaceAll("[^A-Za-z ]", "");

				words = line.split(" ");
				// String previousWord = null;

				ArrayList<String> ngramArray = new ArrayList<String>();
				for (int k = 4; k > 1; k--) {
					for (int j = 1; j < (k + 1); j++) {
						for (int i = 0; i < (k - j); i++) {
							String word = getNgram(targetWordIndex - j + 1,
									targetWordIndex + k - j + 1, words);
							ngramArray.add(word);
							word = getNgram(targetWordIndex + i + 1,
									targetWordIndex + k - j + 1, words);
							ngramArray.add(word);
							word = getNgram(targetWordIndex + j - k,
									targetWordIndex + j, words);
							ngramArray.add(word);
							word = getNgram(targetWordIndex + j - k,
									targetWordIndex - i, words);
							ngramArray.add(word);

						}
					}
				}
				String perSentenceArray = ngramArray.get(0);
				for (int i = 1; i < (ngramArray.size()); i++) {
					perSentenceArray = perSentenceArray + "\n"
							+ ngramArray.get(i);
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
					//
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length",
							String.valueOf(postDataBytes.length));
					conn.setDoOutput(true);

					conn.getOutputStream().write(postDataBytes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					conn.disconnect();
				}

				DataInputStream input = null;
				String str = null;
				List<Double> probabilities = new ArrayList<Double>();
				try {
					input = new DataInputStream(conn.getInputStream());
					while (null != ((str = input.readLine()))) {
						probabilities.add(Double.parseDouble(str));
						// System.out.println(str);
					}
					input.close();
				} catch (IOException ex) {
					// Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
					// null, ex);
					ex.printStackTrace();
				}
				if (ngramArray.get(0).equals(""))
					probabilities.set(0, 0.0);
				// System.out.println(probabilities);
				double sentenceProbability = 0;

				int jointprobabilityno = 0;
				int conditionalprobabilityno = 0;
				Double[] conditionalProbablities = new Double[20];
				for (int k = 4; k >= 1; k--) {
					for (int j = 1; j < (k + 1); j++) {
						for (int i = 0; i < (k - j); i++) {
							Double probability1 = (double) 0;
							Double probability2 = (double) 0;
							Double probability3 = (double) 0;
							Double probability4 = (double) 0;

							probability1 = probabilities
									.get(jointprobabilityno);

							jointprobabilityno++;

							probability2 = probabilities
									.get(jointprobabilityno);

							jointprobabilityno++;

							probability3 = probabilities
									.get(jointprobabilityno);

							jointprobabilityno++;

							probability4 = probabilities
									.get(jointprobabilityno);

							jointprobabilityno++;
							// if(Math.pow(10.0, probability1)<((double)40/(60 *
							// Math.pow(10.0, 9.0))))
							// probability1=0.0;
							// if(Math.pow(10.0, probability2)<((double)40/(60 *
							// Math.pow(10.0, 9.0))))
							// probability2=0.0;
							// if(Math.pow(10.0, probability3)<((double)40/(60 *
							// Math.pow(10.0, 9.0))))
							// probability3=0.0;
							// if(Math.pow(10.0, probability4)<((double)40/(60 *
							// Math.pow(10.0, 9.0))))
							// probability4=0.0;
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

				Double bigramprobabiltiessum = getProbabilities(18, 19,
						conditionalProbablities);
				Double trigramprobabiltiessum = getProbabilities(12, 17,
						conditionalProbablities);
				Double fourgramprobabiltiessum = getProbabilities(0, 11,
						conditionalProbablities);

				sentenceProbability = 8 * fourgramprobabiltiessum + 3
						* trigramprobabiltiessum + bigramprobabiltiessum;

				if (maxSentenceProbability < sentenceProbability) {
					maxSentenceProbability = sentenceProbability;

					maxAnswerNumber = optionCount;
					// System.out.println(maxAnswerNumber);
				}
				// System.out.println(bigramprobabiltiessum+
				// " "+trigramprobabiltiessum+" "+ fourgramprobabiltiessum);
			//	System.out.println(sentenceProbability);

				// answerNumber++;
				optionCount++;
			}
			testAnswerArray.add(answerInttoStringMap.get(maxAnswerNumber));

		}

		ArrayList<String> testActualAnswerArray = new ArrayList<String>();
		// answer in testing
		folder = new File("testing_data/Holmes.machine_format.answers.txt");

		if (folder.isFile()) {

			try {
				reader3 = new Scanner(folder);
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
	/*	System.out.println("testanswers " + testAnswerArray.toString());
		System.out.println("testanswerssize " + testAnswerArray.size());
		System.out.println("testActualanswers "
				+ testActualAnswerArray.toString());
		System.out.println("testActualanswerssize "
				+ testActualAnswerArray.size());
	*/	int testErrorCount = 0;
		int testPresenceErrorCount = 0;
		for (int i = 0; i < (testActualAnswerArray.size()); i++) {
			if (!testAnswerArray.get(i).equalsIgnoreCase(
					testActualAnswerArray.get(i))) {
				// System.out.println("freq err no in neg"+ i);
				testErrorCount += 1;
			}
		}

		System.out.println("Test Answer Prediction Accuracy: "
				+ (double) (testActualAnswerArray.size() - testErrorCount)
				* 100 / testActualAnswerArray.size() + "%");

		//System.out.println("-----------------------");
		//System.out.println("");

	}

	private static String getNgram(int start, int end, String[] words) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		Double sum = 0.0;

		for (int i = start; i <= (end); i++) {
			sum += probabilities[i];
		}

		return sum;
	}
}
