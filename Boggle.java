// package linguistics;

import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.lang.Math;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Solves the game "Boggle", ie efficiently finds all possible words that can be constructed from an N by M grid of letters, by starting at one letter and 
 * going up, down, left, or right to create words. Acceptable words are specified in some inputted dictionary, which will usually be the English language.
 */
public class Boggle {
	Trie dictionary = new Trie();
	char[][] board;

	/**
	 * Basic constructor for Boggle.
	 * @param dictionary The file containing the list of acceptable words.
	 * @param board The file containing the board to use.
	 */
	public Boggle(String dictionary, String board) {
		initializeDictionary(dictionary);
		initializeBoard(board);
	}

	/**
	 * Constructor for Boggle that uses a random board. Leave alphabet as the empty String for default English alphabet.
	 * @param dictionary The file containing the list of acceptable words.
	 * @param n The number of rows in the board.
	 * @param m The number of columns in the board.
	 * @param alphabet The alphabet to use when generating letters for the board.
	 */
	public Boggle(String dictionary, int n, int m, String alphabet) {
		initializeDictionary(dictionary);
		initializeBoard(n, m, alphabet);
	}

	/**
	 * Generates all acceptable (according to the dictionary) words in the board.
	 * @return The list of words.
	 */
	public HashSet<String> solve() {
		HashSet<String> results = new HashSet<>();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				solveHelper(i, j, dictionary.next.get(board[i][j]), results, Character.toString(board[i][j]), new boolean[board.length][board[0].length]);
			}
		}
		return results;
	}

	/**
	 * Returns the n longest words found in this board.
	 * @param n The number of words to return.
	 */
	public ArrayList<String> solve(int n) {
		TreeMap<Integer, ArrayList<String>> map = new TreeMap<>();
		for (String s : solve()) {
			ArrayList<String> temp;
			if (map.containsKey(s.length())) {
				temp = map.get(s.length());
			} else {
				temp = new ArrayList<String>();
			}
			temp.add(s);
			map.put(s.length(), temp);
		}
		ArrayList<String> ret = new ArrayList<>();
		int i = 0;
		for (int length : map.descendingKeySet()) {
			for (String s : map.get(length)) {
				if (i > n) {
					return ret;
				}
				ret.add(s);
				i++;
			}
		}
		return ret;
	}

	/**
	 * Initializes the dictionary for use in this Boggle instance. If an empty String is passed, the default dictionary (the English language) is used.
	 * It is assumed that the dictionary file is a text file with a word on each line. The first line should be the number of words in the file.
	 * @param filepath The path to the text file containing the words.
	 */
	public void initializeDictionary(String filepath) {
		try {
			if (filepath.isEmpty()) {
				filepath = "C:/Users/vip/Documents/Files/English_language.txt";
			}
			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
			reader.readLine();
			String line = reader.readLine();
			while (line != null) {
				dictionary.insert(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Cannot find file: " + filepath);
		}
	}

	/**
	 * Initializes a Boggle board based on a text file.
	 * @param filepath The path to the text file containing the words.
	 */
	public void initializeBoard(String filepath) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
			String line = reader.readLine();
			board = new char[countLines(filepath)][line.length()];
			int i = 0;
			int j = 0;
			while (line != null) {
				j = 0;
				for (char c : line.toCharArray()) {
					board[i][j] = c;
					j++;
				}
				i++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Cannot find file: " + filepath);
		}
	}

	/**
	 * Initializes a random N by M Boggle board containing letters from an inputted alphabet. If an empty String is passed as the alphabet, the default 
	 * lowercase English alphabet is used.
	 * @param n The length of the board.
	 * @param m The width of the board.
	 * @param alphabet The alphabet from which to choose letters.
	 */
	public void initializeBoard(int n, int m, String alphabet) {
		if (alphabet.isEmpty()) {
			alphabet = "abcdefghijklmnopqrstuvwxyz";
		}
		board = new char[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				board[i][j] = alphabet.charAt((int) (Math.random() * alphabet.length()));
			}
		}
	}

	/**
	 * Efficiently counts the number of lines in a file.
	 * @param filename The path to the file to be used for counting.
	 * @return The number of lines in the file.
	 */
	public static int countLines(String filename) throws IOException {
	    BufferedReader b = new BufferedReader(new FileReader(new File(filename)));
	    int count = 0;
	    while (b.readLine() != null) {
	    	count++;
	    }
	    return count;
	}
	
	/**
	 * Helper method.
	 * For the letter in board at the inputted coordinates, checks if surrounding letters are in the dictionary. If they are, recursively explores those letters.
	 * @param i The current row coordinate.
	 * @param j The current column coordinate.
	 * @param node The current node in the Trie being visited.
	 * @param results The list so far of found words.
	 * @param currString The word constructed thus far.
	 */
	public void solveHelper(int i, int j, Trie node, HashSet<String> results, String currString, boolean[][] marked) {
		if (node.word) {
			results.add(currString);
		}		
		marked[i][j] = true;
		for (int row = -1; row <= 1; row++) {
			for (int col = -1; col <= 1; col++) {
				if ((row == 0 && col == 0) || (i + row < 0 || i + row >= board.length || j + col < 0 || j + col >= board[0].length)) {
					continue;
				}
				char c = board[i + row][j + col];
				if (node.next.containsKey(c) && !marked[i + row][j + col]) {
					solveHelper(i + row, j + col, node.next.get(c), results, currString + c, marked);
					marked[i + row][j + col] = false;
				}
			}
		}
	}

	/**
	 * Helper method for printing out the board. This method will print out any 2-D array of characters in a readable format.
	 * @param arr The array to print.
	 */
	public static void printArr(char[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[0].length; j++) {
				System.out.print(arr[i][j] + " ");
			}
			System.out.println();
		}
	}
}