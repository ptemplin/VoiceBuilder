package ptemplin.nlp.tts.audio;

import static ptemplin.nlp.tts.Constants.RES_DIR;
import static ptemplin.nlp.tts.Constants.RECORDINGS_DIR;

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
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import ptemplin.nlp.tts.data.Phoneme;
import ptemplin.nlp.tts.data.SpeechToken;

/**
 * Data store of speech recordings. Recordings are read from disk upon initialization.
 */
public class SpeechBank {
	
	// TODO: Trim phoneme recordings offline, so boundaries aren't necessary
	private final Map<Phoneme, PhonemeBoundaries> phonemeToBoundaries = new HashMap<>();
	private final Map<Phoneme, PhonemeRecording> phonemeToRecording = new HashMap<>();
	
	// TODO: Implement as a part of speech token processing
	private final Map<Phoneme, SpeechRecording> phoneToRecording = new HashMap<>();
	private final Map<List<Phoneme>, SpeechRecording> polyphoneToRecording = new HashMap<>();
	private final Map<String, SpeechRecording> wordToRecording = new HashMap<>();
	
	private AudioFormat audioFormat;
	
	private static final String PHONEME_BOUNDARIES_FILEPATH = RES_DIR + "\\PhonemeBoundaries_temp";
	private static final String PHONEME_RECORDING_FILEPATH_SUFFIX = "_recording.wav";
	
	private static final int INPUT_STREAM_BUFFER_SIZE = 128000;

	public SpeechBank() {
		initializePhonemeBoundaries();
		initializePhonemeRecordings();
	}
	
	/**
	 * Retrieves the list of phoneme recordings corresponding to the given list of phonemes.
	 * @param speech
	 * 			the list of phonemes
	 * @return the list of phoneme recordings for the given phonemes. size == speech.size
	 */
	public List<PhonemeRecording> getPhonemeSpeechStream(List<Phoneme> speech) {
		List<PhonemeRecording> recordings = new ArrayList<>();
		for (Phoneme phoneme : speech) {
			recordings.add(phonemeToRecording.get(phoneme));
		}
		return recordings;
	}
	
	/**
	 * TODO: Implement as a part of speech tokens
	 */
	public List<SpeechRecording> getSpeechStream(List<SpeechToken> speech) {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * @return the audio format of the recordings in this store
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}
	
	/**
	 * Initialize the phoneme boundaries mapping from file.
	 */
	private void initializePhonemeBoundaries() {
		File boundaries = new File(PHONEME_BOUNDARIES_FILEPATH);
    	Scanner scanner;
    	try {
    		scanner = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(boundaries))));
    	} catch (IOException ex) {
    		System.out.println("Error reading phoneme boundaries file");
    		ex.printStackTrace();
    		return;
    	}
    	while (scanner.hasNext()) {
    		Phoneme phoneme = Phoneme.valueOf(scanner.next());
    		if (scanner.hasNextFloat()) {
    			float start = scanner.nextFloat();
    			float end = scanner.nextFloat();
    			PhonemeBoundaries bound = new PhonemeBoundaries(start,end);
    			phonemeToBoundaries.put(phoneme, bound);
    		} else {
    			// defaults
    			phonemeToBoundaries.put(phoneme, new PhonemeBoundaries(0f,0.9f));
    		}
    	}
    	scanner.close();
	}
	
	/**
	 * Initialize the phoneme to recording mapping from file.
	 */
	private void initializePhonemeRecordings() {
		// get phone recordings
		for (Phoneme phoneme : Phoneme.values()) {
			// get the stream
			String fileName = RECORDINGS_DIR + "\\" + phoneme.toString() + PHONEME_RECORDING_FILEPATH_SUFFIX;
			File soundFile = new File(fileName);
			AudioInputStream audioStream;
			try {
				audioStream = AudioSystem.getAudioInputStream(soundFile);
			} catch (UnsupportedAudioFileException ex) {
				System.out.println("Couldn't open audio stream for " + phoneme.toString() + " recording");
				ex.printStackTrace();
				continue;
			} catch (IOException ex) {
				System.out.println("Couldn't open audio stream for " + phoneme.toString() + " recording");
				ex.printStackTrace();
				continue;
			}
			// initialize the audio format if necessary
			if (audioFormat == null) {
				audioFormat = audioStream.getFormat();
			}
			// read the contents of the stream buffer by buffer
			int nBytesRead = 0;
			byte[] abData = new byte[INPUT_STREAM_BUFFER_SIZE];
			ArrayList<Byte> fullSample = new ArrayList<>();
			while (nBytesRead != -1) {
				try {
					nBytesRead = audioStream.read(abData, 0, abData.length);
				} catch (IOException ex) {
					System.out.println("Error reading bytes from audio file for " + phoneme.toString());
					ex.printStackTrace();
					break;
				}
				if (nBytesRead >= 0) {
					for (int i = 0; i < nBytesRead; i++) {
						fullSample.add(abData[i]);
					}
				}
			}
			PhonemeBoundaries bound = phonemeToBoundaries.get(phoneme);
			byte[] fullSampleArr = new byte[fullSample.size()];
			for (int i = 0; i < fullSample.size(); i++) {
				fullSampleArr[i] = fullSample.get(i);
			}
			PhonemeRecording recording = new PhonemeRecording(phoneme, fullSampleArr, bound);
			phonemeToRecording.put(phoneme, recording);
		}
	}
	
}
