package game2D;

import java.io.*;


import javax.sound.sampled.*;

/**
 * 
 * This class allows to create threads for each sound effect.
 * 
 * @StudentId 2731556
 *
 */

public class Sound extends Thread {

	private String filename;	// The name of the file to play
	private boolean finished;	// A flag showing that the thread has finished
	private float maxDistance; // Max distance at which the volume will be 0 (too far to listen to it)
	private float distance; // Current distance between player's sprite (Listener) and sprite generating the sound
	
	public Sound(String fname, float distance, float maxDistance) {
		filename = fname;
		finished = false;
		this.distance = distance;
		this.maxDistance = maxDistance;
	}
	
	/* Extra constructor for sounds produced by the player's character (Volume will be maximum) */
	public Sound(String fname) {
		this(fname, 0, 512);
	}

	/**
	 * run will play the actual sound but you should not call it directly.
	 * You need to call the 'start' method of your sound object (inherited
	 * from Thread, you do not need to declare your own). 'run' will
	 * eventually be called by 'start' when it has been scheduled by
	 * the process scheduler.
	 */
	public void run() {
		try {
			File file = new File(filename);
			AudioInputStream stream = AudioSystem.getAudioInputStream(file);
			AudioFormat	format = stream.getFormat();
			
			/* Implement sound filter */
			DistanceFadeFilterStream filtered = new DistanceFadeFilterStream(stream, distance, maxDistance);
			AudioInputStream audio = new AudioInputStream(filtered, format, stream.getFrameLength());
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			
			Clip clip = (Clip)AudioSystem.getLine(info);
			clip.open(audio);
			clip.start();
			Thread.sleep(100);
			while (clip.isRunning()) { Thread.sleep(100); }
			clip.close();
		}
		catch (Exception e) {	}
		finished = true;
	}
}
