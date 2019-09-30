import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for parsing text in a consistent manner.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2019
 */
public final class TextParser {

	/**
	 * Do not instantiate.
	 */
	private TextParser() {
	}

	/**
	 * Regular expression that matches any whitespace.
	 **/
	private static final Pattern SPLIT_REGEX = Pattern.compile("(?U)\\p{Space}+");

	/**
	 * Regular expression that matches non-alphabetic characters.
	 **/
	private static final Pattern CLEAN_REGEX = Pattern.compile("(?U)[^\\p{Alpha}\\p{Space}]+");

	/**
	 * Cleans the text by removing any non-alphabetic characters (e.g. non-letters
	 * like digits, punctuation, symbols, and diacritical marks like the umlaut) and
	 * converting the remaining characters to lowercase.
	 *
	 * @param text the text to clean
	 * @return cleaned text
	 */
	private static String clean(String text) {
		String cleaned = Normalizer.normalize(text, Normalizer.Form.NFD);
		cleaned = CLEAN_REGEX.matcher(cleaned).replaceAll("");
		return cleaned.toLowerCase();
	}

	/**
	 * Splits the supplied text by whitespaces.
	 *
	 * @param text the text to split
	 * @return an array of {@link String} objects
	 */
	private static String[] split(String text) {
		return text.isBlank() ? new String[0] : SPLIT_REGEX.split(text.strip());
	}

	/**
	 * Cleans the text and then splits it by whitespace.
	 *
	 * @param text the text to clean and split
	 * @return an array of {@link String} objects
	 * @see #clean(String)
	 * @see #parse(String)
	 */
	public static String[] parse(String text) {
		return split(clean(text));
	}
}
