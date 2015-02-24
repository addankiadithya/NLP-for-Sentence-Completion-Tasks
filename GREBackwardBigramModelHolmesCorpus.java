import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class GREBackwardBigramModelHolmesCorpus {
	private static Scanner reader;
	private static Scanner reader2;
	private static Scanner reader3;
	private static Scanner reader4;

	public static void main(String[] args) throws FileNotFoundException {

		File folder = new File("Holmes_Training_Data");
		File[] listOfFiles = folder.listFiles();
		HashMap<String, Integer> unigramWordCount = new HashMap<String, Integer>();
		HashMap<String, Integer> bigramTotalWordCount = new HashMap<String, Integer>();
		for (File file : listOfFiles) {
			HashMap<String, Integer> documentWordCount = new HashMap<String, Integer>();
			if (file.isFile()) {
				reader4 = new Scanner(file);

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
		}
		int totalWordCount = 0;
		Iterator<Map.Entry<String, Integer>> iter = bigramTotalWordCount
				.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			if (entry.getValue() < 20) {
				iter.remove();
			}
		}

		for (Integer bigramCount : bigramTotalWordCount.values()) {
			totalWordCount += bigramCount;
		}

		Integer uniqueWordCount = bigramTotalWordCount.size();
		//System.out.println(totalWordCount);
		//System.out.println("in testing");
		ArrayList<String> testAnswerArray = new ArrayList<String>();
		HashMap<Integer, String> answerInttoStringMap = new HashMap<Integer, String>();
		answerInttoStringMap.put(1, "a");
		answerInttoStringMap.put(2, "b");
		answerInttoStringMap.put(3, "c");
		answerInttoStringMap.put(4, "d");
		answerInttoStringMap.put(5, "e");
		folder = new File("testing_data/Holmes.machine_format.questions_s.txt");

		if (folder.isFile()) {

			reader2 = new Scanner(folder);
			int answerNumber = 1;
			int maxAnswerNumber = 0;
			double maxSentenceProbability = 0;
			while (reader2.hasNextLine()) {

				if (answerNumber == 6) {
					testAnswerArray.add(answerInttoStringMap
							.get(maxAnswerNumber));
					answerNumber = 1;
					maxSentenceProbability = 0;
					maxAnswerNumber = 0;

				}
				String line = reader2.nextLine();
				line = line.substring(line.indexOf(")") + 1, line.length());

				line = line.replaceAll("[^A-Za-z ]", " ");
				line = line.replaceAll("\\s+", " ");
				line = line.trim();
				String[] words = line.split(" ");
				// reader4.useDelimiter("[^A-Za-z]+");
				double sentenceProbability = 1;

				String previousWord = null;

				for (String word : words) {

					if (previousWord != null) {
						String bigram = previousWord + " " + word;
						double bigramProbability = 0;
						if (unigramWordCount.containsKey(previousWord)) {
							if (bigramTotalWordCount.containsKey(bigram)) {
								bigramProbability = (double) (bigramTotalWordCount
										.get(bigram) + 1)
										/ (unigramWordCount.get(previousWord)
										+ uniqueWordCount);
							} else {
								bigramProbability = (double) 1
										/ (unigramWordCount.get(previousWord)
										+ uniqueWordCount);

							}
						} else {
							if (bigramTotalWordCount.containsKey(bigram)) {
								bigramProbability = (double) (bigramTotalWordCount
										.get(bigram) + 1) / uniqueWordCount;
							} else {
								bigramProbability = (double) 1
										/ uniqueWordCount;

							}
						}
						sentenceProbability *= bigramProbability;

					}
					previousWord = word;

				}
				if (maxSentenceProbability < sentenceProbability) {
					maxSentenceProbability = sentenceProbability;

					maxAnswerNumber = answerNumber;

				}
				//System.out.println(sentenceProbability);

				answerNumber++;
			}
			testAnswerArray.add(answerInttoStringMap.get(maxAnswerNumber));
		}

		ArrayList<String> testActualAnswerArray = new ArrayList<String>();
		// answer in testing
		folder = new File("testing_data/Holmes.machine_format.answers_s.txt");

		if (folder.isFile()) {

			reader3 = new Scanner(folder);
			int answerNumber = 1;
			while (reader3.hasNextLine()) {
				double maxSentenceProbability = 0;

				String line = reader3.nextLine();
				line = line.substring(line.indexOf(")") - 1, line.indexOf(")"));
				testActualAnswerArray.add(line);

			}
		}

		// code for counting errors in classification
//		System.out.println("testanswers " + testAnswerArray.toString());
//		System.out.println("testanswerssize " + testAnswerArray.size());
//		System.out.println("testActualanswers "
//				+ testActualAnswerArray.toString());
//		System.out.println("testActualanswerssize "
//				+ testActualAnswerArray.size());
		int testErrorCount = 0;
		int testPresenceErrorCount = 0;
		for (int i = 0; i < testActualAnswerArray.size(); i++) {
			if (!testAnswerArray.get(i).equalsIgnoreCase(
					testActualAnswerArray.get(i))) {

				testErrorCount += 1;
			}
		}

		System.out.println("Test Answer Prediction Accuracy: "
				+ (double) (testActualAnswerArray.size() - testErrorCount)
				* 100 / testActualAnswerArray.size() + "%");

		//System.out.println("-----------------------");
		//System.out.println("");

	}
}
