package game2D;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DistanceFadeFilterStream extends FilterInputStream{

	/**
	 * 
	 * This sound filter will determine the volume based on the distance between the sprite generating the sound
	 * and the listener (Player's sprite), which will be the player's sprite.
	 * 
	 * The sound itself will also be fading away.
	 * 
	 * @param in
	 * @param distance
	 * @param maxDistance
	 * 
	 * @StudentId 2731556
	 */
	
	private float distance; // Distance between listener and sprite generating sound
	private float maxDistance; // Max distance in which the player's character can't hear the sound
	
	public DistanceFadeFilterStream(InputStream in, float distance,float maxDistance) {
		super(in);
		this.distance = distance;
		this.maxDistance = maxDistance;
	}
	
	
	// Get a value from the array 'buffer' at the given 'position'
	// and convert it into short big-endian format
	public short getSample(byte[] buffer, int position)
	{
		return (short) (((buffer[position+1] & 0xff) << 8) |
					     (buffer[position] & 0xff));
	}

	// Set a short value 'sample' in the array 'buffer' at the
	// given 'position' in little-endian format
	public void setSample(byte[] buffer, int position, short sample)
	{
		buffer[position] = (byte)(sample & 0xFF);
		buffer[position+1] = (byte)((sample >> 8) & 0xFF);
	}
	
	public int read(byte[] sample, int offset, int length) throws IOException {
		
		/* Number of bytes in data stream */
		int bytesRead = super.read(sample, offset, length);
		
		/* Work out initial volume -> loudest point*/
		float initialVolume = 1.0f - (distance/maxDistance);
		
		/* In case the distance between the two sprites is bigger than the maxDistance,
		 * then the sound can't be heard */
		float volume = (initialVolume > 0) ? initialVolume : 0;
		float change = 2.0f * (volume / (float)bytesRead);
		
		short amp;
		
		//Loop through the sample 2 bytes at a time
		for (int p=0; p<bytesRead; p = p + 2)
		{
			// Read the current amplitutude (volume)
			amp = getSample(sample,p);
			// Reduce it by the relevant volume factor
			amp = (short)((float)amp * volume);
			// Set the new amplitude value
			setSample(sample,p,amp);
			// Decrease the volume
			volume = volume - change;
		}
		
		return length;
	}

}
