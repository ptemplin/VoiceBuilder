package ptemplin.nlp.tts.control;

import java.util.ArrayList;
import java.util.List;

import ptemplin.nlp.tts.audio.SpeechBank;
import ptemplin.nlp.tts.data.CMUPronunciationDictionary;
import ptemplin.nlp.tts.data.Phoneme;
import ptemplin.nlp.tts.data.PronunciationDictionary;
import ptemplin.nlp.tts.data.SpeechToken;
import ptemplin.nlp.tts.parse.SimpleInputParser;

/**
 * Plays speech tokens individually, allowing for more complex
 * lookup of polyphones, words, and phrases.
 */
public class FullTTSController implements TTSController {
	
	private final Voice voice;
	private final PronunciationDictionary pronunciations;

	public FullTTSController() {
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

		// 2. Retrieve the pronunciations from the dictionary
		List<SpeechToken> speechTokens = new ArrayList<>();
		for (String word : words) {
			List<Phoneme> phonemes = pronunciations.getPronunciation(word);
			speechTokens.add(new SpeechToken(word, phonemes));
		}
		
		// 3. Play the speech
		voice.playSpeech(speechTokens);
	}
	
}
