package ptemplin.nlp.tts.audio;

/**
 * Defines the meaningful start and end points of a phoneme recording.
 */
public class PhonemeBoundaries {

    private final float start;
    private final float end;
    public PhonemeBoundaries(float start, float end) {
    	this.start = start;
    	this.end = end;
    }
    
    public float getStart() { return start; }
    public float getEnd() {  return end; }
	
}
