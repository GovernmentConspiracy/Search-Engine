import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A query which stores...
 *
 * @author Jason Liang
 * @version v2.0.0
 */
public class Query {
	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	private final Map<String, Set<InvertedIndex.SearchResult>> queries; //TreeMap<String, TreeSet<SearchResult>>


	public Query() {
		queries = new TreeMap<>();
	}

	public void query(Path input) throws IOException {
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					queries.putIfAbsent(stemmer.stem(word).toString(), new TreeSet<>());
				}
			}
		}
	}

	public void addQuery() {

	}
}
