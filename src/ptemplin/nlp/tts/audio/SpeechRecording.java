package ptemplin.nlp.tts.audio;

/**
 * A recording of some part of speech determined by the type.
 */
public class SpeechRecording {

	private final Type type;
	private final String identifier;
	private final byte[] rawAudioRecording;
	
	/**
	 * @param type of the recording
	 * @param identifier a unique id
	 * @param rawAudioRecording the audio recording of the part of speech
	 */
	public SpeechRecording(Type type, String identifier, byte[] rawAudioRecording) {
		this.type = type;
		this.identifier = identifier;
		this.rawAudioRecording = rawAudioRecording;
	}
	
	public Type getType() { return type; }
	public String getIdentifier() { return identifier; }
	public byte[] getAudio() { return rawAudioRecording; }
	
	public static enum Type {
		PHONE,
		POLYPHONE,
		WORD
	}
}
