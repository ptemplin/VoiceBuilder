package ptemplin.nlp.tts.control;

import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ptemplin.nlp.tts.audio.PhonemeRecording;
import ptemplin.nlp.tts.audio.SpeechBank;
import ptemplin.nlp.tts.audio.SpeechMixer;
import ptemplin.nlp.tts.data.Phoneme;
import ptemplin.nlp.tts.data.SpeechToken;

public class Voice {
	
	private final SpeechBank speechBank;
	
	private static final int WORD_PAUSE_MILLIS = 50;

	public Voice(SpeechBank speechBank) {
		this.speechBank = speechBank;
	}
	
	/**
	 * Plays the word as the string of merged phonemes.
	 */
	public void playWord(List<Phoneme> phonemes) {
		
		List<PhonemeRecording> speechRecordings = speechBank.getPhonemeSpeechStream(phonemes);
		
		byte[] playbackData = SpeechMixer.buildPhonemeSpeechPlaybackData(speechRecordings);
		
		SourceDataLine sourceLine = startAudioOutput();
		if (sourceLine == null) {
			System.out.println("Couldn't start audio output, exiting");
			return;
		}
		// write to the line and close it
		sourceLine.write(playbackData, 0, playbackData.length);
		sourceLine.drain();
		sourceLine.close();
		try {
			Thread.sleep(WORD_PAUSE_MILLIS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Plays the speech specified by the list of speech tokens.
	 */
	public void playSpeech(List<SpeechToken> speech) {
		// TODO: Implement TTS with string of speech tokens
	}
	
	/**
	 * Starts a configured audio output stream.
	 *
	 * @return an audio output stream
	 */
	public SourceDataLine startAudioOutput() {
		SourceDataLine sourceLine;
		try {
			AudioFormat audioFormat = speechBank.getAudioFormat();
			SourceDataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sourceLine.open(audioFormat);
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return null;
		}
		sourceLine.start();
		return sourceLine;
	}
}
