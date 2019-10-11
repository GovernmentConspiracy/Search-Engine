import java.io.IOException;
import java.io.Writer;

public interface JSONObject {
	String toJSONObjectString(int indent);

	void toJSON(Writer writer, int indent) throws IOException;
}
