import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WebCrawler {
	/**
	 * The logger of this class
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * //TODO
	 */
	private static final int REDIRECT_LIMIT = 3;

	/**
	 * An index to store words and the location (both file location and position in file) of where those words were found.
	 */
	private final ConcurrentInvertedIndex index;

	/**
	 * A work queue for parsing each file into index.
	 */
	private final WorkQueue queue;

	/**
	 * //TODO
	 */
	private final Set<URL> consumed;

	/**
	 * //TODO
	 */
	private final int limit;

	public WebCrawler(ConcurrentInvertedIndex index, WorkQueue queue, int limit) {
		this.queue = queue;
		this.consumed = new HashSet<>();
		this.index = index;
		this.limit = limit;
	}

	/**
	 * Adds a single URL into this index.
	 *
	 * @param input the URL to be added into InvertedIndex
	 * @return the reference of this object
	 */
	public boolean addUrl(URL input) {
		if (consumed.size() >= limit || !consumed.add(input)) {
			return false;
		}
		return addUrl(HtmlFetcher.fetch(input, REDIRECT_LIMIT), input.toString(), index);
	}

	private static boolean addUrl(String html, String inputString, InvertedIndex index) {
		log.trace("Called addURL()");

		if (html == null) {
			log.warn("HTML could not be fetched.");
			return false;
		}

		long i = 0;
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		String cleaned = HtmlCleaner.stripHtml(html);

		for (String word : TextParser.parse(cleaned)) {
			index.indexPut(stemmer.stem(word).toString(), inputString, ++i);
		}
		return true;
	}

	/**
	 * Adds non-directory files into the index from directory input.
	 * If this.queue is not null, the work queue version is used.
	 *
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 */
	public WebCrawler traverse(URL input) {
		if (consumed.size() >= limit || !consumed.add(input)) {
			return this;
		}

		log.trace("Called traverse()");
		queue.execute(new CrawlingTask(input)); //convert to runnable

		try {
			log.debug("NOTIFICATION: .finish() called");
			queue.finish();
			log.debug("NOTIFICATION: .finish() ended");
		} catch (InterruptedException e) {
			log.error("Work did NOT finish.");
		}
		return this;
	}

	public WebCrawler traverse(String input) throws MalformedURLException {
		return traverse(new URL(input));
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
	private class CrawlingTask implements Runnable {
		/**
		 * The url to be added into the common InvertedIndex.
		 */
		private final URL url;

		/**
		 * Constructs a new IndexingTask runnable to add
		 * a non-directory file into the index.
		 *
		 * @param url   the url to be added into InvertedIndex
		 */
		CrawlingTask(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			String html = HtmlFetcher.fetch(url, REDIRECT_LIMIT);
			if (html == null) {
				return;
			}

			ArrayList<URL> references = LinkParser.listLinks(url, html);
			synchronized (consumed) {
				for (URL reference : references) {
					if (consumed.size() >= limit) {
						break;
					}
					if (consumed.add(reference)) {
						queue.execute(new CrawlingTask(reference));
					}
				}
			}

			InvertedIndex tempIndex = new InvertedIndex();

			/* Adds current url into tempIndex */
			addUrl(html, url.toString(), tempIndex);
			index.addAll(tempIndex); //Expensive in memory
			log.debug("Added tempIndex into index!");
		}
	}

}
