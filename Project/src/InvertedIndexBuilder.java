import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A builder class for inverted index, which is an index to store words and
 * the location (both file location and position in file) of where those words were found.
 *
 * @author Jason Liang
 * @version v3.0.2
 */
public class InvertedIndexBuilder {

	/**
	 * The logger of this class.
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * An index to store words and the location (both file location and position in file) of where those words were found.
	 */
	private final InvertedIndex index;

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
	public InvertedIndexBuilder(InvertedIndex index, WorkQueue queue) {
		this.index = index;
		this.queue = queue;
	}

	/**
	 * Constructs a InvertedIndex builder of an existing index
	 *
	 * @param index the initial contents of the InvertedIndex
	 */
	public InvertedIndexBuilder(InvertedIndex index) {
		this(index, null);
	}

	/**
	 * Adds a non-directory file into the index.
	 *
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder addFile(Path input) throws IOException {
		InvertedIndexBuilder.addFile(input, index);
		return this;
	}

	/**
	 * Adds a non-directory file into the index.
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index the index to be edited
	 * @throws IOException if the files could not be inserted
	 */
	public static void addFile(Path input, InvertedIndex index) throws IOException {
		log.trace("Called addFile()");
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			long i = 0;
			Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
			String inputString = input.toString();
			while ((line = reader.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					index.indexPut(stemmer.stem(word).toString(), inputString, ++i);
				}
			}
		}
	}

	/**
	 * Adds non-directory files into the index from directory input.
	 * If this.queue is not null, the work queue version is used.
	 *
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder traverse(Path input) throws IOException {
		if (queue != null) {
			InvertedIndexBuilder.traverse(input, (ConcurrentInvertedIndex) index, queue);
		} else {
			InvertedIndexBuilder.traverse(input, index);
		}
		return this;
	}

	/**
	 * Adds non-directory files into the index from directory input
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index the index to be edited
	 * @throws IOException if the files could not be inserted
	 */
	public static void traverse(Path input, InvertedIndex index) throws IOException {
		List<Path> paths = getFiles(input);
		for (Path in : paths) {
			addFile(in, index);
		}
	}

	/**
	 * Adds non-directory files into the thread-safe index from directory input,
	 * using a WorkQueue.
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index the thread-safe index to be edited
	 * @param queue the work queue executing the code.
	 * @throws IOException if the files could not be inserted
	 */
	public static void traverse(Path input, ConcurrentInvertedIndex index, WorkQueue queue) throws IOException {
		List<Path> paths = getFiles(input);
		int i = 0;
		for (Path in : paths) {
			log.trace("Executing {}...", ++i);
			queue.execute(new IndexingTask(index, in)); //convert to runnable
		}

		try {
			log.debug("NOTIFICATION: .finish() called");
			queue.finish();
			log.debug("NOTIFICATION: .finish() ended");
		} catch (InterruptedException e) {
			log.error("Work did NOT finish.");
		}
	}


	/**
	 * Returns all paths of {@code Path input} recursively or an empty list if the files could not be generated.
	 *
	 * @param input The root directory or text tile
	 * @return A list of paths of the entire directory of {@code Path input} or 0-1 text file(s)
	 */
	public static List<Path> getFilesOrEmpty(Path input) {
		try {
			return getFiles(input);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return Collections.emptyList();
	}

	/**
	 * Returns all paths of {@code Path} input recursively.
	 *
	 * @param input The root directory or text tile
	 * @return A list of non-directory files of the entire directory of {@code Path input}
	 * @throws IOException if the stream could not be made or if {@code Path input} does not exist
	 */
	public static List<Path> getFiles(Path input) throws IOException {
		return TextFileFinder.list(input);
	}

	/**
	 * Returns the InvertedIndex of which this index builder is wrapping.
	 *
	 * @return this InvertedIndex
	 */
	public InvertedIndex getIndex() {
		return index;
	}

	/**
	 * A Runnable for populating the index.
	 */
	private static class IndexingTask implements Runnable {
		/**
		 * The common InvertedIndex.
		 */
		private final ConcurrentInvertedIndex index;
		/**
		 * The path to be added into the common InvertedIndex.
		 */
		private final Path path;

		/**
		 * Constructs a new IndexingTask runnable to add
		 * a non-directory file into the index.
		 *
		 * @param index the thread-safe index to be edited
		 * @param path  the path to be added into InvertedIndex
		 */
		public IndexingTask(ConcurrentInvertedIndex index, Path path) {
			this.index = index;
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
