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

	private final Map<String, Set<SearchResult>> queries; //TreeMap<String, TreeSet<SearchResult>>


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

	/**
	 * Search results
	 */
	public class SearchResult implements Comparable<SearchResult> {
		private String search;
		private String where;
		private Long count;
		private Double score;

		private static final String FORMATTED =
				"{\n\twhere: %s\n\tcount: %d\n\tscore: %f\n}";

		public SearchResult(String search, String where, Long count, Double score) {
			this.search = search;
			this.where = where;
			this.count = count;
			this.score = score;
		}

		@Override
		public int compareTo(SearchResult other) {
			int temp;
			if ((temp = Double.compare(this.score, other.score)) == 0) {
				if ((temp = Long.compare(this.count, other.count)) == 0) {
					if ((temp = this.where.compareTo(other.where)) == 0) {
						return 0;
					}
				}
			}
			return temp;
		}

		public String getSearch() {
			return search;
		}

		public String getWhere() {
			return where;
		}

		public Long getCount() {
			return count;
		}

		public Double getScore() {
			return score;
		}

		@Override
		public String toString() {
			return String.format(FORMATTED, where, count, score);
		}
	}
}
