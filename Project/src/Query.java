import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * A query which stores...
 *
 * @author Jason Liang
 * @version v2.0.0
 */
public class Query {
	private final Map<String, Set<SearchResult>> queryEntries; //TreeMap<String, TreeSet<SearchResult>>


	public Query() {
		queryEntries = new TreeMap<>();
	}

	public void addQuery(String word, String fileName, Long partial, Long total) {
		queryEntries.putIfAbsent(word, new TreeSet<>());
		queryEntries.get(word).add(new SearchResult(fileName, partial, (double) total / partial));
	}

	public void queryToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(queryEntries, output);
	}

	/**
	 * Search results
	 */
	public static class SearchResult implements Comparable<SearchResult> {
		//		private String search;
		private String where;
		private Long count;
		private Double score;

		private static final String FORMATTED =
				"{\n\twhere: \"%s\"\n\tcount: %d\n\tscore: %f\n}";

		public SearchResult(String where, Long count, Double score) {
			this.where = where;
			this.count = count;
			this.score = score;
		}

//		public SearchResult(String search, String where, Long count, Double score) {
//			this.search = search;
//			this.where = where;
//			this.count = count;
//			this.score = score;
//		}

//		public String getSearch() {
//			return search;
//		}

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
		public int compareTo(SearchResult other) {
			int temp;
			if ((temp = Double.compare(this.score, other.score)) == 0) {
				if ((temp = Long.compare(this.count, other.count)) == 0) {
					return this.where.compareTo(other.where);
//					if ((temp = this.where.compareTo(other.where)) == 0) {
//						return 0;
//					}
				}
			}
			return temp;
		}

		@Override
		public String toString() {
			return String.format(FORMATTED, where, count, score);
		}
	}
}
