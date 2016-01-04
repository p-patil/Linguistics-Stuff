// package linguistics;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Math;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

/**
 * Implements a directed acyclic word graph, or D.A.W.G. Dawgs are deterministic finite automata that input String of symbols from the English alphabet
 * and accept specific words in the English language, and have the property that they contain no cycles. Tries are also deterministic finite automata,
 * but consume much more memory than a Dawg does; the central difference between the two is that while Tries eliminate prefix redundancy by storing words 
 * with the same prefix on the same nodes, Dawgs do the same and also eliminate suffix redundancy by employing similar memory efficient techniques on the ends
 * of words.
 * Dawgs are the minimal automata (in terms of the number of states) that accept the given language. Although they are very efficient at storing large amounts
 * of words and for lookup, prefix, and spell check operations, they are not optimal for inserts and deletions.
 * This class in particular gives each Node some additional information (such as character and depth) in order to make construction simpler; if need be, to save
 * space, each node in the fully minimized and streamlined DAWG could be converted to a version of itself with minimal information (essentially containing only
 * a map to children and an endOfWord boolean) to minimize memory consumption.
 */
public class Dawg {
	public Node root; // Initial state of the automaton.

	/**
	 * Basic constructor.
	 */
	public Dawg() {
		root = new Node();
	}

	/**
	 * Constructor that inputs all the words in the file given by filename into a DAWG. The expected file format is for the first line to be the number of words
	 * in the file, and for subsequent lines to contain a single String each (ie Strings are separated by new lines).
	 * @param filename The path to the file for input.
	 */
	public Dawg(String filename) {
		try {
			root = new Node();
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			reader.readLine();
			String line = reader.readLine();
			while (line != null) {
				insert(line.toLowerCase());
				line = reader.readLine();
			}
			minimize();
			removeRedundantLinks();
		} catch (IOException e) {
			System.out.println("Cannot find file: " + filename);
		}
	}

	/**
	 * Inserts a word into this DAWG.
	 * @param word The String to insert.
	 */
	public void insert(String word) {
		if (word.isEmpty() || word == null) {
			throw new IllegalArgumentException("Cannot insert empty or null String.");
		}
		root.insert(word, word.charAt(0));
		root.c = '\u0000';
	}

	/**
	 * Returns the Node at the end of the inputted word, or null if the word is not in this DAWG.
	 * @param word The String to search for.
	 * @return The Node at the end of word.
	 */
	public Node find(String word) {
		if (word.isEmpty() || word == null) {
			throw new IllegalArgumentException("Cannot search for empty or null String.");
		}
		return root.find(word);
	}

	/**
	 * HELPER METHODS BELOW THIS LINE.
	 */

	/**
	 * Helper class that represents the Node objects of the DAWG.
	 */
	public static class Node {
		char c;
		boolean endOfWord;
		HashMap<Character, Node> links;
		int depth;

		public Node() {
			links = new HashMap<>();			
			depth = 0;
		}

		public int insert(String word, char character) {
			c = character;
			if (word.isEmpty()) {
				endOfWord = true;
				depth = 0;
				return 0;
			}
			char first = word.charAt(0);
			String rest = word.substring(1);
			if (links.containsKey(first)) {
				depth = Math.max(1 + links.get(first).insert(rest, first), depth);
			} else {
				Node n = new Node();
				depth = Math.max(1 + n.insert(rest, first), depth);
				links.put(first, n);
			}
			return depth;
		}

