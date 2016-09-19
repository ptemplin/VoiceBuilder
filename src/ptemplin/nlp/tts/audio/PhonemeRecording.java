package ptemplin.nlp.tts.audio;

import ptemplin.nlp.tts.data.Phoneme;

/**
 * The audio recording of a single phoneme.
 */
public class PhonemeRecording {

	private final Phoneme phoneme;
	private byte[] trimmedAudioRecording;
	private final byte[] rawAudioRecording;
	
	/**
	 * Use the whole recording as the trimmed version.
	 */
	public PhonemeRecording(Phoneme phoneme, byte[] audioRecording) {
		this.phoneme = phoneme;
		this.rawAudioRecording = audioRecording;
		this.trimmedAudioRecording = audioRecording;
	}
	
	/**
	 * Use the given phoneme boundaries to determine the trimmed audio recording.
	 */
	public PhonemeRecording(Phoneme phoneme, byte[] audioRecording, PhonemeBoundaries boundaries) {
		this.phoneme = phoneme;
		this.rawAudioRecording = audioRecording;
		trimRawAudio(boundaries);
	}
	
	public Phoneme getPhoneme() {
		return phoneme;
	}
	
	public byte[] getAudio() {
		return trimmedAudioRecording;
	}
	
	/**
	 * Trim the raw audio recording into the trimmed audio recording using the given
	 * phoneme boundaries.
	 *
	 * @param boundaries the boundaries to use to trim
	 */
	private void trimRawAudio(PhonemeBoundaries boundaries) {
		final int rawSize = rawAudioRecording.length;
		final int offset = (int) ((boundaries.getStart()*rawSize)/4)*4;
		final int trimmedLength = (int) ((boundaries.getEnd()*rawSize)/4)*4 - offset;
		
		trimmedAudioRecording = new byte[trimmedLength];
		for (int i = 0; i < trimmedLength; i++) {
			trimmedAudioRecording[i] = rawAudioRecording[i+offset];
		}
	}
	
}
