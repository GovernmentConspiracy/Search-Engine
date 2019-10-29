import java.io.IOException;
import java.io.Writer;

public interface JSONObject {
	String toJSONObjectString(int indent);

	void toJSONObject(Writer writer, int indent) throws IOException;

	static void toJSONObject(Writer writer, int indent, String formatted, Object... args) {
		//TODO use formatter to place tabs at the start and on every new line
	}
}
