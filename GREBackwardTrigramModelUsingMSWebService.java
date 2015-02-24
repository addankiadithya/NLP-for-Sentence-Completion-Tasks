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

public class GREBackwardTrigramModelUsingMSWebService {
	private static Scanner reader2;
	private static Scanner reader3;

	public static void main(String[] args) throws MalformedURLException,
			ProtocolException {
		File folder = new File("Holmes_Training_Data");
		ArrayList<String> testAnswerArray = new ArrayList<String>();
		HashMap<Integer, String> answerInttoStringMap = new HashMap<Integer, String>();
		answerInttoStringMap.put(1, "a");
		answerInttoStringMap.put(2, "b");
		answerInttoStringMap.put(3, "c");
		answerInttoStringMap.put(4, "d");
		answerInttoStringMap.put(5, "e");
		folder = new File("testing_data/Holmes.machine_format.questions.txt");
		int optionCount = 1;
		int maxAnswerNumber = 0;
		double maxSentenceProbability = 0;
		if (folder.isFile()) {
			try {
				reader2 = new Scanner(folder);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			String s = "http://weblm.research.microsoft.com/weblm/rest.svc/bing-body/apr10/4/cp?u=0be45235-bfa5-4e20-88ae-c098e79547fb";
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
					if (maxAnswerNumber == 0)
						maxAnswerNumber = 1;
					testAnswerArray.add(answerInttoStringMap
							.get(maxAnswerNumber));
					optionCount = 1;
					maxSentenceProbability = 0;
			//		System.out.println(answerInttoStringMap
				//			.get(maxAnswerNumber));
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
				String backwardbigram = null;
				String backwardtrigram = null;
				String backwardfourgram = null;
				if ((targetWordIndex - 1) > 0) {
					backwardbigram = words[targetWordIndex - 1] + " "
							+ words[targetWordIndex];
				}
				if ((targetWordIndex - 2) > 0) {
					backwardtrigram = words[targetWordIndex - 2] + " "
							+ words[targetWordIndex - 1] + " "
							+ words[targetWordIndex];
				}
				if ((targetWordIndex - 3) > 0) {
					backwardfourgram = words[targetWordIndex - 3] + " "
							+ words[targetWordIndex - 2] + " "
							+ words[targetWordIndex - 1] + " "
							+ words[targetWordIndex];
				}
				String perSentenceArray = null;
				perSentenceArray = backwardbigram;
				perSentenceArray = perSentenceArray + "\n" + backwardtrigram;
				perSentenceArray = perSentenceArray + "\n" + backwardfourgram;
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
				try {
					input = new DataInputStream(conn.getInputStream());
					while (null != ((str = input.readLine()))) {
						probabilities.add(Double.parseDouble(str));
					}
					input.close();
				} catch (IOException ex) {

					ex.printStackTrace();
				}
				double sentenceProbability = 0;
				sentenceProbability = Math.pow(10, probabilities.get(1));
				if (maxSentenceProbability < sentenceProbability) {
					maxSentenceProbability = sentenceProbability;
					maxAnswerNumber = optionCount;
				}
				optionCount++;
			}
			testAnswerArray.add(answerInttoStringMap.get(maxAnswerNumber));
		}
		ArrayList<String> testActualAnswerArray = new ArrayList<String>();
		folder = new File("testing_data/Holmes.machine_format.answers.txt");
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
}
