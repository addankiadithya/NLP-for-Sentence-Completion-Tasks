import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class GREForwardBackwardBigramModelHolmesCorpus {
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
					// reader4.useDelimiter("[^A-Za-z]+");
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
					// bigramArrayHashMaps.add(documentWordCount);

				}
			}
		}
		int totalWordCount = 0;
		for (Integer bigramCount : bigramTotalWordCount.values()) {
			totalWordCount += bigramCount;
		}

		Integer uniqueWordCount = unigramWordCount.size();
		// System.out.println(totalWordCount);
		// System.out.println("in testing");
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
				// line = line.replaceAll("\\s+", " ");
				// reader4.useDelimiter("[^A-Za-z]+");
				double sentenceProbability = 1;
				words = line.split(" ");
				// String previousWord = null;

				String backwardbigram = null;
				if (targetWordIndex > 0) {
					backwardbigram = words[targetWordIndex - 1] + " "
							+ words[targetWordIndex];
				}
				String forwardbigram = null;
				if (targetWordIndex < words.length - 1) {
					forwardbigram = words[targetWordIndex] + " "
							+ words[targetWordIndex + 1];
				}

				double backwardbigramProbability = 0;
				double forwardbigramProbability = 0;
				if (targetWordIndex > 0 && words[targetWordIndex - 1] != null) {
					if (unigramWordCount
							.containsKey(words[targetWordIndex - 1])
							&& backwardbigram != null) {
						if (bigramTotalWordCount.containsKey(backwardbigram)) {
							backwardbigramProbability = (double) (bigramTotalWordCount
									.get(backwardbigram) + 1)
									/ (unigramWordCount
											.get(words[targetWordIndex - 1]) + uniqueWordCount);
						} else {
							backwardbigramProbability = (double) 1
									/ (unigramWordCount
											.get(words[targetWordIndex - 1]) + uniqueWordCount);

						}
					} else {
						if (bigramTotalWordCount.containsKey(backwardbigram)) {
							backwardbigramProbability = (double) (bigramTotalWordCount
									.get(backwardbigram) + 1) / uniqueWordCount;
						} else {
							backwardbigramProbability = (double) 1
									/ uniqueWordCount;

						}
					}
				}

				if (targetWordIndex < words.length - 1
						&& unigramWordCount
								.containsKey(words[targetWordIndex + 1])
						&& forwardbigram != null) {
					if (bigramTotalWordCount.containsKey(forwardbigram)) {
						forwardbigramProbability = (double) (bigramTotalWordCount
								.get(forwardbigram) + 1)
								/ (unigramWordCount
										.get(words[targetWordIndex + 1]) + uniqueWordCount);
					} else {
						forwardbigramProbability = (double) 1
								/ (unigramWordCount
										.get(words[targetWordIndex + 1]) + uniqueWordCount);

					}
				} else {
					if (bigramTotalWordCount.containsKey(forwardbigram)) {
						forwardbigramProbability = (double) (bigramTotalWordCount
								.get(forwardbigram) + 1) / uniqueWordCount;
					} else {
						forwardbigramProbability = (double) 1 / uniqueWordCount;

					}
				}
				sentenceProbability = (backwardbigramProbability + forwardbigramProbability);

				if (maxSentenceProbability < sentenceProbability) {
					maxSentenceProbability = sentenceProbability;

					maxAnswerNumber = answerNumber;
					// System.out.println(maxAnswerNumber);
				}
				// System.out.println(sentenceProbability);

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
		// System.out.println("testanswers " + testAnswerArray.toString());
		// System.out.println("testanswerssize " + testAnswerArray.size());
		// System.out.println("testActualanswers "
		// + testActualAnswerArray.toString());
		// System.out.println("testActualanswerssize "
		// + testActualAnswerArray.size());
		int testErrorCount = 0;
		int testPresenceErrorCount = 0;
		for (int i = 0; i < testActualAnswerArray.size(); i++) {
			if (!testAnswerArray.get(i).equalsIgnoreCase(
					testActualAnswerArray.get(i))) {
				// System.out.println("freq err no in neg"+ i);
				testErrorCount += 1;
			}
		}

		// System.out.println("----Trial " + ((fold / 200) + 1)
		// + " results----");
		// System.out.println("Test Frequency Errors: " + testErrorCount);
		System.out.println("Test Answer Prediction Accuracy: "
				+ (double) (testActualAnswerArray.size() - testErrorCount)
				* 100 / testActualAnswerArray.size() + "%");

		// System.out.println("-----------------------");
		// System.out.println("");
		// }
		// System.out.println("Average Frequency Accuracy: " + sumfreq / 5 +
		// "%");
		// System.out.println("Average Presence Accuracy: " + sumpresence / 5
		// + "%");

	}
}
