import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;


/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class Driver {

	/**
	 * Stores the default value for the results flag if a value has not been
	 * provided
	 */
	public static final Path DEFAULT_RESULT = Path.of("results.json");
	/**
	 * Stores the default value for the counts flag if a value has not been provided
	 */
	public static final Path DEFAULT_COUNT = Path.of("counts.json");

	/**
	 * Stores the default value for the index flag if a value has not been provided
	 */
	public static final Path DEFAULT_INDEX = Path.of("index.json");

	/**
	 * 
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		for(String arg : args) {
			System.out.println(arg.toString());
		}
	//	System.out.println(args.toString());
		/*
		 * Stores flags mapped to values
		 */
		var map = new ArgumentParser(args);
		/*
		 * Data structure used for storing word stems from a text file mapped to text
		 * files mapped to the locations of the word in the text file
		 */
		InvertedIndex invertedIndex;
		/*
		 * object used for building search results
		 */
		SearchBuilderInterface searchBuilder;
		/*
		 * Thread safe version of the inverted index
		 */
		ThreadSafeInvertedIndex threadSafeIndex = null;
		/*
		 * number of threads to use
		 */
		int threads = 5;
		int limit = 50;
		int port = 8080;

		if (map.hasFlag("-threads")) {
			threadSafeIndex = new ThreadSafeInvertedIndex();
			threads = map.threadArgs(map.getString("-threads"));
			searchBuilder = new ThreadSafeSearchBuilder(threadSafeIndex, threads);
			invertedIndex = threadSafeIndex;
		} 
		else if(map.hasFlag("-url") && !map.hasFlag("-threads")) {
			threadSafeIndex = new ThreadSafeInvertedIndex();
			searchBuilder = new ThreadSafeSearchBuilder(threadSafeIndex, threads);
			invertedIndex = threadSafeIndex;
		}
		else {
			invertedIndex = new InvertedIndex();
			searchBuilder = new SearchBuilder(invertedIndex);
		}
		
		if (map.hasFlag("-url")) {

			if (map.hasFlag("-limit")) {
				limit = Integer.parseInt(map.getString("-limit"));
			}
			String urlSeedString = map.getString("-url");
			try {
				URL url = new URL(urlSeedString);
				WorkQueue workQ = null;
				WebCrawler crawler = new WebCrawler(workQ,limit,threads, threadSafeIndex);
				try {
					crawler.buildFromSeed(url, threadSafeIndex);
				} catch (InterruptedException e) {
					System.out.println("interrupt error occured while crawling");
				}	
			} catch (MalformedURLException e) {
				System.out.println("MalformedURLException occured while getting the url");
			}
			catch (IOException e) {
				System.out.println("IO error occured");
			}
		}
		if (map.hasFlag("-port")) {
			port = Integer.parseInt(map.getString("-port"));
			// Create the Jetty server
			MyServer server = new MyServer(threadSafeIndex);
		    Server jettyServer = new Server();

		    // Setup the connector component
		    ServerConnector connector = new ServerConnector(jettyServer);
		    connector.setHost("localhost");
		    connector.setPort(port);

		    // Setup the handler component
		    ServletHandler handler = new ServletHandler();
		    
		    handler.addServletWithMapping(MyServer.SearchServlet.class, "/");

		    // Configure server to use connector and handler
		    jettyServer.addConnector(connector);
		    jettyServer.setHandler(handler);

		    // Start the server (it is a thread)
		    try {
				jettyServer.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    System.out.println(" Server: " + jettyServer.getState());
		    System.out.println("Threads: " + jettyServer.getThreadPool().getThreads());

		    // Keeps main thread active as long as server is active
		    // Until we implement shutdown, will never see the server enter terminated state
		    try {
				jettyServer.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    System.out.println(" Server: " + jettyServer.getState());
		}
		if (map.hasFlag("-path")) {
			Path inputPath = map.getPath("-path");
			try {
				if (threadSafeIndex != null) {
					ThreadSafeIndexBuilder.addInputWithThreads(inputPath, threadSafeIndex, threads);
				} else {
					IndexBuilder.addInput(inputPath, invertedIndex);
				}
			} catch (NullPointerException e) {
				System.out.println("unable to build inverted index because a path has not been provided");
			} catch (Exception e) {
				System.out.println("unable to build inverted index from path: " + inputPath.toString());
			}
		}
		if (map.hasFlag("-index")) {
			Path outputPath = map.getPath("-index", DEFAULT_INDEX);
			try {
				invertedIndex.toJson(outputPath);
			} catch (IOException e) {
				System.out.println("unable to write inverted index to path: " + outputPath.toString());
			} catch (Exception e) {
				System.out.println("unable to write inverted index to path: " + outputPath.toString());
			}
		}
		if (map.hasFlag("-query")) {
			try {
				Path queryPath = map.getPath("-query");
				searchBuilder.parseQueries(queryPath, map.hasFlag("-exact"));
			} catch (IOException e) {
				System.out.println("unable to Stem the query file " + map.getPath("-query"));
			} catch (NullPointerException e) {
				System.out.println("unable to build inverted index because a path has not been provided");
			} catch (InterruptedException e) {
				System.out.println("unable to search results because an interrupt exception occured");
			}
		}
		if (map.hasFlag("-results")) {
			Path resultPath = map.getPath("-results", DEFAULT_RESULT);
			try {
				searchBuilder.resultsToJson(resultPath);
			} catch (IOException e) {
				System.out.println("unable to write results to path: " + resultPath);
			}
		}
		if (map.hasFlag("-counts")) {
			Path countPath = map.getPath("-counts", DEFAULT_COUNT);
			try {
				invertedIndex.countsToJson(countPath);
			} catch (IOException e) {
				System.out.println("unable to write Count Map to path: " + countPath);
			}
		}
	}
}
