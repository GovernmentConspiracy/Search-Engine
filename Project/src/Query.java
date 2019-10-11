import java.io.IOException;
import java.io.Writer;
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
		queryEntries.get(word).add(new SearchResult(fileName, partial, (double) partial / total));
	}

	public void addEmptyQuery(String word) {
		queryEntries.putIfAbsent(word, Collections.emptySet());
	}

	public void queryToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(queryEntries, output);
	}

	/**
	 * Search results
	 */
	public static class SearchResult implements Comparable<SearchResult>, JSONObject {

		private String where;
		private Long count;
		private Double score;

		private static String SCORE_FORMAT = "%.8f";
		private static final String FORMATTED =
				"{\n\twhere: \"%s\",\n\tcount: %d,\n\tscore: %s\n}";

		public SearchResult(String where, Long count, Double score) {
			this.where = where;
			this.count = count;
			this.score = score;
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
		public int compareTo(SearchResult other) {
			int temp;
			if ((temp = -Double.compare(this.score, other.score)) == 0) {
				if ((temp = -Long.compare(this.count, other.count)) == 0) {
					return this.where.compareToIgnoreCase(other.where);
//					if ((temp = this.where.compareToIgnoreCase(other.where)) == 0) {
//						return 0;
//					}
				}
			}
			return temp;
		}

		@Override
		public String toString() {
			return String.format(FORMATTED, where, count, String.format(SCORE_FORMAT, score));
		}

		public String toJSONObjectString(int indent) {
			StringBuilder str = new StringBuilder();
			for (int i = 1; i <= indent; i++) {
				str.append('\t');
			}
			str.append("{\n");
			for (int i = 1; i <= indent + 1; i++) {
				str.append('\t');
			}
			str.append("\"where\": ").append('\"').append(where).append("\",\n");
			for (int i = 1; i <= indent + 1; i++) {
				str.append('\t');
			}
			str.append("\"count\": ").append(count).append(",\n");
			for (int i = 1; i <= indent + 1; i++) {
				str.append('\t');
			}
			str.append("\"score\": ").append(String.format(SCORE_FORMAT, score)).append('\n');
			for (int i = 1; i <= indent; i++) {
				str.append('\t');
			}
			str.append('}');
			return str.toString();
		}

		@Override
		public void toJSON(Writer writer, int indent) throws IOException {
			SimpleJsonWriter.indent(writer, indent);
			writer.write("{\n");
			SimpleJsonWriter.indent(writer, indent + 1);
			writer.write("\"where\": ");
			SimpleJsonWriter.quote(where, writer);
			writer.write(",\n");
			SimpleJsonWriter.indent(writer, indent + 1);
			writer.write("\"count\": ");
			writer.write(count.toString());
			writer.write(",\n");
			SimpleJsonWriter.indent(writer, indent + 1);
			writer.write("\"score\": ");
			writer.write(String.format(SCORE_FORMAT, score));
			writer.write('\n');
			SimpleJsonWriter.indent(writer, indent);
			writer.write('}');
		}


	}
}
