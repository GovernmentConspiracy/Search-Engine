import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A web crawled builder class for inverted index, which is an index to store words and
 * the location (both file location and position in file) of where those words were found.
 *
 * @author Jason Liang
 * @version v4.0.0
 */
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
	 * The default maximum number of redirects.
	 */
	private static final int DEFAULT_REDIRECT_LIMIT = 3;

	/**
	 * An index to store words and the location (both file location and position in file) of where those words were found.
	 */
	private final ConcurrentInvertedIndex index;

	/**
	 * A work queue for parsing each file into index.
	 */
	private final WorkQueue queue;

	/**
	 * A Set to store unique URLs
	 */
	private final Set<URL> consumed;

	/**
	 * The maximum number of URLs stored in the set
	 */
	private final int limit;

	/**
	 * The maximum number of times to follow redirects
	 */
	private final int redirects;

	/**
	 * Constructs a web crawler of an existing thread-safe index.
	 *
	 * @param index     the initial contents of the InvertedIndex
	 * @param queue     the work queue executing code.
	 * @param limit     the maximum limit of URLs to be traversed
	 * @param redirects the maximum limit of redirects to follow
	 */
	public WebCrawler(ConcurrentInvertedIndex index, WorkQueue queue, int limit, int redirects) {
		this.queue = queue;
		this.consumed = new HashSet<>();
		this.index = index;
		this.limit = limit;
		this.redirects = redirects;
	}

	/**
	 * Constructs a web crawler of an existing thread-safe index
	 * with the default redirect limit.
	 *
	 * @param index the initial contents of the InvertedIndex
	 * @param queue the work queue executing code.
	 * @param limit the maximum limit of URLs to be traversed
	 */
	public WebCrawler(ConcurrentInvertedIndex index, WorkQueue queue, int limit) {
		this(index, queue, limit, DEFAULT_REDIRECT_LIMIT);
	}

	/**
	 * Attempts to adds a single URL into this index. If there is space in consumed,
	 * the URL is unique, and it is valid HTML, returns {@code true}.
	 *
	 * @param input the URL to be added into InvertedIndex
	 * @return {@code true} if the URL can be added.
	 */
	public boolean addUrl(URL input) {
		if (consumed.size() >= limit || !consumed.add(input)) {
			return false;
		}
		String html = HtmlFetcher.fetch(input, DEFAULT_REDIRECT_LIMIT);
		if (html == null) {
			log.warn("HTML could not be fetched.");
			return false;
		}
		addUrl(html, input.toString(), index);
		return true;
	}

	/**
	 * Adds a single URL into this index.
	 *
	 * @param html        the html from the input url
	 * @param inputString the URL in string form to be added into InvertedIndex
	 * @param index       index the index to be edited
	 */
	private static void addUrl(String html, String inputString, InvertedIndex index) {
		log.trace("Called addURL()");

		long i = 0;
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);

		for (String word : TextParser.parse(HtmlCleaner.stripHtml(html))) {
			index.indexPut(stemmer.stem(word).toString(), inputString, ++i);
		}
	}

	/**
	 * Adds the url input and the url of its contents into the index from URL input.
	 *
	 * @param input the URL to be added into InvertedIndex
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

	/**
	 * Converts the String input into URL form. If successful,
	 * adds the url input and the url of its contents into the index from URL input.
	 *
	 * @param input the URL in string form to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws MalformedURLException if URL could not be parsed
	 */
	public WebCrawler traverse(String input) throws MalformedURLException {
		return traverse(new URL(input));
	}


	/**
	 * Returns the InvertedIndex of which this Web Crawler is wrapping.
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
		 * @param url the url to be added into InvertedIndex
		 */
		CrawlingTask(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			String html = HtmlFetcher.fetch(url, redirects);

			if (html == null) {
				return;
			}

			/* 1. Placed this first in order for other workers to start immediately. */
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

			/* 2. Placed after so other crawling tasks can run. */
			InvertedIndex tempIndex = new InvertedIndex();

			/* Adds current url into tempIndex */
			addUrl(html, url.toString(), tempIndex);
			index.addAll(tempIndex); //Expensive in memory
			log.debug("Added tempIndex into index!");
		}
	}

}
