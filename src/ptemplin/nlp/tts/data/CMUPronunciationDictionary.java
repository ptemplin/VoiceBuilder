package ptemplin.nlp.tts.data;

import static ptemplin.nlp.tts.Constants.RES_DIR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMUPronunciationDictionary implements PronunciationDictionary {
	
	private static final String CMU_PRONUNCIATION_DICT_FILE_PATH = RES_DIR + "\\CMUPronunciationDict.txt";

	private final Map<String, List<Phoneme>> wordsToPhonemes;
	
	public CMUPronunciationDictionary() {
		wordsToPhonemes = new HashMap<>();
		readDictionaryFromFile();
	}
	
	/**
	 * Retrieves the pronunciation of the given word from the CMU dictionary.
	 * Empty if word is not found in dictionary.
	 */
	public List<Phoneme> getPronunciation(String word) {
		if (wordsToPhonemes.containsKey(word.toUpperCase())) {
			return wordsToPhonemes.get(word.toUpperCase());
		} else {
			// word is not in the dictionary
			return Collections.emptyList();
		}
	}
	
	/**
	 * Reads the text file which contains the CMU pronunciations and builds a dictionary of
	 * words to pronunciations.
	 */
	private void readDictionaryFromFile() {
		File dict = new File(CMU_PRONUNCIATION_DICT_FILE_PATH);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dict)))) {
			while (true) {
				String line = reader.readLine();
				// break at end of file
				if (line == null) {
					break;
				} else if (line.isEmpty()) {
					continue;
				}
				addEntryToDictionary(line);
			}
		} catch (IOException ex) {
			System.out.println("ERROR: Dictionary could not be read from file");
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Adds a new entry to the dictionary, corresponding to a word and its respective
	 * pronunciation.
	 *
	 * @param line from the dictionary text file in the form:
	 * 			LEXEME PHONEME1 PHONEME2 ...
	 */
	private void addEntryToDictionary(String line) {
		// create a new entry in the dictionary
		String[] lineTokens = line.split(" ");
		String lexeme = lineTokens[0];
		List<Phoneme> phonemes = new ArrayList<>();
		// for each phoneme token:
		for (int i = 1; i < lineTokens.length; i++) {
			// if token is not empty:
			if (!lineTokens[i].isEmpty()) {
				// vowel tokens contain extra numeral for pitch (eg. AY2)
				if (lineTokens[i].length() == 3) {
					// take the first two letters
					String phoneme = lineTokens[i].substring(0, lineTokens[i].length() - 1);
					phonemes.add(Phoneme.valueOf(phoneme));
				} else {
					phonemes.add(Phoneme.valueOf(lineTokens[i]));
				}
			}
		}
		wordsToPhonemes.put(lexeme, phonemes);
	}
		
}
