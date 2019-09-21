import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InvertedIndex {
    private final Map<String, Map<String, Set<Integer>>> indexMap; //String1 = word, String2 = Path, Set1 = Location
    private final Map<String, Long> counter;
    public static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;
    /**
     * String array representing the default acceptable file extensions
     */
    public static final String[] DEFAULT_ACCEPTABLE_FILES = {".txt", ".text"};
    private final String[] acceptableFileExtensions;

    public InvertedIndex(String... acceptableFileExtensions) {
        indexMap = new TreeMap<>();
        counter = new TreeMap<>();
        this.acceptableFileExtensions = acceptableFileExtensions;
        //Arrays.stream(this.acceptableFileExtensions).forEach(System.out::println);
    }

    public InvertedIndex() {
        this(DEFAULT_ACCEPTABLE_FILES);
    }

    public static List<Path> getFiles(Path input) throws IOException {
        List<Path> paths = new ArrayList<>();
        getFiles(paths, input);
        return paths;
    }

    /**
     * Helper method for getFiles() to generate Path to List of Paths
     * @param paths Parameter being edited
     * @param input The current path, either directory or file
     * @throws IOException
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
                            indexPlace(stemmer.stem(word).toString(), in.toString(), ++i);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void indexPlace(String word, String pathString, int location) {
        indexMap.putIfAbsent(word, new TreeMap<>());
        indexMap.get(word).putIfAbsent(pathString, new TreeSet<>());
        indexMap.get(word).get(pathString).add(location);
    }

    /**
     * Generates a JSON text file to store output
     * @param output The output path to store the JSON object
     * @throws IOException
     */
    public void indexToJSON(Path output) {
        mapToJSON(indexMap, output);
    }

    /**
     *
     * @param input
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
                    counter.put(in.toString(),
                            reader.lines()
                                    .flatMap(line -> Arrays.stream(TextParser.parse(line)))
//                            .map(line -> stemmer.stem(line).toString()) //Not necessary for counting
                                    .count()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void countToJSON(Path output) {
        mapToJSON(counter, output);
    }

    private void mapToJSON(Map<String, ?> map, Path output) {
        try {
            SimpleJsonWriter.asGenericObject(map, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void test(Path input) {
//        input.
//    }

    /**
     * The jankiest file extension checker. I'm so sorry
     * @param input
     * @return
     */
    private boolean isCorrectExtension(Path input) {
        if (acceptableFileExtensions == null || acceptableFileExtensions.length == 0) {
            return true;
        }
        //trims input string, leaving index of last '/' till the end
        String inputString = input.toString();
        inputString = inputString.substring(inputString.lastIndexOf('/')).toLowerCase();
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
