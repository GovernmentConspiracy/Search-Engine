import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses URL links from the anchor tags within HTML text.
 */
public class LinkParser {

	/**
	 * String regex which finds anchored hyperlinks. Retrieve URL by calling group(1)
	 * <p>
	 * Process:
	 * 1. "<": Matches with char '<'
	 * 2. "(?i)": Ignores case sensitivity
	 * 3. "a": Matches with case-insensitive char 'a'
	 * 4. "\s+": Matches with one or more whitespaces between 'a' and whatever comes next
	 * 5. "(?:[^>]*?\s+)": (OPTIONAL FLAG) Finds the non-capturing group of whatever character,
	 * except for char '>'. Then gets the required whitespaces.
	 * 6. "?": Step 5 is optional
	 * 7. "href": Matches with case insensitive string "href"
	 * 8. "\s*": Optionally matches with whitespaces between step 7 and 9.
	 * 9. "=": Matches with char '='
	 * 10. See step 8.
	 * 11. "\"": Matches with char '"'
	 * 12. "([^\"]*?)": HTML group, groups all chars together until a '"' is matched.
	 * 13. See 11.
	 * 14. "(?:[^>]*)": (OPTIONAL FLAG) Finds the non-capturing group of whatever character,
	 * except for char '>'.
	 * 15. "?": Step 14 is optional.
	 * 16. ">": Matches with char '>', ending the regex.
	 */
	private static final String HTML_REGEX = "<(?i)a\\s+(?:[^>]*?\\s+)?href\\s*=\\s*\"([^\"]*)\"(?:[^>]*)?>";

	/**
	 * Hard-coded group
	 */
	private static final int HTML_REGEX_GROUP = 1;

	/**
	 * Removes the fragment component of a URL (if present), and properly encodes
	 * the query string (if necessary).
	 *
	 * @param url the url to clean
	 * @return cleaned url (or original url if any issues occurred)
	 */
	public static URL clean(URL url) {
		try {
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
					url.getPort(), url.getPath(), url.getQuery(), null).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return url;
		}
	}

	/**
	 * Creates a new URL the path is appended to the URL.
	 *
	 * @param url  the base url
	 * @param path the path to be appended
	 * @return a URL with appended path
	 */
	public static URL append(URL url, String path) {
		try {
			return new URL(url, path);
		} catch (MalformedURLException e) {
			return url;
		}
	}

	/**
	 * Returns a list of all the HTTP(S) links found in the href attribute of the
	 * anchor tags in the provided HTML. The links will be converted to absolute
	 * using the base URL and cleaned (removing fragments and encoding special
	 * characters as necessary).
	 *
	 * @param base the base url used to convert relative links to absolute3
	 * @param html the raw html associated with the base url
	 * @return cleaned list of all http(s) links in the order they were found
	 */
	public static ArrayList<URL> listLinks(URL base, String html) {
		return (ArrayList<URL>) getMatchedGroup(html, HTML_REGEX, HTML_REGEX_GROUP).stream()
				.map(e -> clean(append(base, e)))
				.collect(Collectors.toList());
	}

	/**
	 * Returns a list of all the matches found in the provided text.
	 *
	 * @param text       text to search in
	 * @param regex      regular expression to search for
	 * @param groupLevel the regex group to search for
	 * @return list of all matches found in text
	 * @see <a href=https://github.com/usf-cs212-fall2019/lectures/blob/master/Regular%20Expressions/src/RegexHelper.java>Taken from lecture code</a>
	 */
	public static ArrayList<String> getMatchedGroup(String text, String regex, int groupLevel) {
		ArrayList<String> matches = new ArrayList<>();

		Matcher m = Pattern.compile(regex).matcher(text);

		while (m.find()) {
			matches.add(m.group(groupLevel));
		}
		return matches;
	}
}
