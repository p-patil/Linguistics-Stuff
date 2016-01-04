import java.util.HashMap;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Calendar;
import java.lang.Math;
import java.lang.reflect.Method;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;


/**
 * Implements a Trie for storing Strings, and gives each stored word a weight.
 */ 
public class WeightedTrie implements Serializable {
	Node root; // The very top of the WeightedTrie.
	Node bigrams; // Another trie structure used for storing word digrams; useful for word prediction.

	/**
	 * Basic constructor.
	 */
	public WeightedTrie() {
		root = new Node();
	}

	/**
	 * Constructor. Creates a new WeightedTrie containing the inputted words with corresponding weights.
	 * @param words The list of words that the constructed WeightedTrie should contain.
	 * @param weights The corresponding weights for the inputted words.
	 */
	public WeightedTrie(String[] words, double[] weights) {
		if (words.length != weights.length) {
			throw new IllegalArgumentException("Word list and weight list must have same length.");
		}
		root = new Node();
		for (int i = 0; i < words.length; i++) {
			insert(words[i], weights[i]);
		}
	}

	/**
	 * Constructor. Creates a new WeightedTrie containing the inputted words with corresponding weights, in Map form.
	 * @param wordWeights A HashMap mapping words to their weights.
	 */
	public WeightedTrie(HashMap<String, Double> wordWeights) {
		root = new Node();
		for (String word : wordWeights.keySet()) {
			insert(word, wordWeights.get(word));
		}
	}

