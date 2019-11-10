import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

/**
 * A query which stores...
 *
 * @author Jason Liang
 * @version v2.0.1
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
				"{\n\twhere: \"%s\",\n\tcount: %d,\n\tscore: %s\n}"; //Want to use it in a certain context

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
				}
			}
			return temp;
		}

		@Override
		public String toString() {
			return this.toJSONObjectString(0);
		}

		@Override
		public void toJSONObject(Writer writer, int indent) throws IOException {
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
