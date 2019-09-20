import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InvertedIndex {
    public void fileTransversal() {

    }

    public void indexToFile(Path output) throws IOException{
        try (
                BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8);
        ) {



        }

    }


}
