package ptemplin.nlp.tts.control;

/**
 * The interface for a TTS player that plays speech as audio from text.
 */
public interface TTSController {
	
	/**
	 * Plays the given string of text as speech.
	 */
	void playSpeech(String rawText);
	
}
