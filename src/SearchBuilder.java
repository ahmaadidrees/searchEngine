import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Class responsible for building a search and storing the results
 * 
 * @author Ahmaad Idrees
 *
 */
public class SearchBuilder implements SearchBuilderInterface {

	/**
	 * Used for keeping track of search results
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> results;
	/**
	 * Inverted Index to use
	 */
	private final InvertedIndex index;

	/**
	 * initializes results and inverted index
	 * 
	 * @param invertedIndex inverted index to use
	 */
	public SearchBuilder(InvertedIndex invertedIndex) {

		this.index = invertedIndex;
		this.results = new TreeMap<String, ArrayList<InvertedIndex.Result>>();
	}

	/**
	 * Writes results to a specific path
	 * 
	 * @param path the path to write to
	 * @throws IOException if IO error occurs
	 */
	@Override
	public void resultsToJson(Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			SimpleJsonWriter.writeResults(results, writer);
		}
	}

	/**
	 * Helper method that takes a line and performs a search
	 * 
	 * @param line  the line to search
	 * @param exact determines which search to perform
	 */
	@Override
	public void buildSearch(String line, boolean exact) {
		var queryLine = TextFileStemmer.uniqueStems(line);
		if (queryLine.isEmpty()) {
			return;
		}
		line = String.join(" ", queryLine);
		if (results.containsKey(line)) {
			return;
		}
		var searchResults = index.search(queryLine, exact);
		results.put(line, searchResults);
	}

}
