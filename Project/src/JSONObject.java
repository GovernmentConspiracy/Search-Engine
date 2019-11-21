import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * JSON writable status for classes which implement the the JSONObject interface.
 * <p>
 * This interface is used within SimpleJsonWriter, which calls the toJSONObject() method.
 */
interface JSONObject {
	/**
	 * Returns a String representing the JSONObject padded with {@code indent}
	 * count of tabs
	 *
	 * @param level the initial indent level, left padded
	 * @return a String of the JSONObject
	 */
	default String toJSONObjectString(int level) {
		try {
			StringWriter writer = new StringWriter();
			this.toJSONObject(writer, level);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes this object as a JSON object.
	 *
	 * @param writer the writer to use
	 * @param level  the initial indent level, left padded
	 * @throws IOException if file is not found
	 */
	void toJSONObject(Writer writer, int level) throws IOException;
}
