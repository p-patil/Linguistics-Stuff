// package linguistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Arrays;
import java.io.IOException;
import java.util.HashSet;
import java.lang.Math;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;

/**
 * Tester class for WeightedTrie.
 */
public class TestWeightedTrie {
	static WeightedTrie storage; // Trie that will be used for testing.
	static String[] words; // Array of all the inputted words.
	static double[] weights; // Array containing corresponding weights to inputted words.
	static String filename; // Name of the inputted file of words.

	public static void main(String[] args) {
		// initialize(args[0]);
		// testInsert();
		// testFind();
		// testSort();
		// testAutocomplete();
		// testDistances();
		// timeDistances(500, 1, 15);
		// testSpellCheck();
		WeightedTrie test = new WeightedTrie("C:/Users/vip/Documents/Files/Default_dictionary.txt", 0.0);
		System.out.print("Converting... ");
		ArrayList<File> files = new ArrayList<File>(Arrays.asList((new File("linguistics/Test/WeightedTrie/words for days")).listFiles()));
		System.out.println("Done.");
		System.out.print("Updating weights... ");
		long start = System.nanoTime();
		test.updateWeights(files);
		long finish = System.nanoTime();
		System.out.println("Done.");
		// System.out.print("Writing to file... ");
		// test.toFile("linguistics/testing.txt");
		// System.out.println("Done.");
		System.out.println("Updating took " + (finish - start) * 0.000000001 + " seconds.");
		// System.out.println("Serializing... ");
		// test.serialize("words for days storage");
		// System.out.println("Done.");
		// System.out.println(test.autocomplete("the", 10));
		// System.out.println(test.autocomplete("the ", 10));
	}

