import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class used for adding and saving data into an Inverted Index
 * 
 * @author ahmaad idrees
 *
 */
public class InvertedIndex {

	/**
	 * Data structure used for storing word stems from a text file mapped to text
	 * files mapped to the locations of the word in the text file
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Data structure used for storing text files from a directory as the keys and
	 * the word count associated as the value
	 */
	private final TreeMap<String, Integer> countsMap;

	/**
	 * Initializes inverted index
	 */
	public InvertedIndex() {
		this.index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		this.countsMap = new TreeMap<String, Integer>();
	}

	/**
	 * adds individual entries into the inverted index
	 * 
	 * @param stem      key of the inverted index
	 * @param inputFile key on inner map of the inverted index
	 * @param position  value of the inner map of the inverted index
	 * @return false if all parameters already exists in the inverted index
	 *         otherwise true
	 */
	public boolean add(String stem, String inputFile, int position) {
		index.putIfAbsent(stem, new TreeMap<String, TreeSet<Integer>>());
		index.get(stem).putIfAbsent(inputFile, new TreeSet<Integer>());
		countsMap.putIfAbsent(inputFile, 0);
		if (index.get(stem).get(inputFile).add(position)) {
			countsMap.replace(inputFile, countsMap.get(inputFile) + 1);
			return true;
		}
		return false;
	}

