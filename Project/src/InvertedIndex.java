import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An index to store words and the location (both file location and position in file) of where those words were found.
 * Auxiliary functions includes word counter and JSON writer
 *
 * @author Jason Liang
 * @version v1.0.1
 */
public class InvertedIndex {
    /**
     * Default SnowballStemmer algorithm from OpenNLP.
     */
    public static SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

    /**
     * String array representing the default acceptable file extensions.
     */
    private static final String[] DEFAULT_ACCEPTABLE_FILES = {".txt", ".text"};

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
     * String array representing acceptable file extensions. Default is set to DEFAULT_ACCEPTABLE_FILES
     * @see #DEFAULT_ACCEPTABLE_FILES
     */
    private final String[] acceptableFileExtensions;

    /**
     * Constructs a new empty inverted index and can pass in acceptable file extensions
     *
     * @param acceptableFileExtensions array of file extensions which are acceptable to read
     */
    public InvertedIndex(String... acceptableFileExtensions) {
        indexMap = new TreeMap<>();
        countMap = new TreeMap<>();
        this.acceptableFileExtensions = acceptableFileExtensions;
        //Arrays.stream(this.acceptableFileExtensions).forEach(System.out::println);
    }

    /**
     * Constructs a new empty inverted index with default file extensions
     *
     * @see #InvertedIndex(String...)
     */
    public InvertedIndex() {
        this(DEFAULT_ACCEPTABLE_FILES);
    }

    /**
     * Generates all Paths of the input path recursively.
     *
     * @param input The root directory or text tile
     * @return A list of paths of the entire directory of {@code input} or 0-1 text file(s)
     * @throws IOException if {@code void getFiles()} failed (i.e {@code input} was wrong)
     *
     * @see #getFiles(List, Path)
     */
    public static List<Path> getFiles(Path input) throws IOException {
        List<Path> paths = new ArrayList<>();
        getFiles(paths, input);
        return paths;
    }

    /**
     * Helper method for getFiles() to generate Path to List of Paths
     *
     * @param paths Parameter being edited
     * @param input The current path, either directory or file
     * @throws IOException if the stream could not be made (i.e {@code input} was wrong)
     * @see #getFiles(Path)
     */
    private static void getFiles(List<Path> paths, Path input) throws IOException { //FileVistor
        if (Files.exists(input)) {
            if (Files.isDirectory(input)) {
                try (
                    DirectoryStream<Path> stream = Files.newDirectoryStream(input)
                ) {
                    for (Path path: stream) {
                        getFiles(paths, path);
                    }
                }
            } else {
                paths.add(input);
            }
        }
    }

    /**
     * Generates word - path - location pairs onto a nested map structure,
     * storing where a stemmed word was found in a file and position
     *
     * @param input The file path which populates {@code indexMap}
     *
     * @see #indexMap
     */
    public void index(Path input) {
        List<Path> paths = null;
        try {
            paths = getFiles(input);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
        for (Path in: paths) {
            if (isCorrectExtension(in)) {
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
    }

    /**
     * Helper method used to store word, pathString, and location into indexMap. See usages below
     *
     * @param word A word made from a stemmed string. Used as the outer map key
     * @param pathString A string representing the path where {@code word} in string form. Used as inner map key
     * @param location An int representing the location of where {@code word} was found in {@code pathString}
     *
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
     *
     * @see #count(Path)
     */
    public void indexToJSON(Path output) {
        mapToJSON(indexMap, output);
    }

    /**
     * Populates {@code countMap} with text files of Path input
     *
     * @param input The file path which populates {@code countMap}
     * @see #countMap
     */
    public void count(Path input) { //TODO: Efficient counter will iterate through the pre-made inverse index
        List<Path> paths = null;
        try {
             paths = getFiles(input);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
//        Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
        for (Path in: paths) {
            if (isCorrectExtension(in)) {
                try (
                        BufferedReader reader = Files.newBufferedReader(in, StandardCharsets.UTF_8)
                ) {
                    long count =
                            reader.lines()
                                    .flatMap(line -> Arrays.stream(TextParser.parse(line)))
//                            .map(line -> stemmer.stem(line).toString()) //Not necessary for counting
                                    .count();
                    if (count > 0)
                        countMap.put(in.toString(), count);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Generates a JSON text file of the count of words, stored at Path output
     *
     * @param output The output path to store the JSON object
     *
     * @see #count(Path)
     */
    public void countToJSON(Path output) {
        mapToJSON(countMap, output);
    }

    /**
     * Helper method which creates a JSON file
     *
     * @param map A map with key string to be converted as a generic object
     * @param output The output path of the JSON file
     *
     * @see #index(Path)
     * @see #count(Path)
     */
    private void mapToJSON(Map<String, ?> map, Path output) {
        try {
            SimpleJsonWriter.asGenericObject(map, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The jankiest file extension checker. I'm so sorry
     *
     * @param input A non-directory file path
     * @return {@code true} if acceptableFileExtensions is empty or if path Input is one of the extensions
     *
     * @see #index(Path)
     */
    private boolean isCorrectExtension(Path input) {
        if (acceptableFileExtensions == null || acceptableFileExtensions.length == 0) {
            return true;
        }
        //trims input string, leaving index of last '/' till the end
        String inputString = input.toString();
        int lastSlash = Math.max(inputString.lastIndexOf('/'), 0);
        inputString = inputString.substring(lastSlash).toLowerCase();
//        System.out.println(inputString);
        for (String extension : acceptableFileExtensions) {
            int i = inputString.lastIndexOf(extension);
            if (i == inputString.length() - extension.length()) {
                return true;
            }
        }
//        System.out.printf("Failed %s\n\n", inputString);
        return false;
    }


}
