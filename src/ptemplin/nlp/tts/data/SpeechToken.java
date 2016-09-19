package ptemplin.nlp.tts.data;

import java.util.List;

/**
 * A word and the corresponding phonemes.
 */
public class SpeechToken {

	private final List<Phoneme> phonemes;
	private final String word;
	
	public SpeechToken(String word, List<Phoneme> phonemes) {
		this.phonemes = phonemes;
		this.word = word;
	}
	
	public List<Phoneme> getPhonemes() {
		return phonemes;
	}
	
	public String getWord() {
		return word;
	}
	
}
