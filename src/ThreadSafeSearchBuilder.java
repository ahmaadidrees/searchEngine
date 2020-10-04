import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * ThreadSafe version of the search builder class, uses a work queue to build
 * each search result
 * 
 * @author ahmaad idrees
 *
 */
public class ThreadSafeSearchBuilder implements SearchBuilderInterface {

	/**
	 * results map to use
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.Result>> results;
	/**
	 * inverted index to use
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * worker threads to use
	 */
	private int threads;

	/**
	 * Initializes the data structures to use
	 * 
	 * @param invertedIndex index to initialize
	 * @param threads       number of threads to initialize
	 */
	public ThreadSafeSearchBuilder(ThreadSafeInvertedIndex invertedIndex, int threads) {
		this.index = invertedIndex;
		this.results = new TreeMap<String, ArrayList<InvertedIndex.Result>>();
		this.threads = threads;
	}

	/**
	 * builds search results using a work queue to individually search each query
	 * line with a different thread
	 * 
	 * @param path  to the query file
	 * @param exact boolean to determine the type of search to perform
	 * @throws IOException          if IO error occurs
	 * @throws InterruptedException if interrupt error occurs
	 */
	@Override
	public void parseQueries(Path path, boolean exact) throws IOException, InterruptedException {
		if (threads < 1) {
			return;
		}
		WorkQueue workQ = new WorkQueue(threads);
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				workQ.execute(new SearchTask(line, exact));
			}
		} finally {
			workQ.finish();
			workQ.shutdown();
		}
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
			synchronized (results) {
				SimpleJsonWriter.writeResults(results, writer);
			}
		}
	}

	/**
	 * class responsible for using a work queue to perform a search on individual
	 * query lines
	 * 
	 * @author ahmaad idrees
	 *
	 */
	private class SearchTask implements Runnable {

		/**
		 * the query line
		 */
		private String line;

		/**
		 * determines the type of search to perform
		 */
		private boolean exact;

		/**
		 * @param qLine  the query line
		 * @param search the type of search to perform
		 */
		public SearchTask(String qLine, boolean search) {
			line = qLine;
			exact = search;
		}

		@Override
		public void run() {
			buildSearch(line, exact);
		}
	}

	@Override
	public void buildSearch(String line, boolean exact) {
		var queryLine = TextFileStemmer.uniqueStems(line);
		if (queryLine.isEmpty()) {
			return;
		}
		line = String.join(" ", queryLine);
		synchronized (results) {
			if (results.containsKey(line)) {
				return;
			}
		}
		var searchResults = index.search(queryLine, exact);
		synchronized (results) {
			results.put(line, searchResults);
		}

	}
}
