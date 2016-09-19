package ptemplin.nlp.tts.data;

import java.util.List;

/**
 * The interface for a pronunciation dictionary. Must be initialized before use.
 */
public interface PronunciationDictionary {
	
	/**
	 * Retrieve the pronunciation from the dictionary for the given word.
	 * Returns the list of phonemes included in the pronunciation.
	 */
	List<Phoneme> getPronunciation(String word);

}
