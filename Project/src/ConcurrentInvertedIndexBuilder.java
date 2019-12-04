import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * A thread-safe builder class for inverted index, which is an index to store words and
 * the location (both file location and position in file) of where those words were found.
 *
 * @author Jason Liang
 * @version v3.0.2
 */
public class ConcurrentInvertedIndexBuilder extends InvertedIndexBuilder {

	/**
	 * The logger of this class.
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * A work queue for parsing each file into index.
	 */
	private final WorkQueue queue;

	/**
	 * Constructs a InvertedIndex builder of an existing index
	 *
	 * @param index the initial contents of the InvertedIndex
	 * @param queue the work queue executing the code.
	 */
	public ConcurrentInvertedIndexBuilder(ConcurrentInvertedIndex index, WorkQueue queue) {
		super(index);
		this.queue = queue;
	}

	@Override
	public ConcurrentInvertedIndexBuilder traverse(Path input) throws IOException {
		List<Path> paths = getFiles(input);
		int i = 0;
		for (Path in : paths) {
			log.trace("Executing {}...", ++i);
			queue.execute(new IndexingTask(in)); //convert to runnable
		}

		try {
			log.debug("NOTIFICATION: .finish() called");
			queue.finish();
			log.debug("NOTIFICATION: .finish() ended");
		} catch (InterruptedException e) {
			log.error("Work did NOT finish.");
		}
		return this;
	}

	/**
	 * A Runnable for populating the index.
	 */
	private class IndexingTask implements Runnable {
		/**
		 * The path to be added into the common InvertedIndex.
		 */
		private final Path path;

		/**
		 * Constructs a new IndexingTask runnable to add
		 * a non-directory file into the index.
		 *
		 * @param path the path to be added into InvertedIndex
		 */
		public IndexingTask(Path path) {
			this.path = path;
		}

		@Override
		public void run() {
			InvertedIndex tempIndex = new InvertedIndex();
			try {
				addFile(path, tempIndex);
			} catch (IOException e) {
				log.warn(e.getMessage());
			}
			index.addAll(tempIndex); //Expensive in memory
			log.debug("Added tempIndex into index!");
		}
	}
}
