package ptemplin.nlp.tts.audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ptemplin.nlp.tts.audio.SpeechRecording.Type;
import ptemplin.nlp.tts.data.Phoneme;

/**
 * Provides utilities for mixing lists of speech and phoneme recordings into a playable
 * stream of bytes using logic about speech.
 */
public class SpeechMixer {
	
	private static final int DEFAULT_MERGE_LENGTH = 4000;
	
	/**
	 * Builds the playback data for the given list of phoneme recordings using logic on
	 * a per-phoneme basis.
	 * @param recordings
	 * 			the list of phoneme recordings to merge
	 * @return the playback data
	 */
	public static byte[] buildPhonemeSpeechPlaybackData(List<PhonemeRecording> recordings) {
		List<Byte> data = new ArrayList<>();
		for (int i = 0; i < recordings.size(); i++) {
			byte[] recording = recordings.get(i).getAudio();
			// if the data array is empty, simply add the recording
			// also don't merge if consonant follows a vowel
			if (data.isEmpty() || recordings.get(i-1).getPhoneme().isVowel()) {
				for (byte b : recording) {
					data.add(b);
				}
			} else {
				mergeSamples(data, recording);
			}
		}

		// flush the list into an array
		byte[] dataArr = new byte[data.size()];
		for (int i = 0; i < dataArr.length; i++) {
			dataArr[i] = data.get(i);
		}
		return dataArr;
	}

	/**
	 * Builds the playback data for the given list of speech recordings using logic about
	 * the types of token and metadata.
	 * @param recordings
	 * 			the list of speech recordings to merge
	 * @return the playback data
	 */
	public static byte[] buildSpeechPlaybackData(List<SpeechRecording> recordings) {
		List<Byte> data = new ArrayList<>();
		for (int i = 0; i < recordings.size(); i++) {
			// lookup the recording in the dictionary
			byte[] recording = recordings.get(i).getAudio();
			// if the data array is empty, simply add the recording
			// also don't merge if consonant follows a vowel
			if (data.isEmpty() || isVowel(recordings.get(i-1))) {
				for (byte b : recording) {
					data.add(b);
				}
			} else {
				mergeSamples(data, recording);
			}
		}
		// flush the list into an array
		byte[] dataArr = new byte[data.size()];
		for (int i = 0; i < dataArr.length; i++) {
			dataArr[i] = data.get(i);
		}
		return dataArr;
	}
	
	/**
	 * Merge sample2 into sample1 using the length specified by the DEFAULT_MERGE_LENGTH.
	 */
	private static void mergeSamples(List<Byte> sample1, byte[] sample2) {
		int mergeLength = DEFAULT_MERGE_LENGTH;
		int mergeStart = sample1.size() - mergeLength;
		if (mergeStart < 0) {
			mergeStart = 0;
			mergeLength = sample1.size();
		}
		// merge the next recording with the previous one
		for (int j = 0; j < mergeLength; j+=4) {
			int dataIndex = j + mergeStart;
			// get the previous left and right values from data
			short prevL = (short) ((sample1.get(dataIndex) & 0x00FF) + (sample1.get(dataIndex+1) & 0xFF00));
			short prevR = (short) ((sample1.get(dataIndex+2) & 0x00FF) + (sample1.get(dataIndex+3) & 0xFF00));
			// get the next left and right values from the recording
			short nextL = (short) ((sample2[j] & 0x00FF) + (sample2[j+1] & 0xFF00));
			short nextR = (short) ((sample2[j+2] & 0x00FF) + (sample2[j+3] & 0xFF00));
			// combine the previous and next values
			short newL = (short) ((prevL + nextL)/2);
			short newR = (short) ((prevR + nextR)/2);
			// write back to the data list
			sample1.set(dataIndex, (byte)(newL&0xFF));
			sample1.set(dataIndex+1, (byte)(newL>>8));
			sample1.set(dataIndex+2, (byte)(newR&0xFF));
			sample1.set(dataIndex+3, (byte)(newR>>8));
		}
		for (int j = mergeLength; j < sample2.length; j++) {
			sample1.add(sample2[j]);
		}
	}
	
	/**
	 * @return true if the speechRecording is vowel phone, false otherwise
	 */
	private static boolean isVowel(SpeechRecording speechRecording) {
		if (speechRecording.getType() == Type.PHONE) {
			return Phoneme.valueOf(speechRecording.getIdentifier()).isVowel();
		}
		return false;
	}
	
}
