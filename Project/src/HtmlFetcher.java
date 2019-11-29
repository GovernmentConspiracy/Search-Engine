import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 */
public class HtmlFetcher {

	/**
	 * The logger of this class.
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * Do not instantiate.
	 */
	private HtmlFetcher() {
	}

	/**
	 * Returns {@code true} if and only if there is a "Content-Type" header and the
	 * first value of that header starts with the value "text/html" (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		String contentTypeKey = "Content-Type";
		String contentTypeValue = "text/html";

		return headers.containsKey(contentTypeKey) &&
				headers.get(contentTypeKey).get(0).toLowerCase().startsWith(contentTypeValue);
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		try {
			String statusRegex = "HTTPS?/1.\\d\\s+?(\\d+)\\s+?(?s:.+)";
			Matcher m = Pattern.compile(statusRegex).matcher(headers.get(null).get(0));
			if (m.find()) {
				return Integer.parseInt(m.group(1));
			}
		} catch (NumberFormatException nfe) {
			/* m.group(1) matches with digits, which means nfe should never occur. */
			log.error("ERROR: This is not supposed to happen. Digits unable to be parsed.");
			log.error(nfe.getMessage());
		}
		return -1;
	}

	/**
	 * Returns {@code true} if and only if the HTTP status code is between 300 and
	 * 399 (inclusive) and there is a "Location" header with at least one value.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isRedirect(Map<String, List<String>> headers) {
		return withinBounds(getStatusCode(headers), 300, 399);
	}

	private static boolean withinBounds(int value, int lower, int upper) {
		return lower <= value && value <= upper;
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
	 * code is 200 and the content type is HTML, returns the HTML as a single
	 * string. If the status code is a valid redirect, will follow that redirect if
	 * the number of redirects is greater than 0. Otherwise, returns {@code null}.
	 *
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 * resource is not html
	 * @see HttpsFetcher#openConnection(URL)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
	 * @see HttpsFetcher#getHeaderFields(BufferedReader)
	 * @see HttpsFetcher#getContent(BufferedReader)
	 * @see String#join(CharSequence, CharSequence...)
	 * @see #isHtml(Map)
	 * @see #isRedirect(Map)
	 */
	public static String fetch(URL url, int redirects) {
		if (redirects >= 0) {
			try {
				Map<String, List<String>> headers = HttpsFetcher.fetch(url);

				if (redirects > 0 && isRedirect(headers)) {
					return fetch(headers.get("Location").get(0), redirects - 1);
				}
				if (isHtml(headers) && getStatusCode(headers) == 200) {
					return String.join("\n", headers.get("Content"));
				}
			} catch (IOException ioe) {
				log.warn("fetch() could not open url:({}).", url);
			}
		}

		return null;
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)}.
	 *
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 * resource is not html
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url, int redirects) {
		try {
			return fetch(new URL(url), redirects);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 * resource is not html
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url) {
		return fetch(url, 0);
	}

	/**
	 * Calls {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 * resource is not html
	 */
	public static String fetch(URL url) {
		return fetch(url, 0);
	}
}