	/**
	 * Constructor. Builds a WeightedTrie from a text file of Strings, one word per line (ie separated by new lines) with weights on the same line, separated
	 * from the word by a tab. The weights should come first.
	 * @param filename The path to the text file to use.
	 */
	public WeightedTrie(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			int size = Integer.parseInt(reader.readLine());
			String[] words = new String[size];
			double[] weights = new double[size];
			String line = reader.readLine();
			int i = 0;
			while (line != null) {
				weights[i] = Double.parseDouble(line.split("\t")[0]);
				words[i] = line.split("\t")[1];
				i++;
				line = reader.readLine();	
			}
			reader.close();
			root = new Node();
			for (int j = 0; j < words.length; j++) {
				insert(words[j], weights[j]);
			}
		} catch (IOException e) {
			System.out.println("Cannot find file: " + filename);
		}
	}

	/**
	 * Initializes a WeightedTrie with all the words in an inputted dictionary, all with the inputted initial weights. The first line of the file should be
	 * the number of words in the file.
	 * @param filepath The path to the dictionary file.
	 * @param initialWeight The weight to initially set all words to.
	 */
	public WeightedTrie(String filepath, double initialWeight) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
			root = new Node();
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				insert(line.split(" ")[0], initialWeight);
			}
		} catch(IOException e) {
			System.out.println("Cannot find file: " + filepath);
		}
	}

	/**
	 * Takes in a list of (text) files and uses them to update the weights in this WeightedTrie. The algorithm used to update weights is specified in the
	 * weightUpdateFunction method.
	 * @param files The list of files to read.
	 * @return A Map from each word (along with any new words) to its total frequency in all files inputted.
	 */
	public HashMap<String, Double> updateWeights(ArrayList<File> files) {
		bigrams = new Node();
		HashMap<String, Double> frequencies = new HashMap<>();
		try {
			double total = 0;
			long count = 0;
			for (File f : files) {
				if (!f.getAbsolutePath().substring(f.getAbsolutePath().length() - 4).equals(".txt")) {
					throw new IllegalArgumentException("Only text files can be spell checked; file " + f + " does not end with \".txt\" (first instance found)");
				}
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line;
				String last = initializeLast(reader);
				while ((line = reader.readLine()) != null) {
					for (String word : line.split("[^a-zA-Z0-9']+")) {
						if (!word.isEmpty()) {
							Node wordNode = find(word);
							if (wordNode == null) {
								insert(word, 1);
								frequencies.put(word.toLowerCase(), 1.0);
							} else {
								wordNode.weight = weightUpdateFunction(wordNode.word, wordNode.weight);
								frequencies.put(word.toLowerCase(), wordNode.weight);
							}
							Node bigramNode = bigrams.find((last + " " + word).toLowerCase());
							if (bigramNode == null) {
								bigrams.insert(last + " " + word, (last + " " + word).toLowerCase(), 1.0, last.charAt(0));
							} else {
								bigramNode.weight = weightUpdateFunction(bigramNode.word, bigramNode.weight);
							}
							last = word;
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		} finally {
			return frequencies;
		}
	}

	/**
	 * The function used in the updateWeights method to update the old weight of a given String. Depending on what the Strings in this WeightedTrie represent,
	 * different update mechanisms may be desired. For example, if the Strings represent simple words in modern English, a reasonable update algorithm to use
	 * would be to always map a word to its frequency of use, which is what the below function does; however, if the Strings represented cities or countries,
	 * for example, the weights might be the populations, in which case simply incrementing a weight by 1 would not be a good update mechanism.
	 * @param word The word whose weight is being updated.
	 * @param weight The old weight to update.
	 * @return The new weight.
	 */
	private double weightUpdateFunction(String word, double weight) {
		return weight + 1;
	}

	/**
	* Inserts a String into this WeightedTrie. If the word already exists in the WeightedTrie, updates the weight of the word to the inputted weight.
	* @param word The String to be inserted.
	* @param wordWeight The weight of the inserted word.
	*/
	public void insert(String word, double weight) {
		if (word == null) {
            throw new IllegalArgumentException("Cannot insert a null string.");
        } else if (word.length() == 0) {
            throw new IllegalArgumentException("Cannot insert an empty string.");
        }
		root.insert(word, word.toLowerCase(), weight, word.charAt(0));
	}

	/**
	 * Deletes a word from this WeightedTrie. Returns whether or not anything was deleted (ie whether or not the inputted word was in the WeightedTrie).
	 * @param word The String to delete.
	 * @return Whether or not word was in the WeightedTrie.
	 */
	public boolean delete(String word) {
		if (find(word) == null) {
			return false;
		}
		Node currNode = root;
		int i = 0;
		while (i < word.length()) {
			if (currNode.links.get(word.charAt(i)).links.size() == 1) {
				currNode.links.remove(word.charAt(i));
				return true;
			}
			currNode = currNode.links.get(word.charAt(i));
			i++;
		}
		currNode.word = null;
		return true;
	}

	/**
	 * Returns the Node that represents the inputted word in the WeightedTrie, ie the Node whose character is the last letter of the inputted word.
	 * @param word The word to search for.
	 * @return The Node that contains the last letter of word, or null if word is not in the WeightedTrie.
	 */
	public Node find(String word) {
		if (word == null) {
            throw new IllegalArgumentException("Cannot search for a null string.");
        } else if (word.isEmpty()) {
            throw new IllegalArgumentException("Cannot search for an empty string.");
        }
        return root.find(word.toLowerCase());
	}


	// MODIFY THIS TO ACCOUNT FOR WHITESPACE ERRORS (eg "thetable" -> "the table") AND TRANSPOSITION ERRORS (eg "freind" -> "friend" has edit distance 1).
	// MODIFY RANKING MECHANISM TO ACCOUNT FOR BIGRAM PROBABILITY, IN ADDITION TO WEIGHT (which represents word frequency).
	/**
	* Returns the highest weighted matches within k edit distance of the word.
	* If the word is in the dictionary, then return an empty list.
	* @param word The word to spell-check
	* @param dist Maximum edit distance to search
	* @param n Number of results to return 
	* @return List of Strings in descending weight order of the matches
	*/
	public String[] spellCheck(String word, int distance, int n) {
		word = word.toLowerCase();
		WeightedTrie.Node wordNode = find(word);
		if (wordNode != null && wordNode.isWord()) {
			return new String[0];
		}
		int[] firstRow = new int[word.length() + 1];
		for (int i = 0; i < firstRow.length; i++) {
			firstRow[i] = i;
		}
		TreeSet<String> results = new TreeSet<>(new StringComparator());
		for (char c : root.links.keySet()) {
			spellCheckHelper(word, distance, firstRow, root.links.get(c), results);
		}
		String[] ret = new String[Math.min(results.size(), n)];
		int i = 0;
		while (!results.isEmpty() && i < n) {
			ret[i] = results.pollFirst();
			i++;
		}
		return ret;
	}

	/**
	 * Spell checks the inputted text file, and returns a map that maps misspelled words to a list of possible corrections (that maxes out at length 10).
	 * @param filepath The path to the text file to be spell checked.
	 * @return A mapping that maps misspelled words in the file to a list (of maximum length 10) of corrections.
	 */
	public HashMap<String, ArrayList<String>> spellCheck(String filepath) {
		HashMap<String, ArrayList<String>> ret = new HashMap<>();
		try {
			String name = (new File(filepath)).getAbsolutePath();
			if (!name.substring(name.length() - 4).equals(".txt")) {
				throw new IllegalArgumentException("Only text files can be spell checked; file path must end with \".txt\"");
			}
			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
			String text = "";
			String line = reader.readLine();
			while (line != null) {
				text += line + "\n";
				line = reader.readLine();
			}
			String[] words = text.split("[^a-zA-Z0-9']+");
			for (String word : words) {
				Node n = find(word);
				if ((n != null && n.isWord()) || ret.containsKey(word)) {
					continue;
				}
				int distance = 1;
				ArrayList<String> corrections = new ArrayList<>();
				while (corrections.size() < 10) {
					corrections.addAll(Arrays.asList(spellCheck(word, distance, 10)));
					distance++;
				}
				while (corrections.size() > 10) {
					corrections.remove(10);
				}
				ret.put(word, corrections);
			}
		} catch (IOException e) {
			System.out.println("Cannot find file: " + filepath);
		} finally {
			return ret;
		}
	}

	/**
	 * Autocompletes the incomplete String by returning the n highest weighted Strings with the given prefix. If there are less matches than n, all matches
	 * are returned. Runs on a modified version of Dijkstra's algorithm.
	 * @param prefix The initial portion of a String to be autocompleted.
	 * @param n The number of suggestions
	 * @return An array of Strings containing the top n matches, by weight.
	 */
	public ArrayList<String> autocomplete(String prefix, int n) {
		String pointer = prefix.toLowerCase();
		Node currNode = root;
		while (pointer.length() > 0) {
			currNode = currNode.links.get(pointer.charAt(0));
			if (currNode == null) {
				return null;
			}
			pointer = pointer.substring(1);
		}
		ArrayList<String> matches = new ArrayList<>();
		TreeSet<String> extras = new TreeSet<>(new StringComparator());
		PriorityQueue<Node> pq = new PriorityQueue<>(currNode.size, new NodeComparator());
		pq.add(currNode);
		while (!pq.isEmpty() && matches.size() < n) {
			currNode = pq.poll();
			if (currNode.links.isEmpty()) {
				matches.add(currNode.word);
			} else {
				if (currNode.isWord()) {
					extras.add(currNode.word);
				}
				for (char c : currNode.links.keySet()) {
					pq.add(currNode.links.get(c));
				}
			}
		}
		ArrayList<String> ret = new ArrayList<>();
		while (ret.size() < n) {
			if (!(matches.isEmpty() || extras.isEmpty())) {
				double first = find(matches.get(0)).weight;
				double second = find(extras.first()).weight;
				if (first > second) {
					ret.add(matches.get(0));
					matches.remove(0);
				} else if (first < second) {
					ret.add(extras.pollFirst());
				} else {
					ret.add(extras.pollFirst());
					ret.add(matches.get(0));
					matches.remove(0);
				}
			} else {
				if (!matches.isEmpty()) {
					ret.add(matches.get(0));
					matches.remove(0);
				} else if (!extras.isEmpty()) {
					ret.add(extras.pollFirst());
				}
				else {
					break;
				}
			}
		}
		return ret;
	}

	// INCOMPLETE: MODIFY TO ACCOUNT FOR GRAMMAR, SO ONLY HIGHLY WEIGHTED WORDS THAT ALSO MAKE GRAMMATICAL SENSE (given the input word) ARE RETURNED IN THE CASE
	// THAT bigrams HAS NOT BEEN INITIALIZED.
	/**
	 * Given an input word, returns the n most likely words to come after the inputted word based on bigram probabilities.
	 * @param word The first word in the bigram; the second is being predicted in this method.
	 * @param n The number of results to return. If this number is larger than the total number of results returned, all results are returned.
	 * @return A list of predicted words.
	 */
	// public ArrayList<String> predictWord(String word, int n) {
	// 	if (bigrams == null) {

	// 	} else {
	// 		String pointer = prefix.toLowerCase();
	// 		Node currNode = bigrams;
	// 		while (pointer.length() > 0) {
	// 			currNode = currNode.links.get(pointer.charAt(0));
	// 			if (currNode == null) {
	// 				return null;
	// 			}
	// 			pointer = pointer.substring(1);
	// 		}
	// 		ArrayList<String> matches = new ArrayList<>();
	// 		TreeSet<String> extras = new TreeSet<>(new StringComparator());
	// 		PriorityQueue<Node> pq = new PriorityQueue<>(currNode.size, new NodeComparator());
	// 		pq.add(currNode);
	// 		while (!pq.isEmpty() && matches.size() < n) {
	// 			currNode = pq.poll();
	// 			if (currNode.links.isEmpty()) {
	// 				matches.add(currNode.word);
	// 			} else {
	// 				if (currNode.isWord()) {
	// 					extras.add(currNode.word);
	// 				}
	// 				for (char c : currNode.links.keySet()) {
	// 					pq.add(currNode.links.get(c));
	// 				}
	// 			}
	// 		}
	// 		ArrayList<String> ret = new ArrayList<>();
	// 		while (ret.size() < n) {
	// 			if (!(matches.isEmpty() || extras.isEmpty())) {
	// 				double first = find(matches.get(0)).weight;
	// 				double second = find(extras.first()).weight;
	// 				if (first > second) {
	// 					ret.add(matches.get(0));
	// 					matches.remove(0);
	// 				} else if (first < second) {
	// 					ret.add(extras.pollFirst());
	// 				} else {
	// 					ret.add(extras.pollFirst());
	// 					ret.add(matches.get(0));
	// 					matches.remove(0);
	// 				}
	// 			} else {
	// 				if (!matches.isEmpty()) {
	// 					ret.add(matches.get(0));
	// 					matches.remove(0);
	// 				} else if (!extras.isEmpty()) {
	// 					ret.add(extras.pollFirst());
	// 				}
	// 				else {
	// 					break;
	// 				}
	// 			}
	// 		}
	// 		return ret;
	// 	}
	// }


	/**
	* Sorts the Strings in this WeightedTrie. The order of Strings is the basic English alphabet.
	* @return An array of Strings containing all the Strings in this WeightedTrie in sorted order.
	*/
	public ArrayList<String> sort() {
		return sort("abcdefghijklmnopqrstuvwxyz");
	}

	/**
	 * Sorts the Strings in this WeightedTrie according to a particular sorting alphabet.
	 * @param alphabet The alphabet to follow when sorting.
	 * @return An array of Strings containing all the words in this WeightdTrie, sorted according to the alphabet.
	 */
	public ArrayList<String> sort(String alphabet) {
		String[] charArray = new String[alphabet.length()];
		for (int i = 0; i < alphabet.length(); i++) {
			charArray[i] = alphabet.substring(i, i + 1);
		}
		if (duplicates(charArray)) {
			throw new IllegalArgumentException("Inputted alphabet cannot contain duplicates.");
		}
		return sortHelper(alphabet, root, new ArrayList<String>());
	}


	/**
	 * Returns the number of Strings in this WeightedTrie.
	 * @return The number of Strings.
	 */
	public int size() {
		return root.size;
	}

	/**
	 * Method that writes all the words in this WeightedTrie to a file, along with each word's corresponding weight. The file format will consist of a single
	 * word per line, with the weight of the word on the same line and appearing right before the word, separated by two tab characters. The number of words in
	 * the file is on the very first line.
	 * @param filepath The location to create the new file.
	 */
	public void toFile(String filepath) {
		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.delete();
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			writer.write(String.valueOf(size()) + "\n");	
			for (String s : autocomplete("", size())) {
				writer.write(find(s).weight + "\t\t" + s.toLowerCase() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that serializes this WeightedTrie to the inputted file location, so it can be read back in at another point in time. The name of the file is 
	 * given as a parameter, but the timestamp of when the object was serialized is automatically appended to the name. All serialized objects are put in the
	 * "Serialized Objects" directory, as specified below, and are also put in a sub-directory named "Weighted Trie", which contains any serialized WeightedTrie
	 * objects.
	 * @param name The name of the serialization file.
	 */
	public void serialize(String name) {
		try {
			File folder = new File("linguistics/Serialized Objects/WeightedTrie");
			if (!folder.exists()) {
				folder.mkdir();
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(folder.getAbsolutePath() + "/" + name + " - " + (new SimpleDateFormat("hh mm ss, M-dd-yyyy")).format(Calendar.getInstance().getTime()) + ".ser"));
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static WeightedTrie deserialize(String serializeFile) {
		ObjectInputStream in = null;
		try {
			File f = new File(serializeFile);
			if (!f.getAbsolutePath().substring(f.getAbsolutePath().length() - 4).equals(".ser")) {
				throw new IllegalArgumentException("File must be a serialized file.");
			}
			in = new ObjectInputStream(new FileInputStream(serializeFile));
			return (WeightedTrie) in.readObject();
		} catch (IOException e) {
			// System.out.println("File not found.");
			e.printStackTrace();
			return null;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Inputted serialization file does not encode a WeightedTrie object.");
		} catch (ClassNotFoundException e) {
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method that provides an interactive command line user interface that allows users to interactively run commands in this class.
	 */
	public void interactiveUI() {
		try {
			Class<?> c = Class.forName("WeightedTrie");
			System.out.println("This interactive UI allows one to call methods and interact with an initially empty WeightedTrie object.");
			System.out.println("Usage: <Method name>(<parameter1, parameters2, ...>)");
			System.out.println("Parameters can only be Strings, numbers, and booleans. Enter String parameters in quotes.");
			Scanner scan = new Scanner(System.in);
			System.out.print("> ");
			String input = scan.nextLine();
	        while (!input.equals("")) {
	        	String methodName = input.substring(0, input.indexOf('('));
				String[] formatParameters = input.substring(input.indexOf('(')).split(",");
				if (formatParameters.length == 1 && formatParameters[0].equals("()")) {
					Class<?>[] types = null;
					Object[] parameters = null;
					System.out.println(c.getMethod(methodName, types).invoke(this, parameters));
					System.out.print("> ");
	            	input = scan.nextLine();
	            	continue;
				}
				formatParameters[0] = " " + formatParameters[0].substring(1);
				formatParameters[formatParameters.length - 1] = formatParameters[formatParameters.length - 1].substring(0, formatParameters[formatParameters.length - 1].length() - 1);
				Object[] parameters = null;
				for (Method m : c.getMethods()) {
					if (m.getName().equals(methodName)) {
						Class<?>[] parameterTypes = m.getParameterTypes();
						if (formatParameters.length == parameterTypes.length) {
							parameters = new Object[formatParameters.length];
							for (int i = 0; i < formatParameters.length; i++) {
								if (parameterTypes[i].getName().equals("int") || parameterTypes[i].getName().equals("java.lang.Integer")) {
									parameters[i] = Integer.parseInt(formatParameters[i]);
								} else if (parameterTypes[i].getName().equals("double") || parameterTypes[i].getName().equals("java.lang.Double")) {
									parameters[i] = Double.parseDouble(formatParameters[i]);
								} else if (parameterTypes[i].getName().equals("java.lang.String")) {
									parameters[i] = formatParameters[i].split("\"")[1];
								} else if (parameterTypes[i].getName().toLowerCase().equals("boolean") || parameterTypes[i].getName().equals("java.lang.Boolean")) {
									parameters[i] = Boolean.valueOf(formatParameters[i].toLowerCase());
								}
							}
							if (m.getReturnType().equals(Void.TYPE)) {
								System.out.println("Done.");
							} else {
								System.out.println(m.invoke(this, parameters));
							}
							break;
						}
					}
				}
				if (parameters == null) {
					throw new NoSuchMethodException("");
				}
	        	System.out.print("> ");
	            input = scan.nextLine();
	        }
	        scan.close();
		} catch (ClassNotFoundException e) {

		} catch (NoSuchMethodException e) {
			System.out.println("Inputted method does not exist in this class.");
		} catch (IllegalAccessException e) {
			System.out.println("Inputted method cannot be accessed.");
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}
	}

	/**
	 * HELPER CODE BELOW THIS LINE.
	 */

	/**
	 * Class of Node objects. Each Node represents a node in the overall WeightedTrie.
	 */
	public class Node implements Serializable {
		public HashMap<Character, Node> links;
		public char c;
		public double weight;
		public double maxWeight;
		public String word;
		public int size;

		public Node() {
			links = new HashMap<Character, Node>();
			word = "";
			size = 0;
		}

		private Node(double w) {
			weight = w;
		}

		public void insert(String fullWord, String currWord, double wordWeight, char character) {
			size++;
			if (wordWeight > maxWeight) {
				maxWeight = wordWeight;
			}
			char first = currWord.charAt(0);
			String rest = currWord.substring(1);
			if (this != root) {
				c = character;
			}
			if (rest.length() == 0) {
				if (links.containsKey(first)) {
					links.get(first).word = fullWord;
					links.get(first).weight = wordWeight;
					if (wordWeight > links.get(first).maxWeight) {
						links.get(first).maxWeight = wordWeight;						
					}
					links.get(first).size = 1;
				} else {
					Node next = new Node();
					next.c = first;
					next.word = fullWord;
					next.weight = wordWeight;
					next.maxWeight = wordWeight;
					next.size = 1;
					links.put(first, next);
				}
			} else {
				if (links.containsKey(first)) {
					Node next = links.get(first);
					next.insert(fullWord, rest, wordWeight, first);
				} else {
					Node next = new Node();
					next.insert(fullWord, rest, wordWeight, first);
					links.put(first, next);
				}
			}
		}

		public Node find(String word) {
			if (word.length() == 1) {
				if (!links.containsKey(word.charAt(0))) {
					return null;
				}
				return links.get(word.charAt(0));
			}
			if (!links.containsKey(word.charAt(0))) {
				return null;
			} else {
				return links.get(word.charAt(0)).find(word.substring(1));
			}
		}

		public boolean isWord() {
			return word.length() > 0;
		}

		public ArrayList<String> toArrayList() {
			ArrayList<String> arr = new ArrayList<String>();
			if (isWord()) {
				arr.add(word);
			}
			return toArrayListHelper(arr, "");
		}

		private ArrayList<String> toArrayListHelper(ArrayList<String> curr, String currString) {
			for (char c : links.keySet()) {
				if (links.get(c).isWord()) {
					curr.add(links.get(c).word);
				}
				curr = links.get(c).toArrayListHelper(curr, currString + c);
			}
			return curr;
		}
	}

	/**
	 * Comparator class used to compare Node objects.
	 */
	public class NodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node n1, Node n2) {
			return (int) (-(n1.maxWeight - n2.maxWeight));
		}
	}
	
	/**
	 * Helper method.
	 * Sorts the words inputted into this WeightedTrie by performing a pre-order traversal on the WeightedTrie.
	 * @param alphabet The ordering of letters to use when sorting.
	 * @param n The current node being visited.
	 * @param ret The current sorted list of words so far.
	 * @return A sorted list of every word in this WeightedTrie.
	 */
	private ArrayList<String> sortHelper(String alphabet, Node n, ArrayList<String> ret) {
		for (char c : alphabet.toCharArray()) {
			if (n.links.containsKey(c)) {
				Node next = n.links.get(c);
				if (next.isWord()) {
					ret.add(next.word);
				}
				if (!next.links.isEmpty()) {
					ret = sortHelper(alphabet, next, ret);
				}
			}
		}
		return ret;
	}

	/**
	 * Class for comparing Strings by their weights.
	 */
	public class StringComparator implements Comparator<String> {
		@Override
		public int compare(String s1, String s2) {
			int ret = - (int) (find(s1).weight - find(s2).weight);
			if (ret != 0) {
				return ret;
			} else {
				return s1.compareTo(s2);
			}
		}
	}

	/**
	 * Retrieves a StringComparator for this WeightedTrie. For use in external classes.
	 * @return The String comparator.
	 */
	public Comparator<String> getStringComparator() {
		return new StringComparator();
	}

	/**
	 * Checks if the inputted array contains duplicates.
	 * @param arr The array to check.
	 * @return Whether or not arr contains duplicates.
	 */
	private static <T> boolean duplicates(T[] arr) {
		HashSet<T> testDuplicates = new HashSet<>();
		for (T t : arr) {
			if (testDuplicates.contains(t)) {
				return true;
			}
			testDuplicates.add(t);
		}
		return false;
	}

	/**
	 * Helper method.
	 * Computes the Levenshtein distance between two Strings. The Levenshtein distance is defined as the minimum number of edits to transform one String
	 * into another, where an edit is defined as either an insertion (inserting a letter), a deletion (deleting a letter), or a substitution (changing a
	 * letter into another);
	 * @param s The first String being compared.
	 * @param t The second String being compared.
	 * @return The distance.
	 */
	public static int distance(String s, String t) {
		int[][] arr = new int[1 + t.length()][1 + s.length()];
		for (int i = 0; i < arr[0].length; i++) {
			arr[0][i] = i;
		}
		for (int j = 0; j < arr.length; j++) {
			arr[j][0] = j;
		}
		for (int i = 1; i < arr[0].length; i++) {
			for (int j = 1; j < arr.length; j++) {
				if (s.charAt(i - 1) == t.charAt(j - 1)) {
					arr[j][i] = arr[j - 1][i - 1];
				} else {
					arr[j][i] = 1 + Math.min(Math.min(arr[j - 1][i], arr[j][i - 1]), arr[j - 1][i - 1]);
				}
			}
		}
		return arr[t.length()][s.length()];
	}

	/**
	 * Computes the Levenshtein distance between two Strings recursively. This algorithm is a straightforward implementation of the definition of the 
	 * Levenshtein distance. It is MUCH slower than the iterative version.
	 * When running 1000 trials on random Strings of (randomly selected) lengths between 1 and 100, the iterative algorithm terminated in ~4 microseconds 
	 * (4 * 10^(-6)) whereas the recursive algorithm did not terminate even after 30 minutes, at which point execution was manually halted.
	 * @param s The first String.
	 * @param t The second String.
	 * @return The Levenshtein distance between s and t.
	 */
	public static int recursiveDistance(String s, String t) {
		return levHelper(s, t, s.length(), t.length());
	}

	/**
	 * Helper method.
	 * Recursively calculates the Levenshtein distance.
	 * @param s The first String.
	 * @param t The second String.
	 * @param i The current letter of s being checked.
	 * @param j The current letter of t being checked.
	 * @return The Levenshtein distance between s and t.
	 */
	private static int levHelper(String s, String t, int i, int j) {
		if (Math.min(i ,j) == 0) {
			return Math.max(i, j);
		} else {
			int indicator;
			if (s.charAt(i - 1) == t.charAt(j - 1)) {
				indicator = 0;
			} else {
				indicator = 1;
			}
			return Math.min(Math.min(levHelper(s, t, i - 1, j) + 1, levHelper(s, t, i, j - 1) + 1), levHelper(s, t, i - 1, j - 1) + indicator);
		}
	}

	/**
	 * Helper method.
	 * Prints out a 2-D array in a readable format.
	 * @param arr The array to be printed.
	 */
	private static void printArr(int[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[0].length; j++) {
				System.out.print(arr[i][j] + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Helper method.
	 * Traverses a Trie and finds words within a given Levenshtein distance of an inputted (possibly misspelled) word.
	 * @param word The word to be spell checked.
	 * @param distance The maximum Levenshtein distance to check for.
	 * @param previousRow The last row in the 2-D matrix used for computing the Levenshtein distance between word and current prefix so far.
	 * @param currNode The current node being visited.
	 * @param results The list of all words matching the above criterion.
	 */
	private void spellCheckHelper(String word, int distance, int[] previousRow, WeightedTrie.Node currNode, TreeSet<String> results) {
		int[] currentRow = new int[previousRow.length];
		currentRow[0] = previousRow[0] + 1;
		for (int i = 1; i < currentRow.length; i++) {
			if (currNode.c != word.charAt(i - 1)) {
				currentRow[i] = Math.min(Math.min(previousRow[i], previousRow[i - 1]), currentRow[i - 1]) + 1;
			} else {
				currentRow[i] = previousRow[i - 1];
			}
		}
		if (currNode.isWord() && currentRow[currentRow.length - 1] <= distance) {
			results.add(currNode.word);
		}
		for (int i : currentRow) {
			if (i <= distance) {
				for (char c : currNode.links.keySet()) {
					spellCheckHelper(word, distance, currentRow, currNode.links.get(c), results);
				}
				break;
			}
		}
	}

	/**
	 * Helper method.
	 * Given a BufferedReader for a text file, returns the first (non-empty) word in the text file.
	 * @param reader The BufferedReader used to read through the text file.
	 * @return The first non-empty String found in the file.
	 */
	private static String initializeLast(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			for (String s : line.split("[^a-zA-Z0-9']+")) {
				if (!s.isEmpty()) {
					return s;
				}
			}
		}
		return null;
	}
}