	/**
	 * loops through a collection of words and adds each of them into the inverted
	 * index
	 * 
	 * @param inputFile    the file that contains the collection of words
	 * @param stemmedWords the collection of words to use
	 * @return true if all of the words were added, returns false if at least one
	 *         was not added
	 */
	public boolean addAll(String inputFile, Collection<String> stemmedWords) {
		boolean ret = false;
		int position = 1;
		for (String word : stemmedWords) {
			boolean wasAdded = add(word, inputFile, position++);
			if (wasAdded) {
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * fills a local inverted index with contents from the global inverted index
	 * 
	 * @param invertedIndex index to use
	 */
	public void addAll(InvertedIndex invertedIndex) {
		for (String word : invertedIndex.index.keySet()) {
			if (!this.index.containsKey(word)) {
				this.index.put(word, invertedIndex.index.get(word));
			} else {
				for (String local : invertedIndex.index.get(word).keySet()) {
					if (!this.index.get(word).containsKey(local)) {
						this.index.get(word).put(local, invertedIndex.index.get(word).get(local));
					} else {
						var locations = invertedIndex.index.get(word).get(local);
						this.index.get(word).get(local).addAll(locations);
					}
				}
			}
		}

		for (String key : invertedIndex.countsMap.keySet()) {
			if (this.countsMap.getOrDefault(key, 0) < invertedIndex.countsMap.get(key)) {
				this.countsMap.put(key, invertedIndex.countsMap.get(key));
			}
		}
	}

	/**
	 * helper method that adds search results into the results data structure
	 * 
	 * @param stem    the word to search
	 * @param lookup  map used to store locations of search results
	 * @param results the data structure that stores the search results
	 */
	private void addResults(String stem, Map<String, Result> lookup, ArrayList<Result> results) {
		for (String txtFile : index.get(stem).keySet()) {
			if (!lookup.containsKey(txtFile)) {
				Result searchResult = new Result(txtFile);
				results.add(searchResult);
				lookup.put(txtFile, searchResult);
			}
			lookup.get(txtFile).updateResult(stem);
		}
	}

	/**
	 * writes counts map in JSON format to the path provided
	 * 
	 * @param path to path to write to
	 * @throws IOException if IO error occurs
	 */
	public void countsToJson(Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			SimpleJsonWriter.asObject(this.countsMap, writer, 1);
		}
	}

	/**
	 * determines if a word is found in the inverted index
	 * 
	 * @param word to find
	 * @return true if found false if not
	 */
	public boolean containsWord(String word) {
		return index.containsKey(word);
	}

	/**
	 * determines if a word is found in a specific position in a specific location
	 * in the inverted index
	 * 
	 * @param word     the word to use
	 * @param location the location to use
	 * @param position the position to use
	 * @return returns true if the position is correct, false if is not
	 */
	public boolean containsPosition(String word, String location, int position) {
		if (containsLocation(word, location)) {
			return this.index.get(word).get(location).contains(position);
		}
		return false;
	}

	/**
	 * determines if a word is found in a specific location in the inverted index
	 * 
	 * @param word     the word to use
	 * @param location the location to use
	 * @return returns true if the location exists, false if it does not
	 */
	public boolean containsLocation(String word, String location) {
		if (containsWord(word)) {
			return this.index.get(word).containsKey(location);
		}
		return false;
	}

	/**
	 * gets an unmodifiable view of the count map
	 * 
	 * @return the count map
	 */
	public Map<String, Integer> getCountsMap() {
		return Collections.unmodifiableMap(countsMap);
	}

	/**
	 * Returns an unmodifiable view of the Stems stored in this inverted index.
	 *
	 * @return unmodifiable view of the prefixes
	 */
	public Set<String> getUnmodifiableWords() {
		return Collections.unmodifiableSet(index.keySet());
	}

	/**
	 * Returns an unmodifiable view of the locations for a given word.
	 *
	 * @param word the word to get
	 * @return unmodifiable view of the words for that prefix
	 */
	public Set<String> getUnmodifiableLocations(String word) {
		if (index.containsKey(word)) {
			return Collections.unmodifiableSet(index.get(word).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of the words for a given prefix.
	 *
	 * @param word     the word to use
	 * @param location the location to get
	 * @return unmodifiable view of the words for that prefix
	 */
	public Set<Integer> getUnmodifiablePositions(String word, String location) {
		if (index.containsKey(word) && index.get(word).containsKey(location)) {
			return Collections.unmodifiableSet(index.get(word).get(location));
		}
		return Collections.emptySet();
	}

	/**
	 * method that performs an exact search on a line from a query file
	 * 
	 * @param queries the line of a query file stored in a collection of strings
	 * @return an array list of the results from the search
	 */
	public ArrayList<Result> exactSearch(Collection<String> queries) {
		var results = new ArrayList<Result>();
		Map<String, Result> lookup = new HashMap<String, Result>();
		for (String word : queries) {
			if (index.containsKey(word)) {
				addResults(word, lookup, results);
			}
		}
		return results;
	}

	/**
	 * method that performs an partial search on a line from a query file
	 * 
	 * @param queries the line of a query file stored in a collection of strings
	 * @return an array list of the results from the search
	 */
	public ArrayList<Result> partialSearch(Collection<String> queries) {
		var results = new ArrayList<Result>();
		Map<String, Result> lookup = new HashMap<String, Result>();
		for (String word : queries) {
			for (String stem : index.tailMap(word).keySet()) {
				if (stem.startsWith(word)) {
					addResults(stem, lookup, results);
				} else {
					break;
				}
			}
		}
		return results;
	}

	/**
	 * writes inverted index in JSON format to the path provided
	 * 
	 * @param path path to use
	 * @throws IOException if an IO error occurs
	 */
	public void toJson(Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			SimpleJsonWriter.asInvertedIndex(this.index, writer, 0);
		}
	}

	/**
	 * helper method that decides which search to perform
	 * 
	 * @param queries the query line to search
	 * @param exact   boolean value that determines the search
	 * @return an array list of search results
	 */
	public ArrayList<Result> search(Collection<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}

	/**
	 * returns the inverted index as a string
	 */
	@Override
	public String toString() {
		return this.index.toString();
	}

	/**
	 * Class responsible for storing a single search result
	 * 
	 * @author Ahmaad Idrees
	 *
	 */
	public class Result implements Comparable<Result> {

		/**
		 * Stores the location a search was found
		 */
		private final String where;

		/**
		 * Stores the number of matches found from a search
		 */
		private int count;

		/**
		 * Stores the score associated with a search
		 */
		private double score;

		/**
		 * Initializes search result
		 * 
		 * @param where the location to set the result to
		 */
		public Result(String where) {
			this.where = where;
			this.count = 0;
			this.score = 0;
		}

		/**
		 * gets the location of a search
		 * 
		 * @return the location
		 */
		public String getWhere() {
			return this.where;
		}

		/**
		 * gets the location of a search
		 * 
		 * @return the count
		 */
		public int getCount() {
			return this.count;
		}

		/**
		 * updates results by updating new count and score
		 * 
		 * @param word the word to use
		 * 
		 */
		private void updateResult(String word) {
			this.count += index.get(word).get(where).size();
			this.score = (double) this.count / countsMap.get(where);
		}

		/**
		 * gets the score of a search
		 * 
		 * @return the score
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * returns the search as a string
		 */
		@Override
		public String toString() {
			return (this.where + " " + this.count + " " + this.score);

		}

		/**
		 * compareTo method overridden to sort search results
		 */
		@Override
		public int compareTo(Result searchResult) {
			if (this.score > searchResult.score) {
				return -1;
			} else if (this.score < searchResult.score) {
				return 1;
			} else {
				if (this.count > searchResult.count) {
					return -1;
				} else if (this.count < searchResult.count) {
					return 1;
				} else {
					return this.where.compareTo(searchResult.where);
				}
			}
		}
	}
}
