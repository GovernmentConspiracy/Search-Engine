import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvertedIndex {
    private final Map<String, Map<String, Set<Integer>>> behemoth; //String1 = word, String2 = Path, Set1 = Location

    public InvertedIndex() {
        behemoth = new TreeMap<>() {
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

    /*
     * public static boolean forceDelete(File path) {
     *         if (path.exists() && path.isDirectory()) {
     *             File[] files = path.listFiles();
     *             for (int i = 0; i < files.length; i++) {
     *                 if (files[i].isDirectory()) {
     *                     forceDelete(files[i]);
     *                 }
     *                 else
     *                     files[i].delete();
     *             }
     *         }
     *         return (path.delete());
     *     }
     */

    /**
     *
     * @param output
     * @throws IOException
     */
    public void mapToFile(Path output) throws IOException{
        try (
                BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8);
        ) {



        }

    }


}
