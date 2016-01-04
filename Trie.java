// package linguistics;

import java.util.HashMap;

/**
 * Prefix-Trie. Supports linear (in length of the word) time find and insert operations. 
 */
public class Trie {
    public HashMap<Character, Trie> next = new HashMap<Character, Trie>();
    public boolean word = false;

    /**
     * Basic constructor. 
     */
    public Trie() {

    }

    /**
     * Constructor with option to set word.
     * @param fullWord What word should be set to.
    */
    public Trie(boolean fullWord) {
        word = fullWord;
    }

    /**
     * Constructor with option to set next.
     * @param nextt What next should be set to.
     */
    public Trie(HashMap<Character, Trie> nextt) {
        next = nextt;
    }

    /**
     * Full constructor.
     * @param nextt What next should be set to.
     * @param wordd What word should be set to.
     */
    public Trie(HashMap<Character, Trie> nextt, boolean wordd) {
        next = nextt;
        word = wordd;
    }

    /**
     * Modifies this Trie's word.
     *
     * @param newWord The boolean value to which this Trie's word should be.
     */
    public void setWord(boolean newWord) {
        word = newWord;
    }

    /**
     * Returns whether or not this Trie contains S; if isFullWord is false, returns whether this 
     * Trie contains any partial prefix of S.
     * @param s The word to be found.
     * @param isFullWord Whether the word is a complete word or simply a prefix.
     * @return Returns whether or not S exists in this Trie.
     */
    public boolean find(String s, boolean isFullWord) {
        char first = s.substring(0, 1).charAt(0);
        String rest = s.substring(1);
        if (rest.equals("")) {
            if (next.containsKey(first)) {
                if (isFullWord) {
                    return next.get(first).word;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        if (next.containsKey(first)) {
            return next.get(first).find(rest, isFullWord);
        } else {
            return false;
        }
    }

    /** 
     * Inserts S into this trie.
     * @param s The word to be inserted.
     */
    public void insert(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Cannot insert a null string.");
        } else if (s.length() == 0) {
            throw new IllegalArgumentException("Cannot insert an empty string.");
        }
        char first = s.substring(0, 1).charAt(0);
        String rest = s.substring(1);
        if (rest.equals("")) {
            if (next.containsKey(first)) {
                next.get(first).setWord(true);
            } else {
                next.put(first, new Trie(true));
            }
        } else if (next.containsKey(first)) {
            next.get(first).insert(rest);
        } else {
            Trie temp = new Trie();
            temp.insert(rest);
            next.put(first, temp);
        }
    }
}
