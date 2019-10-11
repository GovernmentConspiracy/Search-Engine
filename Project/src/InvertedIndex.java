import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An index to store words and the location (both file location and position in file) of where those words were found.
 * <p>Auxiliary functions includes word counter and JSON writer
 *
 * @author Jason Liang
 * @version v1.1.0
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
	private final Map<String, Map<String, Set<Long>>> indexMap; //String1 = word, String2 = Path, Set1 = Location

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
	 * Generates word - path - location pairs onto a nested map structure,
	 * storing where a stemmed word was found in a file and position
	 *
	 * @param input The file path which populates {@code indexMap}
	 * @throws IOException if path input could not be read
	 * @see #indexMap
	 */
	public void index(Path input) throws IOException {
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			long i = 0;
			while ((line = reader.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					indexPut(stemmer.stem(word).toString(), input.toString(), ++i);
				}
			}
			if (i > 0)
				countMap.put(input.toString(), i); //writes into count map
		}

	}

	/**
	 * Helper method used to store word, pathString, and location into indexMap. See usages below
	 *
	 * @param word       A word made from a stemmed string. Used as the outer map key
	 * @param pathString A string representing the path where {@code word} in string form. Used as inner map key
	 * @param location   An long representing the location of where {@code word} was found in {@code pathString}
	 * @see #index(Path)
	 */
	private void indexPut(String word, String pathString, long location) {
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
			//TODO logger
			return false;
		}
		return true;
	}

	public Map<String, Long> getWordFileCount(String word, boolean exact) {
		if (exact)
			return getExactWordFileCount(word);
		return getPartialWordFileCount(word);
	}

	private Map<String, Long> getExactWordFileCount(String word) {
		var map = indexMap.get(word);
		if (map != null) {
			return map.entrySet()
					.stream()
					.collect(
							Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> (long) e.getValue().size())
					);
		}
		return Collections.emptyMap();
	}

	//TODO Fix this bad boy
	private Map<String, Long> getPartialWordFileCount(String word) {
		if (!indexMap.isEmpty()) {
			return indexMap.entrySet().stream()
					.filter(e -> e.getKey().startsWith(word))
					.map(Map.Entry::getValue)
					.flatMap(e -> e.entrySet().stream())
					.collect(
							Collectors.toUnmodifiableMap(
									Map.Entry::getKey, e -> (long) e.getValue().size(), Long::sum //resolve duplicates with merge
							)
					);
		}
		return Collections.emptyMap();
	}

	public Map<String, Long> getCounts() {
		return Collections.unmodifiableMap(countMap);
	}
//
//	/**
//	 * DEPRECIATED since v1.1.0
//	 * Populates {@code countMap} with text files of Path input
//	 *
//	 * @param input The file path which populates {@code countMap}
//	 * @throws IOException if path input could not be read
//	 * @see #countMap
//	 */
//	public void countIfEmpty(Path input) throws IOException { //Note: Efficient counter will iterate through the pre-made inverse index
//		List<Path> paths = InvertedIndexBuilder.getFiles(input); //Nasty change
//		for (Path in : paths) {
//			try (
//					BufferedReader reader = Files.newBufferedReader(in, StandardCharsets.UTF_8)
//			) {
//				long count = reader.lines()
//						.flatMap(line -> Arrays.stream(TextParser.parse(line)))
//						.count();
//				if (count > 0)
//					countMap.put(in.toString(), count);
//			}
//		}
//	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 * @see #index(Path)
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
			//TODO logger
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
	 */
	private void mapToJSON(Map<String, ?> map, Path output) throws IOException {
		SimpleJsonWriter.asObject(map, output);
//        } catch (IOException e) {
//            System.err.printf("Could not write into Path \"%s\"\n", output.toString());
//            System.err.println(e.getMessage());
//        }
	}


}
