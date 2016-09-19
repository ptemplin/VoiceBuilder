package ptemplin.nlp.tts.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ptemplin.nlp.tts.parse.HTMLExtractor;

@Deprecated
public class Voice {

    private static final int BUFFER_SIZE = 128000;
    private static DataLine.Info dataLineInfo;
    private static AudioFormat audioFormat;
    
    public static final String recordingDir = "file:/C:/Users/Me/Documents/SoundRecordings/";
    
	private static HashMap<String,byte[]> phonemeToRecording = new HashMap<>();
	private static String[] phonemes = {
		"AA","AE","AH","AO","AW","AY",
		"B","CH","D","DH","EH","ER",
		"EY","F","G","HH","IH","IY",
		"JH","K","L","M","N","NG","OW",
		"OY","P","R","S","SH","T","TH",
		"UH","UW","V","W","Y","Z","ZH"};
	// vowels are played for longer
	private static String[] vowels = {
		"AA","AE","AH","AO","AW","AY",
		"EH","ER","EY","IH","IY",
		"NG","OW","OY","UH","UW"
	};
	private static HashMap<String,Boundary> phonemeToBoundary = new HashMap<>();
	private static HashMap<String,List<String>> wordToPhones = new HashMap<>();
    
    public static void main(String[] args) throws Exception{
    	initialize();
    	readUserInput();
    }
    
    public static void readWikipediaPage(String pageName) throws Exception {
    	String[] text = HTMLExtractor.getWikiText("/wiki/" + pageName).split(" ");
    	System.out.println("Reading...");
    	for (String s : text) {
    		sayWord(s);
    	}
    }
    
    public static void readUserInput() throws Exception {
    	System.out.println("What would you like me to say?");
		Scanner scanner = new Scanner(System.in);
		String input = "";
		while (!input.equals("x")) {
			input = scanner.nextLine();
			String[] tokens = input.split(" ");
			for (String s : tokens) {
				sayWord(s);
			}
		}
    }
    
    public static void sayWord(String word) throws Exception{
		if (wordToPhones.isEmpty()) {
			initialize();
		}
		// word entries are in all caps, so do toUpper
		List<String> phones = null;
		if (wordToPhones.containsKey(word.toUpperCase())) {
			phones = wordToPhones.get(word.toUpperCase());
		} else {
			// don't know how to say word
			return;
		}
		
		// play each phone in sequence
		// open a data line and start it
        SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceLine.open(audioFormat);
        sourceLine.start();
        
        List<Byte> data = new ArrayList<>();
        int MERGE_LENGTH = 4000;
        for (int i = 0; i < phones.size(); i++) {
        	String p = phones.get(i);
        	// lookup the recording in the dictionary
        	byte[] recording = phonemeToRecording.get(p);
        	// if the data array is empty, simply add the recording
        	// also don't merge if consonant follows a vowel
        	if (data.isEmpty() || isVowel(phones.get(i-1))) {
        		for (byte b : recording) {
        			data.add(b);
        		}
        	} else {
        		int mergeStart = data.size() - MERGE_LENGTH;
        		if (mergeStart < 0) {
        			mergeStart = 0;
        			MERGE_LENGTH = data.size();
        		}
        		// merge the next recording with the previous one
        		for (int j = 0; j < MERGE_LENGTH; j+=4) {
        			int dataIndex = j + mergeStart;
        			// get the previous left and right values from data
        			short prevL = (short) ((data.get(dataIndex) & 0x00FF) + (data.get(dataIndex+1) & 0xFF00));
        			short prevR = (short) ((data.get(dataIndex+2) & 0x00FF) + (data.get(dataIndex+3) & 0xFF00));
        			// get the next left and right values from the recording
        			short nextL = (short) ((recording[j] & 0x00FF) + (recording[j+1] & 0xFF00));
        			short nextR = (short) ((recording[j+2] & 0x00FF) + (recording[j+3] & 0xFF00));
        			// combine the previous and next values
        			short newL = (short) ((prevL + nextL)/2);
        			short newR = (short) ((prevR + nextR)/2);
        			// write back to the data list
        			data.set(dataIndex, (byte)(newL&0xFF));
        			data.set(dataIndex+1, (byte)(newL>>8));
        			data.set(dataIndex+2, (byte)(newR&0xFF));
        			data.set(dataIndex+3, (byte)(newR>>8));
        		}
        		for (int j = MERGE_LENGTH; j < recording.length; j++) {
        			data.add(recording[j]);
        		}
        	}
        }
        
        // flush the list into an array
        byte[] dataArr = new byte[data.size()];
        for (int i = 0; i < dataArr.length; i++) {
        	dataArr[i] = data.get(i);
        }
        
        // write to the line and close it
        sourceLine.write(dataArr, 0, dataArr.length);
        sourceLine.drain();
        sourceLine.close();
		Thread.sleep(100);
	}

