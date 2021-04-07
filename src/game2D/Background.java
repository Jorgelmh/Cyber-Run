package game2D;

import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * 
 * This class is in charge of updating the background when needed and
 * displaying a parallax scrolling effect.
 * 
 * @StudentId 2741556
 */
public class Background {
	
	private Image bg;
	private double x1;
	private double x2;
	private double velX;
	private int screenWidth;
	
	/* Get array of images that'd be displayed as background (should be in order) */
	public Background(String fileName, int screenWidth) {
		
		/* Create image from fileName */
		bg = new ImageIcon(fileName).getImage();
		
		/* Set screenWidth */
		this.screenWidth = screenWidth;
		
		/* Set initial velocity to 0 */
		velX = 0;
		
		/* Initialize variables */
		x1 = 0;
		x2 = screenWidth;
		
	}
	
	public void update(long elapsedTime) {
		
		x1 += velX*elapsedTime;
		x2 += velX*elapsedTime;
		
		/* Moving forward */
		if(x1 + screenWidth <= 0 ) {
			x1 = x2 + screenWidth;
			return;
		}
		
		if(x2 + screenWidth <= 0) {
			x2 =x1 + screenWidth;
			return;
		}
		
		/* Moving backwards */
		if(x1 >= screenWidth) {
			x1 = x2-screenWidth;
			return;
		}
		
		if(x2 >= screenWidth) {
			x2 = x1-screenWidth;
			return;
		}
	}
	
	public void draw (Graphics2D g) {
				
		/* Draw first background image */
		g.drawImage(bg,(int) x1, 0, null);
		
		/* Draw Second background image */
		g.drawImage(bg, (int) x2, 0, null);
		
	}
	
	public void setVelocity(double velX) {
		this.velX = velX;
	}

	public double getVelocity() {
		return velX;
	}
}
