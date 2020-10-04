import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thread safe version of the index builder uses a work queue to build the
 * inverted index by a single text file at a time
 * 
 * @author ahmaad idrees
 *
 */
public class ThreadSafeIndexBuilder {

	/**
	 * add input into the inverted index but uses a specified number of threads in a
	 * work queue
	 * 
	 * @param inputPath file to use
	 * @param index     inverted index to use
	 * @param threads   number of threads to use
	 * @throws IOException          if IO error occurs
	 * @throws InterruptedException if interrupt occurs
	 */
	public static void addInputWithThreads(Path inputPath, ThreadSafeInvertedIndex index, int threads)
			throws IOException, InterruptedException {
		if (threads < 1) {
			return;
		}
		List<Path> stream = new ArrayList<Path>();
		stream = TextFileFinder.find(inputPath).collect(Collectors.toList());
		WorkQueue workQ = new WorkQueue(threads);
		for (Path textFile : stream) {
			workQ.execute(new IndexTask(textFile, index));
		}
		workQ.finish();
		workQ.shutdown();
	}

	/**
	 * Class responsible for building the inverted index using a work queue
	 * 
	 * @author ahmaadidrees
	 *
	 */
	private static class IndexTask implements Runnable {

		/**
		 * the text file to build the inverted index from
		 */
		private final Path textFile;

		/**
		 * the inverted index to build to
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * Initializes text file and thread safe inverted index
		 * 
		 * @param text          to use
		 * @param invertedIndex to use
		 */
		public IndexTask(Path text, ThreadSafeInvertedIndex invertedIndex) {
			textFile = text;
			index = invertedIndex;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				IndexBuilder.build(textFile, local);
				index.addAll(local);
			} catch (Exception e) {
				System.err.println("Error occured while building the index with threads");
			}
		}
	}
}
