import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

/*
 * TODO

Since the SearchResult data depends on a specific instance of a specific inverted index,
it should be an inner class inside of inverted index. (Not a static nested class.)

Need to make the SearchResult class mutable. So where you used to store Map<String, Long>
you are going to store a Map<String (location?), SearchResult> instead and update the count within that object.

SearchResult will still have a where, count, score and get methods. It needs a new method:

private void update(String word) {
	this.count += indexMap.get(word).get(where).size();
	this.score = (double) this.count / countMap.get(where);
}

Combine Query INTO SearchBuilder.

This is SearchBuilder:

private final Map<String, List<InvertedIndex.SearchResult>> queryEntries;
private final InvertedIndex index;

public void queryToJSON(Path output) throws IOException

public void parseQueries(Path input, boolean exact) throws IOException {
		open the file, read line by line, and on each line call the other parseQueries
}

public void parseQueries(String line, boolean exact) {
	Set<String> usedPhrases = ... filled up by parse and stemming the line
	String lineFinal = String.join(" ", usedPhrases);
	queryEntries.put(lineFinal, index.search(usedPhrases, exact));
}
 */

/**
 * A query to store phrases and each word count of the phrase of where those words were found.
 *
 * @author Jason Liang
 * @version v2.0.1
 */
public class Query {

	/**
	 * Nested data structure used to store count of where a word was found.
	 */
	private final Map<String, Set<SearchResult>> queryEntries; //TreeMap<String, TreeSet<SearchResult>>

	/**
	 * Constructs a new empty query.
	 */
	public Query() {
		queryEntries = new TreeMap<>();
	}

	/**
	 * Adds a search result to the set mapped to the provided phrase.
	 *
	 * @param phrase   The search phrase
	 * @param pathName The path of where a specified word of the search phrase was found
	 * @param partial  The word count of the specified word of the search phrase in the path
	 * @param total    The total word count of the path
	 */
	public void addQuery(String phrase, String pathName, Long partial, Long total) {
		addEmptyQuery(phrase);
		queryEntries.get(phrase).add(new SearchResult(pathName, partial, (double) partial / total));
	}

	/**
	 * Adds an empty set of search result into the query.
	 * (Not Collections.emptySet(), so more expensive);
	 *
	 * @param phrase The search phrase
	 */
	public void addEmptyQuery(String phrase) {
		queryEntries.putIfAbsent(phrase, new TreeSet<>());
	}

	/**
	 * Generates a JSON text file of the search result of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 */
	public void queryToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(queryEntries, output);
	}

	/**
	 * A search result to store file location, word count, and word occurrence ratio.
	 */
	public static class SearchResult implements Comparable<SearchResult>, JSONObject {
		/**
		 * The String representation of a file location of where the word was found.
		 */
		private String where;

		/**
		 * The number of occurrence of a certain word in a file.
		 */
		private long count;

		/**
		 * Word occurrence ratio to total word count of a file.
		 */
		private double score;

		/**
		 * Constructs a new SearchResult,
		 * storing the file location, word count, and score.
		 *
		 * @param where String representing the file location
		 * @param count long representing the word count
		 * @param score double representing the ratio between count of a particular word and total word count
		 */
		public SearchResult(String where, long count, double score) {
			this.where = where;
			this.count = count;
			this.score = score;
		}

		/**
		 * Returns the String representation of a file location of where the word was found.
		 *
		 * @return String representation of a path
		 */
		public String getWhere() {
			return where;
		}

		/**
		 * Returns the number of occurrence of a certain word in a file.
		 *
		 * @return count of a particular String
		 */
		public long getCount() {
			return count;
		}

		/**
		 * Returns the ratio between word count of a particular String and total word count
		 *
		 * @return (word count) / (total word count)
		 */
		public double getScore() {
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
		public void toJSONObject(Writer writer, int level) throws IOException {
			String scoreFormat = "%.8f";

			SimpleJsonWriter.indent(writer, level);
			writer.write("{\n");
			SimpleJsonWriter.indent(writer, level + 1);
			writer.write("\"where\": ");
			SimpleJsonWriter.quote(where, writer);
			writer.write(",\n");
			SimpleJsonWriter.indent(writer, level + 1);
			writer.write("\"count\": ");
			writer.write(Long.toString(count));
			writer.write(",\n");
			SimpleJsonWriter.indent(writer, level + 1);
			writer.write("\"score\": ");
			writer.write(String.format(scoreFormat, score));
			writer.write('\n');
			SimpleJsonWriter.indent(writer, level);
			writer.write('}');
		}
	}
}
