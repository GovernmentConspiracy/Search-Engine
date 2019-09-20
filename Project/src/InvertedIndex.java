import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InvertedIndex {
    private final Map<String, Map<String, Set<Integer>>> map; //String1 = word, String2 = Path, Set1 = Location

    public InvertedIndex() {
        map = new TreeMap<>() {
            @Override
            public Set<String> keySet() {
                return Collections.unmodifiableSet(super.keySet());
            }
            @Override
            public Collection<Map<String, Set<Integer>>> values() {
                return Collections.unmodifiableCollection(super.values());
            }
            @Override
            public Set<Map.Entry<String, Map<String, Set<Integer>>>> entrySet() {
                return Collections.unmodifiableSet(super.entrySet());
            }
        };
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
    private static void getFiles(List<Path> paths, Path input) throws IOException {
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
     * Generates a JSON text file to store output
     * @param output The output path to store the JSON object
     * @throws IOException
     */
    public void mapToJSON(Path output) throws IOException{
        SimpleJsonWriter.asGenericObject(map, output);
    }


}
