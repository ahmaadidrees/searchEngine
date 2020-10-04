import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * interface responsible for search building
 * 
 * @author ahmaad idrees
 *
 */
public interface SearchBuilderInterface {

	/**
	 * takes a query file and performs a search on each line of the query file
	 * 
	 * @param path  to query file
	 * @param exact determines the type of search to perform
	 * @throws IOException          if IO error occurs
	 * @throws InterruptedException if interrupt occurs
	 */
	public default void parseQueries(Path path, boolean exact) throws IOException, InterruptedException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				buildSearch(line, exact);
			}
		}
	}

	/**
	 * writes search results in JSON format to a specified path
	 * 
	 * @param path to be written to
	 * @throws IOException if IO error occurs
	 */
	public void resultsToJson(Path path) throws IOException;

	/**
	 * @param line  to use
	 * @param exact boolean to determine which search to perform
	 */
	public void buildSearch(String line, boolean exact);

}
