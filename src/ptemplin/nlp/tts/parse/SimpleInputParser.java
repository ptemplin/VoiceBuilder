package ptemplin.nlp.tts.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class for simple tokenization of input text.
 */
public class SimpleInputParser {
	
	private static final String SPLIT_REGEX = "\\s|\\.|,|!|\\?|\"";

	/**
	 * Takes a string of raw input text and tokenizes it into the individual words
	 * of the sentence. Uses simple whitespace and punctuation removal to produce the tokens.
	 */
	public static List<String> getTokensFromText(String rawText) {
		String[] initialTokens = rawText.split(SPLIT_REGEX);
		return removeUnwantedTokens(initialTokens);
	}
	
	/**
	 * Filters the given list of tokens using simple whitespace and punctuation
	 * removal rules.
	 *
	 * @param tokens the initial list of tokens to be modified
	 * @return the filtered list of tokens
	 */
	private static List<String> removeUnwantedTokens(String[] tokens) {
		final List<String> filteredTokens = new ArrayList<>();
		for (String token : tokens) {
			if (token.isEmpty()) {
				continue;
			}
			filteredTokens.add(token);
		}
		return filteredTokens;
	}
	
	/**
	 * Test harness.
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		List<String> tokens = getTokensFromText(input);
		System.out.println(Arrays.toString(tokens.toArray()));
		scanner.close();
	}
	
}
