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
	 * @throws IOException if file is not found
	 * @see #asDependentGenericArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<?> elements, Writer writer, int level) throws IOException {
		indent(writer, level);
		asDependentGenericArray(elements, writer, level);
		writer.write('\n');
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if file is not found
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<?> elements, Path path) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<?> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
//            System.out.println("Could not generate a String");
			return null;
		}
	}

	/**
	 * Helper method for writing arrays as nested or single JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if file is not found
	 * @see #asArray(Collection, Writer, int)
	 */
	private static void asDependentGenericArray(Collection<?> elements, Writer writer, int level) throws IOException {
		writer.write('[');
		Iterator<?> elemIterator = elements.iterator();

		if (elemIterator.hasNext()) {
			writer.write('\n');
			indent(elemIterator.next().toString(), writer, level + 1);
		}

		while (elemIterator.hasNext()) {
			writer.write(',');
			writer.write('\n');
			indent(elemIterator.next().toString(), writer, level + 1);
		}

		writer.write('\n');

		indent("]", writer, level);
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
		asDependentGenericObject(elements, writer, level);
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
	private static void asDependentGenericObject(Set<? extends Map.Entry<?, ?>> elements, Writer writer, int level) throws IOException {
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
	private static void asDependentGenericObject(Map<?, ?> elements, Writer writer, int level) throws IOException {
		asDependentGenericObject(elements.entrySet(), writer, level);
	}

	/**
	 * Helper method for writing an element with its object name and value
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the initial indent level
	 * @throws IOException if file is not found
	 */
	private static void asObjectVariable(Map.Entry<?, ?> element, Writer writer, int level) throws IOException {
		quote(element.getKey().toString(), writer, level + 1);
		writer.write(": ");
		var type = element.getValue();
		//Why is there no switch case for this??
		if (type instanceof Collection<?>) {
			asDependentGenericArray((Collection<?>) type, writer, level + 1);
		} else if (type instanceof Map<?, ?>) {
			asDependentGenericObject((Map<?, ?>) type, writer, level + 1);
		} else {
			writer.write(type.toString());
		}
	}

	/**
	 * Writes the {@code \t} tab symbol by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException if file is not found
	 */
	private static void indent(Writer writer, int times) throws IOException {
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
	private static void indent(String element, Writer writer, int times) throws IOException {
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
	private static void quote(String element, Writer writer) throws IOException {
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
	private static void quote(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		quote(element, writer);
	}
}
