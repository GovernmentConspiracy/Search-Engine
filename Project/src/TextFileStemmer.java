import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class for parsing and stemming text and text files into sets of
 * stemmed words.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2019
 *
 * @see TextParser
 */
@SuppressWarnings("WeakerAccess")
public class TextFileStemmer {

	/** The default stemmer algorithm used by this class. */
	private static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #DEFAULT
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static TreeSet<String> uniqueStems(String line) {
		// THIS IS PROVIDED FOR YOU; NO NEED TO MODIFY
		return uniqueStems(line, new SnowballStemmer(DEFAULT));
	}

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
		TreeSet<String> stems = new TreeSet<>();

		String[] words = TextParser.parse(line);
		for (String word: words) {
			stems.add((String)stemmer.stem(word)); //Nasty casting
		}
		return stems;
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 *
	 * @param inputFile the input file to parse
	 * @return a sorted set of stems from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #uniqueStems(String)
	 * @see TextParser#parse(String)
	 */
	public static TreeSet<String> uniqueStems(Path inputFile) throws IOException {
		TreeSet<String> stems = new TreeSet<>();
		try (
				BufferedReader reader = Files.newBufferedReader(inputFile, UTF_8)
			) {
			String line;
			Stemmer stemmer = new SnowballStemmer(DEFAULT);

			while ((line = reader.readLine()) != null) {
				stems.addAll(uniqueStems(line, stemmer)); //Inefficient if there are duplicates in each line
			}
		}
		return stems;
	}

	/**
	 * A simple main method that demonstrates this class.
	 *
	 * @param args unused
	 * @throws IOException if unable to read or parse file
	 */
	public static void main(String[] args) throws IOException {
		// What even is a stemmer?
		String text = "practic practical practice practiced practicer practices "
				+ "practicing practis practisants practise practised practiser "
				+ "practisers practises practising practitioner practitioners";

		System.out.println(uniqueStems(text));

		Path inputPath = Paths.get("test", "animals.text");
		//Path inputPath = Paths.get("test", "rfc475.txt");
		Set<String> actual = TextFileStemmer.uniqueStems(inputPath);
		System.out.println(actual);
	}
}