	/**
	 * Initializes instance variables.
	 * @param file The name of the inputted word file.
	 */
	public static void initialize(String file) {
		filename = file;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			words = new String[Integer.parseInt(reader.readLine())];
			weights = new double[words.length];
			storage = new WeightedTrie();
			String line = reader.readLine();
			int i = 0;
			while (line != null) {
				String[] bufferedLine = line.split("\t");
				weights[i] = Double.parseDouble(bufferedLine[0]);
				words[i] = merge(Arrays.copyOfRange(bufferedLine, 1,  bufferedLine.length));
				i++;
				line = reader.readLine();
			}
		} catch (IOException e) {
			System.out.println("File not found");
		}
	}

	/**
	 * Tests WeightedTrie's ability to insert words.
	 */
	public static void testInsert() {
		long startTime = System.nanoTime();
		for (int i = 0; i < words.length; i++) {
			storage.insert(words[i], weights[i]);
		}
		for (int i = 0; i < 50; i++) {
			WeightedTrie.Node n = storage.find(words[(int) (Math.random() * words.length)]);
			if (n == null || !n.isWord()) {
				evaluate("Insertion", false, 0);
				return;
			}
		}
		evaluate("Insertion", true, (System.nanoTime() - startTime) * 0.000000001);
		return;
	}

	/**
	 * Tests WeightedTrie's find method. Assumes that the WeightedTrie has already been constructed.
	 */
	public static void testFind() {
		long startTime = System.nanoTime();
		HashSet<Integer> testWords = new HashSet<>();
		int num;
		if (words.length < 10) {
			num = words.length;
		} else {
			num = 10;
		}
		while (testWords.size() < num) {
			testWords.add((int) (Math.random() * words.length));
		}
		for (int i : testWords) {
			WeightedTrie.Node n = storage.find(words[i]);
			if (!n.word.equals(words[i])) {
				if (n.weight != weights[i]) {
					evaluate("Find", false, 0);
					return;
				}
			}
		}
		evaluate("Find", true, (System.nanoTime() - startTime) * 0.000000001);
		return;
	}

	/**
	 * Tests WeightedTrie's sort methods.
	 */
	public static void testSort() {
		long startTime = System.nanoTime();
		for (int i = 0; i < 25; i++) {
			String newAlphabet = shuffleString("abcdefghijklmnopqrstuvwxyz");
			for (String s : storage.sort(newAlphabet)) {
				if (s == null) System.out.println(s);
			}
			if (!isSorted(newAlphabet, storage.sort(newAlphabet))) {
				evaluate("Sort", false, 0);
				return;
			}
		}
		evaluate("Sort", true, (System.nanoTime() - startTime) * 0.000000001);
		return;
	}

	/**
	 * Tests WeightedTrie's topMatches method.
	 */
	public static void testAutocomplete() {
		long startTime = System.nanoTime();
		String[] test;
		HashSet<Integer> indices = new HashSet<>();
		int index = -1;
		if (words.length < 1000) {
			test = words;
		} else {
			test = new String[(int)(words.length * 0.1)];
			HashSet<String> temp = new HashSet<>();
			while (temp.size() < test.length) {
				temp.add(words[(int) (Math.random() * words.length)]);
			}
			int i = 0;
			for (String s : temp) {
				test[i] = s;
				i++;
			}
		}
		int num;
		double weightage;
		if (words.length < 1000) {
			num = 70;
			weightage = 0.2;
		} else {
			num = 10;
			weightage = 8.0/test.length;
		}
		for (int i = 0; i < num; i++) {
			index = (int) (Math.random() * test.length);
			int prefix = (int) (Math.random() * test[index].length() * 0.7) + 1;
			int n = (int) (Math.random() * test.length * weightage) + 1;
			ArrayList<String> arrList = storage.autocomplete(test[index].substring(0, prefix), n);
			String[] arr = arrList.toArray(new String[arrList.size()]);
			String[] compareArr = expectedTopMatches(test[index].substring(0, prefix));
			for (int k = 0; k < arr.length; k++) {
				double gotWeight = storage.find(arr[k]).weight;
				double expectedWeight = storage.find(compareArr[k]).weight;
				if (gotWeight != expectedWeight) {
					evaluate("Top Matches", false, 0);
					return;
				}
			}
		}
		evaluate("Top Matches", true, (System.nanoTime() - startTime) * 0.000000001);
		return;
	}

	/**
	 * Tests WeightedTrie's distance method.
	 */
	public static void testDistances() {
		long startTime = System.nanoTime();
		boolean pass1 = true;
		boolean pass2 = true;
		if (WeightedTrie.distance("book", "back") != 2) pass1 = false;
		if (WeightedTrie.distance("back", "book") != 2) pass1 = false;
		if (WeightedTrie.distance("metaphysics", "physical") != 6) pass1 = false;
		if (WeightedTrie.distance("", "dog") != 3) pass1 = false;
		if (WeightedTrie.distance("meow", "") != 4) pass1 = false;
		if (WeightedTrie.distance("dinosaur", "computer") != 7) pass1 = false;
		if (WeightedTrie.distance("mouse", "mozilla") != 5) pass1 = false;
		if (WeightedTrie.distance("newspaper", "newspaper") != 0) pass1 = false;
		if (WeightedTrie.distance("", "") != 0) pass1 = false;
		if (WeightedTrie.recursiveDistance("book", "back") != 2) pass2 = false;
		if (WeightedTrie.recursiveDistance("back", "book") != 2) pass2 = false;
		if (WeightedTrie.recursiveDistance("metaphysics", "physical") != 6) pass2 = false;
		if (WeightedTrie.recursiveDistance("", "dog") != 3) pass2 = false;
		if (WeightedTrie.recursiveDistance("meow", "") != 4) pass2 = false;
		if (WeightedTrie.recursiveDistance("dinosaur", "computer") != 7) pass2 = false;
		if (WeightedTrie.recursiveDistance("mouse", "mozilla") != 5) pass2 = false;
		if (WeightedTrie.recursiveDistance("newspaper", "newspaper") != 0) pass2 = false;
		if (WeightedTrie.recursiveDistance("", "") != 0) pass2 = false;
		evaluate("Array distance", true, (System.nanoTime() - startTime) * 0.000000001);
		evaluate("Recursive distance", pass1, (System.nanoTime() - startTime) * 0.000000001);
	}

	/**
	 * Tests weightedTrie's spellCheck method.
	 */
	public static void testSpellCheck() {
		long startTime = System.nanoTime();
		int num;
		if (words.length < 2000) {
			if (words.length < 100) {
				num = words.length;
			} else {
				num = 100;
			}			
		} else {
			num = (int) (0.05 * words.length);
		}
		for (int i = 0; i < num; i++) {
			int index = (int) (Math.random() * words.length);
			int distance = (int) (1 + Math.random() * 10);
			String[] gotArr = storage.spellCheck(words[index], distance, 25);
			String[] expectedArr = expectedSpellCheck(words[index], distance, 25);
			for (int j = 0; j < gotArr.length; j++) {
				if (!gotArr[j].equals(expectedArr[j])) {
					evaluate("Spell Check", false, 0);
					return;
				}
			}
		}
		evaluate("Spell Check", true, (System.nanoTime() - startTime) *  0.000000001);
	}

	/**
	 * Tests weightedTrie's updateWeights method.
	 */
	public static void testUpdateWeights() {
		long startTime = System.nanoTime();
		WeightedTrie test = new WeightedTrie();
		ArrayList<File> files = new ArrayList<>();
		files.add(new File("Test/WeightedTrie/CatsParagraph.txt"));
		files.add(new File("Test/WeightedTrie/Harry-Potter-Prisoner-Azkaban.txt"));
		HashMap<String, Double> updatedWeights = test.updateWeights(files);
		HashMap<String, Double> expected = expectedWeights(files);
		if (updatedWeights.size() != expected.size()) {
			evaluate("Update Weights", false, 0);
			return;
		}
		for (String word : updatedWeights.keySet()) {
			double x = test.find(word).weight;
			double y = expected.get(word);
			if (x != y) {
				System.out.println(word + ": " + x + ", " + y);
				evaluate("Update Weights", false, 0);
				return;
			}
		}
		evaluate("Update Weights", true, (System.nanoTime() - startTime) *  0.000000001);
	}

	/**
	 * HELPER METHODS BELOW THIS LINE.
	 */

	/**
	 * Helper method.
	 * Used for printing the evaluation of a test.
	 * @param testName The name of the test being run.
	 * @param pass Whether or not the test passed.
	 * @param time How long it took to run that method.
	 */
	public static void evaluate(String testName, boolean pass, double time) {
		if (pass) {
			System.out.print("PASSED: " +  testName + " test on " + filename + ".txt, ");
			System.out.println(time + " seconds elapsed.");
		} else {
			System.out.println("FAILED: " + testName + " test on " + filename + ".txt");
		}
	}

	/**
	 * Helper method.
	 * Tests if a list of words is sorted according to a particular ordering.
	 * @param alphabet The ordering against which to test for sortedness. This String cannot have duplicates.
	 * @param words The list of words to check.
	 * @return Whether or not the list is sorted.
	 */
	public static boolean isSorted(String alphabet, ArrayList<String> words) {
		for (int i = 0; i < words.size() - 1; i++) {
			for (int j = 0; j < words.get(i).length(); j++) {
				if (j == words.get(i + 1).length()) {
					return false;
				}
				int first =  alphabet.indexOf(Character.toLowerCase(words.get(i).charAt(j)));
				int second = alphabet.indexOf(Character.toLowerCase(words.get(i + 1).charAt(j)));
				if (first < second) {
					break;
				} else if (first > second) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Helper method.
	 * Randomly shuffles the characters in a String around.
	 * @param s The String to be shuffled.
	 * @return The shuffled String.
	 */
	public static String shuffleString(String s) {
		char[] arr = s.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			int random = i + (int) (Math.random() * (arr.length - i));
			char randomElement = arr[random];
			arr[random] = arr[i];
			arr[i] = randomElement;
		}
		return new String(arr);
	}

	/**
	 * Helper method.
	  * Merges all the Strings in an array together, buffering them with spaces.
	 * @param arr The array of Strings to be merged.
	 * @return The String representation of the array.
	 */
	public static String merge(String[] arr) {
		String ret = "";
		for (String s : arr) {
			ret += s;
			ret += " ";
		}
		return ret.substring(0, ret.length() - 1);
	}

	/**
	 * Compares times for the two distance methods in WeightedTrie.
	 * @param n The number of trials.
	 * @param minLength The minimum length a word used for testing can be.
	 * @param maxLength The maximum length a word used for testing can be.
	 */
	public static void timeDistances(int n, int minLength, int maxLength) {
		int average1 = 0;
		int average2 = 0;
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < n; i++) {
			String s = "";
			for (int j = 0; j < (int) (minLength + (Math.random() * (maxLength - minLength))); j++) {
				s += alphabet.charAt((int) (Math.random() * alphabet.length()));
			}
			String t = "";
			for (int j = 0; j < (int) (minLength + (Math.random() * (maxLength - minLength))); j++) {
				t += alphabet.charAt((int) (Math.random() * alphabet.length()));
			}
			long startTime = System.nanoTime();
			WeightedTrie.distance(s, t);
			average1 += System.nanoTime() - startTime;
			startTime = System.nanoTime();
			WeightedTrie.recursiveDistance(s, t);
			average2 += System.nanoTime() - startTime;
		}
		System.out.print("Array distance: " + average1/n * 0.000000001 + " seconds, ");
		System.out.println("Recursive distance: " + average2/n * 0.000000001 + " seconds");
	}

	/**
	 * Brute-force computes the top matches for a given prefix, used for testing WeightedTrie's topMatches method.
	 * @param prefix The prefix to test.
	 * @return An array of Strings containing the words, in order of weight, that match the prefix.
	 */
	public static String[] expectedTopMatches(String prefix) {
		ArrayList<String> compareArrList = storage.find(prefix).toArrayList();
		TreeSet<String> temp = new TreeSet<>(storage.getStringComparator());
		for (String s : compareArrList) {
			temp.add(s);
		}
		String[] compareArr = new String[temp.size()];
		int i = 0;
		while (!temp.isEmpty()) {
			compareArr[i] = temp.pollFirst();
			i++;
		}
		return compareArr;
	}

	/**
	 * Brute-force computes the n highest weighted words in storage that have an edit distance with word that is smaller than distance.
	 * @param word The word to spell check.
	 * @param distance The maximum edit distance.
	 * @param n The number of matches to return.
	 * @return An array of the top n words that match the above criterion.
	 */
	public static String[] expectedSpellCheck(String word, int distance, int n) {
		WeightedTrie.Node wordNode = storage.find(word);
		if (wordNode != null && wordNode.isWord()) {
			return new String[0];
		}
		TreeSet<String> expectedWords = new TreeSet<>(storage.getStringComparator());
		for (String s : words) {
			if (WeightedTrie.distance(s, word) <= distance) {
				expectedWords.add(s);
			}
		}
		String[] ret = new String[Math.min(expectedWords.size(), n)];
		int i = 0;
		while (!expectedWords.isEmpty() && i < n) {
			ret[i] = expectedWords.pollFirst();
			i++;
		}
		return ret;
	}

	/**
	 * Brute force computes the frequency of every single word in every text file in the inputted list of files. Used for testing updateWeights.
	 * @param files The list of files to read.
	 * @return A Map from words to their overall frequencies in the text files.
	 */
	public static HashMap<String, Double> expectedWeights(ArrayList<File> files) {
		HashMap<String, Double> expected = new HashMap<>();
		try {
			for (File f : files) {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String text = "";
				String line = reader.readLine();
				while (line != null) {
					text += line + "\n";
					line = reader.readLine();
				}
				for (String word : text.split("[^a-zA-Z0-9']+")) {
					word = word.toLowerCase();
					if (expected.containsKey(word)) {
						expected.put(word, expected.get(word) + 1);
					} else {
						expected.put(word, 1.0);
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			return expected;
		}
	}

	public static double timeUpdateWeights(int n) {
		ArrayList<File> test = new ArrayList<>();
		test.add(new File("Test/WeightedTrie/Harry-Potter-Prisoner-Azkaban.txt"));
		double total = 0.0;
		for (int i = 0; i < n; i++) {
			WeightedTrie temp = new WeightedTrie();
			long startTime = System.nanoTime();
			temp.updateWeights(test);
			long finishTime = System.nanoTime();
			double time = (finishTime - startTime) * 0.000000001;
			System.out.println(time);
			total += time;
		}
		return total / n;
	}
}