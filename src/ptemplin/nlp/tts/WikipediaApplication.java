package ptemplin.nlp.tts;

import java.util.Scanner;

import ptemplin.nlp.tts.control.SimpleTTSController;
import ptemplin.nlp.tts.control.TTSController;
import ptemplin.nlp.tts.parse.HTMLExtractor;

/**
 * A sample application that reads a wikipedia page to read from stdin and 
 * plays the corresponding TTS.
 */
public class WikipediaApplication {
	
	private static final String INITIAL_INPUT_PROMPT = "What would you like to hear about?";
	private static final String FURTHER_INPUT_PROMPT = "Anything else?";
	private static final String EXIT_INPUT = "no";
	private static final String EXIT_MESSAGE = "Exiting...";
	
	private static final String WIKI_PAGE_PREFIX = "/wiki/";

	/**
	 * Reads in a wikipedia page from stdin, retrieves and parses the contents,
	 * and plays the corresponding TTS.
	 */
	public static void main(String[] args) {
		String input = "";
		
		try (Scanner scanner = new Scanner(System.in)) {
			TTSController ttsPlayer = new SimpleTTSController();
			int count = 0;
			while (!input.equalsIgnoreCase(EXIT_INPUT)) {
				if (count == 0) {
					System.out.println(INITIAL_INPUT_PROMPT);
				} else {
					System.out.println(FURTHER_INPUT_PROMPT);
				}
				input = scanner.nextLine();
				ttsPlayer.playSpeech(getWikiPageToRead(input));
				++count;
			}
			
			System.out.println(EXIT_MESSAGE);
		}
	}
	
	/**
	 * Returns a parsed wikipedia page to read as sample text.
	 * @param pageTitle the page title to retrieve
	 * @return the parsed wikipedia page
	 */
	private static String getWikiPageToRead(String pageTitle) {
		return HTMLExtractor.getWikiText(WIKI_PAGE_PREFIX + pageTitle);
	}
	
}