		public Node find(String word) {
			char first = word.charAt(0);
			String rest = word.substring(1);
			if (!links.containsKey(first)) {
				return null;
			}
			if (rest.isEmpty()) {
				return links.get(first);
			}
			return links.get(first).find(rest);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Node)) {
				throw new IllegalArgumentException("Can only compare two Nodes.");
			}
			Node n = (Node) obj;
			if (c != n.c || links.size() != n.links.size()) {
				return false;
			}
			if (links.size() == 0) {
				return true;
			} else {
				for (char c : links.keySet()) {
					boolean same = false;
					for (char d : n.links.keySet()) {
						if (links.get(c).equals(n.links.get(d))) {
							same = true;
							break;
						}
					}
					if (!same) {
						return false;
					}
				}
				return true;
			}
		}
	}

	/**
	 * Helper method.
	 * Merges identical nodes together, thereby eliminating suffix redundancy.
	 */
	private void minimize() {
		HashMap<Integer, ArrayList<Node>> depthNodes = nodesByDepth();
		HashMap<Node, HashMap<Character, ArrayList<Node>>> allParents = getAllParents();
		for (int depth = 0; depth < root.depth; depth++) {
			if (depthNodes.containsKey(depth)) {
				minimizeHelper(depthNodes.get(depth), allParents);
			}
		}
	}

	/**
	 * Helper method for minimize.
	 */
	private void minimizeHelper(ArrayList<Node> nodes, HashMap<Node, HashMap<Character, ArrayList<Node>>> parents) {
		while (!nodes.isEmpty()) {
			Node compare = nodes.remove(0);
			for (int i = 0; i < nodes.size();) {
				if (compare.equals(nodes.get(i))) {
					for (char c : parents.get(nodes.get(i)).keySet()) {
						for (Node n : parents.get(nodes.get(i)).get(c)) {
							n.links.put(c, compare);
						}
					}
					nodes.remove(i);
				} else {
					i++;
				}
			}
		}
	}

	/**
	 * Helper method.
	 * Further streamlines the minimized DAWG by removing all redundant links that can be safely removed (ie without introducing ambiguity or losing 
	 * information).
	 */
	private void removeRedundantLinks() {
		HashMap<Integer, ArrayList<Node>> depthNodes = nodesByDepth();
		HashMap<Node, HashMap<Character, ArrayList<Node>>> allParents = getAllParents();
		for (int depth = 0; depth < root.depth; depth++) {
			removeRedundantLinksHelper(depthNodes.get(depth), allParents);
		}
	}

	/**
	 * Helper method for removeRedundantLinks.
	 */
	private void removeRedundantLinksHelper(ArrayList<Node> nodes, HashMap<Node, HashMap<Character, ArrayList<Node>>> parents) {
		while (!nodes.isEmpty()) {
			Node compare = nodes.remove(0);
			Node newParent = parents.get(compare).get(compare.c).get(0);
			for (char c : parents.get(compare).keySet()) {
				for (Node n : parents.get(compare).get(c)) {
					if (n != newParent && n.links.size() == 1) {
						merge(newParent, n, parents);
					}
				}
			}
		}
	}

	/**
	 * Helper method for removeRedundantLinksHelper. Merges two Nodes together by causing the parent of the second Node to point to the first instead, causing
	 * the second node to be deleted. 
	 * This method should only be used if the parent of second has no other children besides second.
	 */
	private void merge(Node first, Node second, HashMap<Node, HashMap<Character, ArrayList<Node>>> parents) {
		parents.get(second).get(second.c).get(0).links.put(second.c, first);
	}

	/**
	 * Returns a map that maps a depth to the list of nodes in this DAWG at that depth.
	 * @return The map.
	 */
	private HashMap<Integer, ArrayList<Node>> nodesByDepth() {
		HashMap<Integer, ArrayList<Node>> ret = new HashMap<>();
		nodesByDepthHelper(ret, root);
		return ret;
	}

	/**
	 * Helper method to nodesByDepth.
	 */
	private void nodesByDepthHelper(HashMap<Integer, ArrayList<Node>> curr, Node n) {
		ArrayList<Node> temp;
		if (curr.containsKey(n.depth)) {
			temp = curr.get(n.depth);
		} else {
			temp = new ArrayList<Node>();
		}
		temp.add(n);
		curr.put(n.depth, temp);
		for (char c : n.links.keySet()) {
			nodesByDepthHelper(curr, n.links.get(c));
		}
	}

	/**
	 * Maps nodes to their parents. Specifically, returns a map that maps nodes to another map which maps characters to lists of nodes that are parents to the 
	 * node initially mapped and which go to that node through the aforementioned character.
	 * @return The map.
	 */
	private HashMap<Node, HashMap<Character, ArrayList<Node>>> getAllParents() {
		HashMap<Node, HashMap<Character, ArrayList<Node>>> ret = new HashMap<>();
		getAllParentsHelper(ret, root);
		return ret;
	}

	/**
	 * Helper method to getAllParents.
	 */
	private void getAllParentsHelper(HashMap<Node, HashMap<Character, ArrayList<Node>>> curr, Node n) {
		for (char c : n.links.keySet()) {
			HashMap<Character, ArrayList<Node>> tempMap;
			if (curr.containsKey(n.links.get(c))) {
				tempMap = curr.get(n.links.get(c));
			} else {
				tempMap = new HashMap<>();
			}
			ArrayList<Node> tempArr;
			if (tempMap.containsKey(c)) {
				tempArr = tempMap.get(c);
			} else {
				tempArr = new ArrayList<>();
			}
			tempArr.add(n);
			tempMap.put(c, tempArr);
			curr.put(n.links.get(c), tempMap);
			getAllParentsHelper(curr, n.links.get(c));
		}
	}

	/**
	 * Manually computes the size of the DAWG.
	 * @return The size.
	 */
	public int computeSize() {
		return sizeHelper(0, root, new HashMap<Node, Boolean>());
	}

	/**
	 * Helper method for computeSize.
	 */
	private int sizeHelper(int size, Node n, HashMap<Node, Boolean> marked) {
		if (marked.containsKey(n)) {
			return 0;
		} else {
			marked.put(n, true);
		}
		int currSize = size;
		for (char c : n.links.keySet()) {
			size += sizeHelper(currSize, n.links.get(c), marked);
		}
		return size + 1;
	}
}