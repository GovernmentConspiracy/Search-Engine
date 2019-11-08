import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * JSON writable status for classes which implement the the JSONObject interface.
 * <p>
 * This interface is used within SimpleJsonWriter, which calls the toJSONObject() method.
 */
public interface JSONObject {
	default String toJSONObjectString(int indent) {
		try {
			StringWriter writer = new StringWriter();
			this.toJSONObject(writer, indent);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	void toJSONObject(Writer writer, int indent) throws IOException;
}
