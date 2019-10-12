import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

/**
 * A utility class for finding all text files in a directory using lambda
 * functions and streams.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2019
 */
public final class TextFileFinder {
	/**
	 * Do not instantiate.
	 */
	private TextFileFinder() {
	}

	/**
	 * String array representing the default acceptable file extensions.
	 */
	private static final String[] ACCEPTABLE_FILE_EXTENSIONS = {
			"txt",
			"text"
	};

	/**
	 * String representing the system's file separator. Does not work if using different systems.
	 */
	private static final String FILE_SEPARATOR = java.nio.file.FileSystems.getDefault().getSeparator();

	/**
	 * char representing the system's extension separator.
	 */
	private static final char EXTENSION_SEPARATOR = '.';

	/**
	 * Largest int value representing the maximum level of directories to search
	 */
	private static final int DEFAULT_DEPTH = Integer.MAX_VALUE;

	/**
	 * A lambda function that returns true if the path is a file that ends in a .txt or .text extension
	 * (case-insensitive). Useful for {@link Files#walk(Path, FileVisitOption...)}.
	 *
	 * @see Files#isRegularFile(Path, java.nio.file.LinkOption...)
	 * @see Path#getFileName()
	 * @see Files#walk(Path, FileVisitOption...)
	 */
	public static final Predicate<Path> IS_TEXT = TextFileFinder::isAcceptableExtension;

	/**
	 * A lambda function that returns true if the path is a file that ends in a .txt or .text extension
	 * (case-insensitive). Useful for {@link Files#find(Path, int, BiPredicate, FileVisitOption...)}.
	 *
	 * @see Files#find(Path, int, BiPredicate, FileVisitOption...)
	 */
	// DO NOT MODIFY; THIS IS PROVIDED FOR YOU
	// (Hint: This is only useful if you decide to use Files.find(...) instead of Files.walk(...)
	public static final BiPredicate<Path, BasicFileAttributes> IS_TEXT_ATTR = (path, attr) -> IS_TEXT.test(path);

	/**
	 * Returns a stream of text files, following any symbolic links encountered.
	 *
	 * @param start the initial path to start with
	 * @return a stream of text files
	 * @throws IOException if an I/O error is thrown when accessing Files.find(Path)
	 * @see #IS_TEXT
	 * @see #IS_TEXT_ATTR
	 * @see FileVisitOption#FOLLOW_LINKS
	 * @see Files#walk(Path, FileVisitOption...)
	 * @see Files#find(Path, int, BiPredicate, FileVisitOption...)
	 * @see Integer#MAX_VALUE
	 */
	public static Stream<Path> find(Path start) throws IOException {
		return Files.find(start, DEFAULT_DEPTH, IS_TEXT_ATTR, FOLLOW_LINKS);
	}

	/**
	 * Returns a list of text files.
	 *
	 * @param start the initial path to search
	 * @return list of text files
	 * @throws IOException if an I/O error is thrown when accessing Files.find(Path) in TextFileFinder.find(Path)
	 * @see #find(Path)
	 */
	public static List<Path> list(Path start) throws IOException {
		// THIS METHOD IS PROVIDED FOR YOU DO NOT MODIFY
		return find(start).collect(Collectors.toList());
	}

	/**
	 * Tests if {@code path} ends with one of the correct file extensions in ACCEPTABLE_FILE_EXTENSIONS.
	 *
	 * @param path A relative path
	 * @return {@code true} if the path ends with the correct extension
	 * @see #ACCEPTABLE_FILE_EXTENSIONS
	 */
	public static boolean isAcceptableExtension(Path path) {
		if (Files.exists(path) && !Files.isDirectory(path)) {
			String pathExtension = getExtension(path.toString()).toLowerCase();
			for (String ext : ACCEPTABLE_FILE_EXTENSIONS) {
				if (pathExtension.equals(ext))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns a substring of the valid file extension, where the file extension
	 * is considered to be the string that succeeds a valid EXTENSION_SEPARATOR,
	 * where an EXTENSION_SEPARATOR is valid if it does not precede a FILE_SEPARATOR
	 *
	 * @param pathName The String representation of a relative path
	 * @return the file extension of the parameter,
	 * an empty string if there is no valid EXTENSION_SEPARATOR,
	 * or {@code null} if the parameter is null
	 */
	public static String getExtension(String pathName) {
		if (pathName != null) {
			int index = indexOfExtension(pathName);
			if (index != -1) {
				return pathName.substring(index + 1);
			}
			return "";
		}
		return null;
	}

	/**
	 * Returns an index of a valid EXTENSION_SEPARATOR, where
	 * an EXTENSION_SEPARATOR is valid if it does not precede a FILE_SEPARATOR
	 *
	 * @param pathName The String representation of a relative path
	 * @return the index of the last occurrence of a valid EXTENSION_SEPARATOR in pathName,
	 * or {@code -1} if the EXTENSION_SEPARATOR precedes a FILE_SEPARATOR
	 */
	private static int indexOfExtension(String pathName) {
		int extensionPos = pathName.lastIndexOf(EXTENSION_SEPARATOR);
		int lastSeparator = pathName.lastIndexOf(FILE_SEPARATOR);
		return (extensionPos > lastSeparator) ? extensionPos : -1;
	}
}
