import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * thread safe version of the InvertedIndex class
 * 
 * @author ahmaad idrees
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/** The lock used to protect concurrent access to the underlying set. */
	private final SimpleReadWriteLock lock;

	/**
	 * Initializes an unsorted thread-safe indexed set.
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}

	@Override
	public boolean add(String stem, String inputFile, int position) {
		lock.writeLock().lock();
		try {
			return super.add(stem, inputFile, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean addAll(String inputFile, Collection<String> stemmedWords) {
		lock.writeLock().lock();
		try {
			return super.addAll(inputFile, stemmedWords);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex index) {
		lock.writeLock().lock();
		try {
			super.addAll(index);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void countsToJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.countsToJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsWord(String word) {
		lock.readLock().lock();
		try {
			return super.containsWord(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsPosition(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.containsPosition(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsLocation(String word, String location) {
		lock.readLock().lock();
		try {
			return super.containsLocation(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getUnmodifiableWords() {
		lock.readLock().lock();
		try {
			return super.getUnmodifiableWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getUnmodifiableLocations(String prefix) {
		lock.readLock().lock();
		try {
			return super.getUnmodifiableLocations(prefix);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> getUnmodifiablePositions(String prefix, String location) {
		lock.readLock().lock();
		try {
			return super.getUnmodifiablePositions(prefix, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> exactSearch(Collection<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<Result> partialSearch(Collection<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void toJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.toJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}
}
