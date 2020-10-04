import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for building the inverted index
 * 
 * @author Ahmaad Idrees
 *
 */
public class IndexBuilder {

	/**
	 * Adds all input from the file inputPath into the inverted index
	 * 
	 * @param inputPath path to either a text file or directory
	 * @param index     inverted index to use
	 * @throws IOException if an IO error occurs
	 */
	public static void addInput(Path inputPath, InvertedIndex index) throws IOException {
		List<Path> stream = new ArrayList<Path>();
		stream = TextFileFinder.find(inputPath).collect(Collectors.toList());
		for (Path textFile : stream) {
			build(textFile, index);
		}
	}

	/**
	 * helper method that actually adds the input into the inverted index
	 * 
	 * @param textFile path to either a text file or a directory
	 * @param index    inverted index to use
	 * @throws IOException if an IO error occurs
	 */
	public static void build(Path textFile, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(textFile, StandardCharsets.UTF_8)) {
			String line;
			Stemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
			int position = 1;
			String location = textFile.toString();
			while ((line = reader.readLine()) != null) {
				String wordList[] = TextParser.parse(line);
				for (int i = 0; i < wordList.length; i++) {
					String stem = stemmer.stem(wordList[i]).toString();
					index.add(stem, location, position++);
				}
			}
		}
	}
	
	/**
	 * helper method that actually adds the input into the inverted index
	 * @param url link to build from
	 * 
	 * @param html  to use
	 * 
	 * @param index inverted index to use
	 * @throws IOException if an IO error occurs
	 */
	public static void build(URL url, InvertedIndex index, String html) throws IOException {

		Stemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
		int position = 1;
		String location = url.toString();
		//synchronized(url) {
		//	String html = HtmlFetcher.fetch(url);
		//	var cleanedHtml = new HtmlCleaner(url, html);
		//	html = cleanedHtml.getHtml();
		//	System.out.println("location: "+location);
		String wordList[] = TextParser.parse(html);
			//LinkParser.listLinks(url, html);
		for (int i = 0; i < wordList.length; i++) {
				//System.out.println("wordList["+i+"]: "+wordList[i]);
			String stem = stemmer.stem(wordList[i]).toString();
			index.add(stem, location, position++);
		}
	//	}
		
		

	}
}
