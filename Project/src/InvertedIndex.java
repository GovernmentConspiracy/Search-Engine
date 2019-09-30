import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// TODO Try to separate file parsing and directory traversing into its own "builder" class. have an addFile(Path path, InvertedIndex index) and maybe a traverse(Path path, InvertedIndex index)
// TODO Ask how to do that: Attempted to place it in

/**
 * An index to store words and the location (both file location and position in file) of where those words were found.
 * Auxiliary functions includes word counter and JSON writer
 *
 * @author Jason Liang
 * @version v1.0.4
 */
public class InvertedIndex {
	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;


	/**
	 * Nested data structure used to store location of where a word was found.
	 * Outer map stores (key - value) as (word - file location)
	 * Inner map stores (key - value) as (file location - position in file)
	 */
	private final Map<String, Map<String, Set<Integer>>> indexMap; //String1 = word, String2 = Path, Set1 = Location

	/**
	 * Nested data structure used to store word count of a file
	 * Map stores (key - value) as (file location - number count)
	 */
	private final Map<String, Long> countMap;


	/**
	 * Constructs a new empty inverted index and can pass in acceptable file extensions
	 */
	public InvertedIndex() {
		this.indexMap = new TreeMap<>();
		this.countMap = new TreeMap<>();
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
	 * Generates word - path - location pairs onto a nested map structure,
	 * storing where a stemmed word was found in a file and position
	 *
	 * @param input The file path which populates {@code indexMap}
	 * @see #indexMap
	 */
	public void index(Path input) {
		List<Path> paths = getFilesOrEmpty(input);
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		for (Path in : paths) {
			try (
					BufferedReader reader = Files.newBufferedReader(in, StandardCharsets.UTF_8)
			) {
				String line;
				int i = 0;
				while ((line = reader.readLine()) != null) {
					for (String word : TextParser.parse(line)) {
						indexPut(stemmer.stem(word).toString(), in.toString(), ++i);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Helper method used to store word, pathString, and location into indexMap. See usages below
	 *
	 * @param word       A word made from a stemmed string. Used as the outer map key
	 * @param pathString A string representing the path where {@code word} in string form. Used as inner map key
	 * @param location   An int representing the location of where {@code word} was found in {@code pathString}
	 * @see #index(Path)
	 */
	private void indexPut(String word, String pathString, int location) {
		indexMap.putIfAbsent(word, new TreeMap<>());
		indexMap.get(word).putIfAbsent(pathString, new TreeSet<>());
		indexMap.get(word).get(pathString).add(location);
	}

	/**
	 * Generates a JSON text file of the inverted index, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 * @see #index(Path)
	 */
	public void indexToJSON(Path output) throws IOException {
		mapToJSON(indexMap, output);
	}

	/**
	 * Generates a JSON text file of the inverted index, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @return {@code true} if successful in creating a JSON file
	 * @see #index(Path)
	 * @see #indexToJSON(Path)
	 */
	public boolean indexToJSONSafe(Path output) {
		try {
			indexToJSON(output);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Populates {@code countMap} with text files of Path input
	 *
	 * @param input The file path which populates {@code countMap}
	 * @see #countMap
	 */
	public void count(Path input) { //TODO: Efficient counter will iterate through the pre-made inverse index
		List<Path> paths = getFilesOrEmpty(input);
		for (Path in : paths) {
			try (
					BufferedReader reader = Files.newBufferedReader(in, StandardCharsets.UTF_8)
			) {
				long count = reader.lines()
						.flatMap(line -> Arrays.stream(TextParser.parse(line)))
						.count();
				if (count > 0)
					countMap.put(in.toString(), count);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 * @see #count(Path)
	 */
	public void countToJSON(Path output) throws IOException {
		mapToJSON(countMap, output);
	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @return {@code true} if successful in creating a JSON file
	 * @see #index(Path)
	 * @see #indexToJSON(Path)
	 */
	public boolean countToJSONSafe(Path output) {
		try {
			countToJSON(output);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Helper method which creates a JSON file
	 *
	 * @param map    A map with key string to be converted as a generic object
	 * @param output The output path of the JSON file
	 * @throws IOException if the output file could not be created or written
	 * @see #index(Path)
	 * @see #count(Path)
	 */
	private void mapToJSON(Map<String, ?> map, Path output) throws IOException {
		SimpleJsonWriter.asObject(map, output);
//        } catch (IOException e) {
//            System.err.printf("Could not write into Path \"%s\"\n", output.toString());
//            System.err.println(e.getMessage());
//        }
	}
}
