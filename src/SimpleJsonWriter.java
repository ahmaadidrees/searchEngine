import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class responsible for writing different data structures into JSON format
 *
 * @author Ahmaad Idrees
 * 
 */
public class SimpleJsonWriter {

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		var iterator = elements.iterator();
		writer.write("[");
		if (iterator.hasNext()) {
			writer.write("\n");
			indent(iterator.next(), writer, level + 1);
		}
		while (iterator.hasNext()) {
			writer.write(",\n");
			indent(iterator.next(), writer, level + 1);
		}
		indent("\n]", writer, level);
		writer.flush();
	}

	/**
	 * writes a collection of results into JSON format
	 * 
	 * @param elements to write
	 * @param writer   to use
	 * @param level    to indent
	 * @throws IOException if IO error occurs
	 */
	public static void asResult(Collection<InvertedIndex.Result> elements, Writer writer, int level)
			throws IOException {
		Collections.sort((List<InvertedIndex.Result>) elements);
		var iterator = elements.iterator();
		writer.write("[");
		if (iterator.hasNext()) {
			InvertedIndex.Result current = iterator.next();
			writer.write(
					"\n    {\n      \"where\": \"" + current.getWhere() + "\",\n      \"count\": " + current.getCount()
							+ ",\n      \"score\": " + String.format("%.8f", current.getScore()) + "\n    }");
		}
		while (iterator.hasNext()) {
			InvertedIndex.Result current = iterator.next();
			writer.write(
					",\n    {\n      \"where\": \"" + current.getWhere() + "\",\n      \"count\": " + current.getCount()
							+ ",\n      \"score\": " + String.format("%.8f", current.getScore()) + "\n    }");
		}
		indent("\n]", writer, level);
		writer.flush();
	}

	/**
	 * writes results associated to searches in JSON format
	 * 
	 * @param results results to write
	 * @param writer  writer to use
	 * @throws IOException if IO Error occurs
	 */
	public static void writeResults(TreeMap<String, ArrayList<InvertedIndex.Result>> results, Writer writer)
			throws IOException {
		var setIterator = results.keySet().iterator();
		writer.write("{");
		if (setIterator.hasNext()) {
			String key = setIterator.next();
			writer.write("\n  \"" + key + "\": ");
			asResult(results.get(key), writer, 1);
		}
		while (setIterator.hasNext()) {
			String key = setIterator.next();
			writer.write(",\n  \"" + key + "\": ");
			asResult(results.get(key), writer, 1);
		}
		writer.write("\n}");
		writer.flush();
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
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
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		var itr = elements.entrySet().iterator();
		writer.write("{");
		if (itr.hasNext()) {
			var entry = itr.next();
			writer.write("\n  \"" + entry.getKey() + "\": " + entry.getValue());
		}
		while (itr.hasNext()) {
			var entry = itr.next();
			writer.write(",\n  \"" + entry.getKey() + "\": " + entry.getValue());
		}
		writer.write("\n}");
		writer.flush();
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		var setIterator = elements.entrySet().iterator();
		writer.write("{");
		level++;
		if (setIterator.hasNext()) {
			writer.write("\n");
			asNestedArray(setIterator.next(), writer, level);
		}
		while (setIterator.hasNext()) {
			writer.write(",\n");
			asNestedArray(setIterator.next(), writer, level);
		}

		writer.write("\n");
		indent("}", writer, level);
		writer.flush();
	}

	/**
	 * Helper method that calls asArray to take care of the collection of integers
	 * 
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map.Entry<String, ? extends Collection<Integer>> elements, Writer writer,
			int level) throws IOException {
		quote(elements.getKey(), writer, level);
		writer.write(": ");
		asArray(elements.getValue(), writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object with an inverted index
	 * 
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asInvertedIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> elements,
			Writer writer, int level) throws IOException {
		var stemIterator = elements.entrySet().iterator();
		writer.write("{");
		level++;
		if (stemIterator.hasNext()) {
			writer.write("\n");
			asInvertedIndex(stemIterator.next(), writer, level);
		}
		while (stemIterator.hasNext()) {
			writer.write(",\n");
			asInvertedIndex(stemIterator.next(), writer, level);
		}
		writer.write("\n");
		indent("}", writer, level);
		writer.flush();
	}

	/**
	 * Helper method that writes the elements from the nested map
	 * 
	 * @param elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asInvertedIndex(Map.Entry<String, ? extends Map<String, ? extends Collection<Integer>>> elements,
			Writer writer, int level) throws IOException {
		quote(elements.getKey(), writer, level);
		writer.write(": ");
		asNestedArray(elements.getValue(), writer, level);
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Indents using 2 spaces by the number of times specified.
	 *
	 * @param writer the writer to use
	 * @param times  the number of times to write a tab symbol
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write(' ');
			writer.write(' ');
		}
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(String, Writer, int)
	 * @see #indent(Writer, int)
	 */
	public static void indent(Integer element, Writer writer, int times) throws IOException {
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents and then writes the element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param times   the number of times to indent
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 */
	public static void indent(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by {@code " "} quotation marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer) throws IOException {
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
	 * @throws IOException if an IO error occurs
	 *
	 * @see #indent(Writer, int)
	 * @see #quote(String, Writer)
	 */
	public static void quote(String element, Writer writer, int times) throws IOException {
		indent(writer, times);
		quote(element, writer);
	}

}
