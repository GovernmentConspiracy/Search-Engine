import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Outputs several simple data structures in "pretty" JSON format where
 * newlines are used to separate elements and nested elements are indented.
 * <p>
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2019
 */
public final class SimpleJsonWriter {
	/**
	 * The logger of this class
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * Do not instantiate.
	 */
	private SimpleJsonWriter() {
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @param <V>      the element type stored in elements
	 * @throws IOException if file is not found
	 * @see #asDependentArray(Collection, Writer, int)
	 */
	public static <V> void asArray(Collection<V> elements, Writer writer, int level) throws IOException {
		indent(writer, level);
		asDependentArray(elements, writer, level);
		writer.write('\n');
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @param <V>      the element type stored in elements
	 * @throws IOException if file is not found
	 * @see #asArray(Collection, Writer, int)
	 */
	public static <V> void asArray(Collection<V> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @param <V>      the element type stored in elements
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @see #asArray(Collection, Writer, int)
	 */
	public static <V> String asArray(Collection<V> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			log.warn("Could not write JSON array");
			return null;
		}
	}

	/**
	 * Helper method for writing arrays as nested or single JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @param <V>      the element type stored in elements
	 * @throws IOException if file is not found
	 * @see #asArray(Collection, Writer, int)
	 */
	private static <V> void asDependentArray(Collection<V> elements, Writer writer, int level) throws IOException {
		writer.write('[');
		Iterator<?> elemIterator = elements.iterator();

		if (elemIterator.hasNext()) {
			writer.write('\n');
			asArrayVariable(elemIterator.next(), writer, level);
		}

		while (elemIterator.hasNext()) {
			writer.write(',');
			writer.write('\n');
			asArrayVariable(elemIterator.next(), writer, level);
		}

		writer.write('\n');

		indent("]", writer, level);
	}

	/**
	 * Helper method for writing an element's value
	 *
	 * @param element the element to be written
	 * @param writer  the writer to use
	 * @param level   the initial intent level
	 * @param <V>     the element type
	 * @throws IOException if file is not found
	 * @see #asVariable(Object, Writer, int)
	 */
	private static <V> void asArrayVariable(V element, Writer writer, int level) throws IOException {
		indent(writer, level + 1);
		asVariable(element, writer, level);
	}

	/**
	 * Returns the elements as a generic pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<?, ?> elements) {
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			log.warn("Could not write JSON object");
			return null;
		}
	}

	/**
	 * Writes the elements as a generic pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if file is not found
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<?, ?> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a generic pretty JSON object. The generic notation used
	 * allows this method to be used for any type of map with any type of nested
	 * collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if file is not found
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<?, ?> elements, Writer writer, int level) throws IOException {
		indent(writer, level);
		asDependentObject(elements, writer, level);
		writer.write('\n');
	}

	/**
	 * Helper method for writing the elements as either a normal or nested pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if file is not found
	 * @see #asObject(Map, Writer, int)
	 */
	private static void asDependentObject(Set<? extends Map.Entry<?, ?>> elements, Writer writer, int level) throws IOException {
		var elemIterator = elements.iterator();
		writer.write('{');

		if (elemIterator.hasNext()) {
			writer.write('\n');
			asObjectVariable(elemIterator.next(), writer, level);
		}

		while (elemIterator.hasNext()) {
			writer.write(',');
			writer.write('\n');
			asObjectVariable(elemIterator.next(), writer, level);
		}

		writer.write('\n');

		indent("}", writer, level);

	}

	/**
	 * Helper method for writing the elements as either a normal or nested pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if file is not found
	 */
	private static void asDependentObject(Map<?, ?> elements, Writer writer, int level) throws IOException {
		asDependentObject(elements.entrySet(), writer, level);
	}

	/**
	 * Helper method for writing an element with its object name and value
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the initial indent level
	 * @throws IOException if file is not found
	 * @see #asVariable(Object, Writer, int)
	 */
	private static void asObjectVariable(Map.Entry<?, ?> element, Writer writer, int level) throws IOException {
		quote(element.getKey().toString(), writer, level + 1);
		writer.write(": ");
		asVariable(element.getValue(), writer, level);
	}

	/**
	 * Helper method for writing an element's value
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the initial indent level
	 * @param <V>     the element's type
	 * @throws IOException if file is not found
	 */
	private static <V> void asVariable(V element, Writer writer, int level) throws IOException {
		if (element instanceof Map<?, ?>) {
			asDependentObject((Map<?, ?>) element, writer, level + 1);
		} else if (element instanceof Collection<?>) {
			asDependentArray((Collection<?>) element, writer, level + 1);
		} else if (element instanceof String) {
			quote(element.toString(), writer);
		} else if (element instanceof JSONObject) {
			((JSONObject) element).toJSONObject(writer, level);
		} else {
			writer.write(element.toString());
		}
	}

	/**
	 * Writes the {@code \t} tab symbol by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException if file is not found
	 */
	public static void indent(Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if file is not found
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @throws IOException if file is not found
	 */
	public static void quote(String element, Writer writer) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Indents and then writes the element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if file is not found
	 * @see #indent(Writer, int)
	 * @see #quote(String, Writer)
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		quote(element, writer);
	}
}
