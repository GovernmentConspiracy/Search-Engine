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
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2019
 */
@SuppressWarnings("WeakerAccess")
public class SimpleJsonWriter {

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if file is not found
	 *
	 * @see #asDependentGenericArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
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
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
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
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
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
	 *
	 * @see #asArray(Collection, Writer, int)
     */
	private static void asDependentGenericArray(Collection<?> elements, Writer writer, int level) throws IOException{
	    writer.write('[');
        Iterator<?> elemIterator = elements.iterator();

        if (elemIterator.hasNext()) {
        	writer.write('\n');
			indent(elemIterator.next().toString(), writer, level + 1);
		}

        while(elemIterator.hasNext()) {
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
	 *
	 * @see #asGenericObject(Map, Writer, int)
	 */
    public static String asGenericObject(Set<? extends Map.Entry<String, ?>> elements) {
        try {
            StringWriter writer = new StringWriter();
            asGenericObject(elements, writer, 0);
            return writer.toString();
        }
        catch (IOException e) {
            return null;
        }
    }

	/**
	 * Writes the elements as a generic pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if file is not found
	 *
	 * @see #asGenericObject(Map, Writer, int)
	 */
	public static void asGenericObject(Map<String, ? extends Collection<Integer>> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asGenericObject(elements, writer, 0);
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
	 *
	 * @see #asGenericObject(Map, Writer, int)
	 */
	public static void asGenericObject(Map<String, ?> elements, Writer writer, int level) throws IOException {
		indent(writer, level);
		asDependentGenericObject(elements, writer, level);
		writer.write('\n');
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
	 *
	 * @see #asDependentGenericObject(Map, Writer, int)
	 */
	public static void asGenericObject(Set<? extends Map.Entry<String, ?>> elements, Writer writer, int level) throws IOException {
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
	 *
	 * @see #asGenericObject(Map, Writer, int)
	 */
	private static void asDependentGenericObject(Set<? extends Map.Entry<String, ?>> elements, Writer writer, int level) throws IOException {
		var elemIterator = elements.iterator();
		writer.write('{');

		if (elemIterator.hasNext()) {
			writer.write('\n');
			asObjectVariable(elemIterator.next(), writer, level);
		}

		while(elemIterator.hasNext()) {
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
	private static void asDependentGenericObject(Map<String, ?> elements, Writer writer, int level) throws IOException {
		asDependentGenericObject(elements.entrySet(), writer, level);
	}

	/**
	 * Helper method for writing an element with its object name and value
	 *
	 * @param element the element to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if file is not found
	 */
    private static void asObjectVariable(Map.Entry<String, ?> element, Writer writer, int level) throws IOException{
		quote(element.getKey(), writer, level + 1);
		writer.write(": ");
		var type = element.getValue();
		//Why is there no switch case for this??
		if (type instanceof Collection<?>) {
			asDependentGenericArray((Collection)type, writer, level + 1);
		}
		else if (type instanceof Map<?, ?>) {
			asDependentGenericObject((Map<String, ?>)type, writer, level + 1);
		}
		else {
			writer.write(type.toString());
		}
	}

//	private static void asArrayVariable(? element, Writer writer, int level) throws IOException {
//		if (element instanceof Collection<?>) {
//			asDependentGenericArray((Collection)element, writer, level);
//		}
//		else if (element instanceof Map<?, ?>) {
//			asDependentGenericObject((Map<String, ?>)element, writer, level);
//		}
//		else {
//			writer.write(element.toString());
//		}
//	}

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
	 *
	 * @see #indent(String, Writer, int)
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if file is not found
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		writer.write(element);
	}

    public static void indent(char ch, Writer writer, int times) throws IOException {
        // THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
        indent(writer, times);
        writer.write(ch);
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
	 *
	 * @see #indent(Writer, int)
	 * @see #quote(String, Writer)
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		// THIS CODE IS PROVIDED FOR YOU; DO NOT MODIFY
		indent(writer, times);
		quote(element, writer);
	}

	/**
	 * A simple main method that demonstrates this class.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// MODIFY AS NECESSARY TO DEBUG YOUR CODE

		TreeSet<Integer> elements = new TreeSet<>();
		System.out.println("Empty:");
		System.out.println(asArray(elements));

		elements.add(65);
		System.out.println("\nSingle:");
		System.out.println(asArray(elements));

		elements.add(66);
		elements.add(67);
		System.out.println("\nSimple:");
		System.out.println(asArray(elements));
	}
}
