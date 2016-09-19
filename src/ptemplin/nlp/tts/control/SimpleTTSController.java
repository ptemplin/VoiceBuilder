package ptemplin.nlp.tts.control;

import java.util.List;

import ptemplin.nlp.tts.audio.SpeechBank;
import ptemplin.nlp.tts.data.CMUPronunciationDictionary;
import ptemplin.nlp.tts.data.Phoneme;
import ptemplin.nlp.tts.data.PronunciationDictionary;
import ptemplin.nlp.tts.parse.SimpleInputParser;

/**
 * Plays phonemes individually.
 */
public class SimpleTTSController implements TTSController {
	
	private final Voice voice;
	private final PronunciationDictionary pronunciations;

	public SimpleTTSController() {
		pronunciations = new CMUPronunciationDictionary();
		voice = new Voice(new SpeechBank());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void playSpeech(String rawText) {
		
		// 1. Tokenize the raw text
		List<String> words = SimpleInputParser.getTokensFromText(rawText);

		// For each word of input:
		for (String word : words) {
			// 2. Retrieve the pronunciation from the dictionary
			List<Phoneme> phonemes = pronunciations.getPronunciation(word);
			// 3. Play the audio for the phonemes
			voice.playWord(phonemes);
		}
	}
	
}
