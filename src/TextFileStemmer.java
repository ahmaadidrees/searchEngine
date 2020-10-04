import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into collections
 * of stemmed words.
 *
 * @author Ahmaad Idrees
 * 
 */
public class TextFileStemmer {

	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a list of cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static ArrayList<String> listStems(String line, Stemmer stemmer) {
		ArrayList<String> stemmedWords = new ArrayList<String>();
		stemToCollection(line, stemmer, stemmedWords);
		return stemmedWords;
	}

	/**
	 * Helper function that stems a word and stores it in some collection of
	 * strings.
	 * 
	 * @param line         the line of words to clean, split, and stem
	 * @param stemmer      the stemmer to use
	 * @param stemmedWords some collecton of strings
	 */
	public static void stemToCollection(String line, Stemmer stemmer, Collection<String> stemmedWords) {
		String wordList[] = TextParser.parse(line);
		for (String word : wordList) {
			String stem = stemmer.stem(word).toString();
			stemmedWords.add(stem);
		}
	}

	/**
	 * Returns a list of cleaned and stemmed words parsed from the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a list of cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #listStems(String, Stemmer)
	 */
	public static ArrayList<String> listStems(String line) {
		return listStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed from
	 * the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
		TreeSet<String> stemmedWords = new TreeSet<String>();
		stemToCollection(line, stemmer, stemmedWords);
		return stemmedWords;
	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed from
	 * the provided line.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static TreeSet<String> uniqueStems(String line) {
		return uniqueStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(Path inputFile) throws IOException {
		TreeSet<String> stemmedWords = new TreeSet<String>();
		stemToCollection(inputFile, stemmedWords);
		return stemmedWords;
	}

	/**
	 * Helper function that stems all of the words in a inputFile and stores it in
	 * some collection of strings.
	 * 
	 * @param inputFile    the text to stem
	 * @param stemmedWords the collection of strings
	 * @throws IOException if unable to read or parse file
	 */
	public static void stemToCollection(Path inputFile, Collection<String> stemmedWords) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
			String line;
			Stemmer stemmer = new SnowballStemmer(DEFAULT);
			while ((line = reader.readLine()) != null) {
				stemToCollection(line, stemmer, stemmedWords);
			}
		}
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static ArrayList<String> listStems(Path inputFile) throws IOException {
		ArrayList<String> stemmedWords = new ArrayList<String>();
		stemToCollection(inputFile, stemmedWords);
		return stemmedWords;
	}
}
