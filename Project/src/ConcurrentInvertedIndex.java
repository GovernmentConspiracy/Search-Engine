import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * A thread-safe index to store words and the location (both file location and position in file) of where those words were found.
 *
 * @author Jason Liang
 * @version v3.0.0
 */
public class ConcurrentInvertedIndex extends InvertedIndex {
	/**
	 * Read/write lock for concurrent operations.
	 */
	SimpleReadWriteLock lock;

	/**
	 * Constructs a new empty thread-safe inverted index.
	 */
	public ConcurrentInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}

//	@Override
//	public void indexPut(String word, String pathString, long location) {
//		lock.writeLock().lock();
//		try {
//			super.indexPut(word, pathString, location);
//		} finally {
//			lock.writeLock().unlock();
//		}
//	}

	@Override
	public void indexToJSON(Path output) throws IOException {
		lock.readLock().lock();
		try {
			super.indexToJSON(output);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean indexToJSONSafe(Path output) {
		lock.readLock().lock();
		try {
			return super.indexToJSONSafe(output);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<InvertedIndex.SearchResult> search(Set<String> phrases, boolean exact) {
		lock.readLock().lock();
		try {
			return super.search(phrases, exact);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Map<String, Long> getCounts() {
		lock.readLock().lock();
		try {
			return super.getCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void countToJSON(Path output) throws IOException {
		lock.readLock().lock();
		try {
			super.countToJSON(output);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean countToJSONSafe(Path output) {
		lock.readLock().lock();
		try {
			return super.countToJSONSafe(output);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word) {
		lock.readLock().lock();
		try {
			return super.contains(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location) {
		lock.readLock().lock();
		try {
			return super.contains(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean contains(String word, String location, long position) {
		lock.readLock().lock();
		try {
			return super.contains(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getWords() {
		lock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getLocations(String word) {
		lock.readLock().lock();
		try {
			return super.getLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Long> getPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.getPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();
		}
	}
}
