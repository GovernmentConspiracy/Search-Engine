import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A query which stores...
 *
 * @author Jason Liang
 * @version v2.0.0
 */
public class Query {
	private Map<String, Set<SearchResult>> queries;


	public Query() {
		queries = new TreeMap<>();
	}


	/**
	 * Search results
	 */
	public class SearchResult implements Comparable<SearchResult> {
		private String search;
		private String where;
		private Long count;
		private Double score;

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
	}
}