    /**
     * @param filename the name of the file that is going to be played
     */
    private static void playPhone(String phoneName, double startOffset, double endOffset) throws Exception{
    	
    	// open a data line and start it
        SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceLine.open(audioFormat);
        sourceLine.start();
        
        // lookup the recording in the dictionary
        byte[] data = phonemeToRecording.get(phoneName);
        // EXPERIMENTING
        for (int i = 0; i < data.length; i++) {
        	data[i] = (byte) (data[i]);
        }
        // calculate the offset and length to play
        int size = data.length;
        int offset = (int) ((startOffset*size)/4)*4;
        int length = (int) ((endOffset*size)/4)*4 - offset;
        // fallback in case we go out of bounds
        if (offset + length > size) {
        	length = size - offset;
        }
        
        // write to the line and close it
        sourceLine.write(data, offset, length);
        sourceLine.drain();
        sourceLine.close();
    }
    
    private static void initialize() throws Exception {
    	// initialize the phoneme boundaries
    	File boundaries = new File("C:\\Projects\\NLPExperiments\\PhonemeBoundaries_temp");
    	Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(boundaries))));
    	while (scanner.hasNext()) {
    		String phoneme = scanner.next();
    		if (scanner.hasNextFloat()) {
    			float start = scanner.nextFloat();
    			float end = scanner.nextFloat();
    			Boundary bound = new Boundary(start,end);
    			phonemeToBoundary.put(phoneme, bound);
    		} else {
    			// defaults
    			phonemeToBoundary.put(phoneme, new Boundary(0f,0.9f));
    		}
    	}
    	scanner.close();
    	
    	// get phone recordings
		for (String phoneme : phonemes) {
			// get the stream
			String fileName = "C:\\Users\\Me\\Documents\\Sound recordings\\" + phoneme + "_recording.wav";
	    	File soundFile = new File(fileName);
	    	AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
	    	// init the audio format if necessary
	    	if (audioFormat == null) {
	    		audioFormat = audioStream.getFormat();
	            dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
	    	}
	    	// read the contents of the stream buffer by buffer
	        int nBytesRead = 0;
	        byte[] abData = new byte[BUFFER_SIZE];
	        List<Byte> fullSample = new ArrayList<>();
	        while (nBytesRead != -1) {
	            nBytesRead = audioStream.read(abData, 0, abData.length);
	            if (nBytesRead >= 0) {
	            	for (int i = 0; i < nBytesRead; i++) {
	            		fullSample.add(abData[i]);
	            	}
	            }
	        }
	        // calculate the offset and length to play
	        Boundary bound = phonemeToBoundary.get(phoneme);
	        int size = fullSample.size();
	        int offset = (int) ((bound.start*size)/4)*4;
	        int length = (int) ((bound.end*size)/4)*4 - offset;
	        // copy the list values to an array for playback
	        byte[] fullSampleArr = new byte[length];
	        for (int i = 0; i < fullSampleArr.length; i++) {
	        	fullSampleArr[i] = fullSample.get(i+offset);
	        }
	    	
	        // put it in the table
			phonemeToRecording.put(phoneme,fullSampleArr);
		}
    	
		// initialize pronunciation dictionary
		File dict = new File("C:\\Projects\\NLPExperiments\\CMUPronunciationDict.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dict)));
		while (true) {
			String line = reader.readLine();
			// break at end of file
			if (line == null) {
				break;
			} else if (line.isEmpty()) {
				continue;
			}
			// create a new entry in the dictionary
			String[] parts = line.split(" ");
			String lexeme = parts[0];
			List<String> phones = new ArrayList<>();
			for (int i = 1; i < parts.length; i++) {
				if (!parts[i].equals("")) {
					// contains extra numeral for pitch
					if (parts[i].length() == 3) {
						phones.add(parts[i].substring(0,parts[i].length()-1));
					} else {
						phones.add(parts[i]);
					}
				}
			}
			wordToPhones.put(lexeme, phones);
		}
	}
    
    private static boolean isVowel(String phone) {
    	boolean found = false;
    	for (String vowel : vowels) {
    		if (phone.equals(vowel)) {
    			return true;
    		}
    	}
    	return found;
    }
    
    private static class Boundary {
    	float start;
    	float end;
    	public Boundary(float start, float end) {
    		this.start = start;
    		this.end = end;
    	}
    }
}