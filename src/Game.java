
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.swing.ImageIcon;


import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialize event handlers etc. By default GameCore
// will handle the 'Escape' key to quit the game but you should
// override this with your own event handler.

/**
 * 
 * Controls: 
 * 
 *  - Key arrows to move.
 *  - Space to jump.
 *  - "A" to shoot.
 * 
 * @author David Cairns
 * @StudentId 2741556
 */
@SuppressWarnings("serial")

public class Game extends GameCore 
{
	// Useful game constants
	static int screenWidth = 512;
	static int screenHeight = 384;
	static int startPosX = 64;
	static int startPosY = 150;
	static int bulletCharger = 6;
	
	/* weapon bullets */
	int weaponLoad = bulletCharger; 
	
	static float bulletVelocityX = 0.3f;

    float 	lift = 0.005f;
    float	gravity = 0.00015f;
    
    float shootingDelay = 640;
    float reloadingDelay = 1020;
    
    long lastShot = 0;
    long lastReload = 0;
    
    boolean ableToShoot = true;
    
    boolean isFalling = true;
    
    // Game state flags
    boolean flap = false;
    boolean moveRight = false;
    boolean moveLeft = false;
    boolean jump = false;
    boolean inTheAir = false;
    boolean moving = false;
    boolean shoot = false;
    boolean showHackingMonitor = false;
    boolean canMove = true;
    boolean gameEnded = false;
    
    boolean gameStarted = false;
    
    // Movement velocity
    static float velocityX = .1f;
    static float velocityY = -.10f;
    static double parallaxVelX = .05;

    // Game resources
    Animation landing;
    Animation idle;
    Animation walking;
    Animation reloading;
    Animation shooting;
    Animation robotDead;
    Animation robotWalking;
    Animation robotDead2;
    Animation robotWalking2;
    
    Sprite	player = null;
    /* Parallax scrolling */
    Background parallax = null;
    
    /* Collision stage width */
    float collisionStageWidth;
    int firstEnemyIndex = 0;
    int lastEnemyIndex = 0;
    
    /* Enemies array */
    ArrayList<Sprite> enemies = new ArrayList<Sprite>();
    
    /* Store bullets */
    ArrayList<Sprite> bullets = new ArrayList<Sprite>();
    Animation bulletSprite;
    
    /* Enemy positions for each level */
    int [][] level1Enemies = {null, {455, 190}, {880, 190}, {1234, 255}, {1619, 222}, {2030, 222}, {2423, 190}};
    int [][] level2Enemies = {null, {455, 222}, {880, 222}, {1234, 255}, {1619, 222}, {2030, 190}, {2423, 190}};
    int [][] level3Enemies = {null, {455, 190}, {880, 158}, {1234, 255}, {1619, 222}, {2030, 190}, {2423, 158}};
    ArrayList<Animation> levelSprites;
    
    int pcLevel1X;
    int pcLevel1Y;
    
    /* HUD elements */
    Image playerIcon;
    Image hudBackground;
    Image pistol;
    Image bulletsKit;
    Image gameEndedBg;
    Image gameEndedString;
    float gameEndedAnim = 50;
    float endedAnimVel = 1f;
    
    /* Sounds */
    Sequencer sequencer;
    
    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    
    long total;         			// The score will be the total time elapsed since a crash

    /* Hacking monitor to pass levels */
    Image monitorBorder;
    Image monitorBackground;
    float hackingProgress = 0;
    boolean gateClosed = true;
    int obscureMap = 0;
    
    int level = 0;
    
    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false, screenWidth,screenHeight);
    }

    /**
     * Initialize the class, e.g. set up variables, load images,
     * create animations, register event handlers
     */
    public void init()
    {         

        /** 
         * 	=============================
         * 		INSTANCIATE ANIMATIONS
         * 	=============================
         * */
        idle = new Animation();
        idle.loadAnimationFromSheet("images/idleGun.png", 4, 1, 100);
        
        walking = new Animation();
        walking.loadAnimationFromSheet("images/walkingGun.png", 8, 1, 80);
        
        shooting = new Animation();
        shooting.loadAnimationFromSheet("images/shooting.png", 8, 1, 80);
        
        reloading = new Animation();
        reloading.loadAnimationFromSheet("images/reloading.png", 17, 1, 80);
        
        // Initialise the player with an animation
        player = new Sprite(idle);
        
        /* Load background images */
        parallax = new Background("images/parallax.png", screenWidth);
        
        /* Load bullet sprite */
        bulletSprite = new Animation();
        bulletSprite.addFrame(new ImageIcon("images/bullet.png").getImage(), 100);
        bulletSprite.pause();

        /**
         *  ==========================
         * 			 ENEMIES
         *  ==========================
         */
       
        /* Robot dead animation */
        robotDead = new Animation();
        robotDead.loadAnimationFromSheet("images/robotDead.png", 5, 1, 100);
        
        robotWalking = new Animation();
        robotWalking.loadAnimationFromSheet("images/robot.png", 7, 1, 120);
        
        robotWalking2 = new Animation();
        robotWalking2.loadAnimationFromSheet("images/robot2.png", 8, 1, 120);
        
        robotDead2 = new Animation();
        robotDead2.loadAnimationFromSheet("images/robot2Dead.png", 5, 1, 120);
        
        /**
         *  =====================
         * 		HUD ELEMENTS
         *  =====================
         */
        playerIcon = loadImage("images/Icon2.png");
        hudBackground = loadImage("images/HUDbackground.png");
        pistol = loadImage("images/pistol3.png");
        bulletsKit = loadImage("images/bullets-kit.png");
        
        /* Hacking monitor */
        monitorBorder =  loadImage("images/monitor.png");
        monitorBackground = loadImage("images/matrix.png");
        
        gameEndedBg = loadImage("images/cyberpunk-street.png");
        gameEndedString = loadImage("images/gameover.png");
        
        /**
         *  ======================
         * 			SOUNDS
         *  ======================
         */
        try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        // Load the tile map and print it out so we can check it is valid
        startNewLevel();
        setSize(tmap.getPixelWidth()/4, tmap.getPixelHeight());
        setVisible(true);
        
        initialiseGame();
        collisionStageWidth = 12 * tmap.getTileWidth();
        System.out.println(tmap);
       
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseGame()
    {
    	total = 0;
    	      
        player.setPosition(startPosX, startPosY);
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.show();
        
        /* Show enemies */
        for(Sprite enemy: enemies) {
        	if(enemy != null)
                enemy.show();
        }
        
    }
    
    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {    	
    	
    	if(!gameStarted) {
    		g.setColor(Color.WHITE);
    		g.setFont(new Font("Arial Black", Font.PLAIN, 40));
    		g.drawImage(gameEndedBg, 0, 0, screenWidth, screenHeight, this);
    		 g.drawImage(hudBackground, 110, 80, 300, 50, this);
    		g.drawString("CYBER RUN", 130, 120);
    		g.setFont(new Font("Arial Black", Font.PLAIN, 15));
    		g.drawString("Press any key to start game", 150, 300);
    		
    		return;
    	}
    	
    	if(gameEnded) {
    		g.drawImage(gameEndedBg, 0, 0, screenWidth, screenHeight, this);
    		int width = 300;
    		g.setColor(Color.WHITE);
    		g.setFont(new Font("Arial Black", Font.PLAIN, 20));
    		g.drawString("Press f to start again", 140, 300);
    		g.drawString("Esc to leave", 190, 350);
    		g.drawImage(gameEndedString, (screenWidth - width)/2, (int) gameEndedAnim, width, 100, this);
			gameEndedAnim +=endedAnimVel;

    		if(gameEndedAnim >= 60 || gameEndedAnim <= 45) 
    			endedAnimVel *= -1;
    		
    		return;
    	}
    	
    	// Be careful about the order in which you draw objects - you
    	// should draw the background first, then work your way 'forward'

    	// First work out how much we need to shift the view 
    	// in order to see where the player is.
        int xo = 0;
        int yo = 0;

        // If relative, adjust the offset so that
        // it is relative to the player
        float offset = (player.getScale() == 1) ? 0 : tmap.getTileWidth();
    	float sx = player.getX() - offset;
        
    	if(sx >= tmap.getPixelWidth() - (screenWidth/2) - (tmap.getTileWidth()/2))
        	xo = screenWidth - tmap.getPixelWidth();
    	
        if(sx >= screenWidth/2 - (tmap.getTileWidth()/2)&& xo == 0) {
        	xo = Math.round(screenWidth/2 - (tmap.getTileWidth()/2) - sx);
        	
        	if(player.getVelocityX() == 0)
        		parallax.setVelocity(0);
        	
        	else if(player.getVelocityX() < 0) 
        		parallax.setVelocity(parallaxVelX);
        	
        	else if(player.getVelocityX() > 0)
        		parallax.setVelocity(-parallaxVelX);
        }else {
        	if(parallax.getVelocity() != 0)
        		parallax.setVelocity(0);
        }
        
        /* Draw background before anything else */
        g.setFont(getFont());
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        /* Draw parallax background */
        parallax.draw(g);

        // Apply offsets to player and draw 
        player.setOffsets(xo, yo);
        player.drawTransformed(g);
                
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo);   
        
        // Show score and status information
        String msg = String.format("FPS: %d", (int)getFPS());
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 80, 50);
        
        /* Update enemy stage index */
        firstEnemyIndex =(int)(Math.abs(xo)/collisionStageWidth);
        lastEnemyIndex = (int)((Math.abs(xo) + screenWidth)/collisionStageWidth);
        
        if(lastEnemyIndex > enemies.size() - 1)
        	lastEnemyIndex = enemies.size() - 1;
        
        /* Draw enemies that are in the sections displayed on the screen */
        for(int i = firstEnemyIndex ; i <= lastEnemyIndex; i++ ) {
        
        	if(enemies.get(i) == null || !enemies.get(i).isVisible())
        		continue;
        	
        	/* Set offsets for the sprite */
        	enemies.get(i).setOffsets(xo, yo);
        	int offsetLife = (enemies.get(i).getScale() == 1) ? 0 : enemies.get(i).getWidth();
        		
        	/* Calculate life-bar's width */
        	int lifeWidth = (int) (enemies.get(i).getLife() * enemies.get(i).getWidth() / 100);
            		
            /* Draw life statistics */
        	g.setColor(Color.red);
            g.fillRect(Math.round(enemies.get(i).getX() + xo - offsetLife), Math.round(enemies.get(i).getY() - 20), lifeWidth, 8);
            		
            /* Draw life border */
            g.setColor(Color.WHITE);
            g.drawRect(Math.round(enemies.get(i).getX() + xo - offsetLife), Math.round(enemies.get(i).getY() - 20), enemies.get(i).getWidth(), 8);
            
        	enemies.get(i).drawTransformed(g);
       }
        
        /* Draw bullets */
        for(Sprite bullet: bullets) {
        	bullet.setOffsets(xo, yo);
        	bullet.draw(g);
        }
        
        
        
        /**
         *  ==================
         * 	     DRAW HUD
         *  ==================
         */
        g.setColor(Color.WHITE);
        g.drawImage(hudBackground, 110, screenHeight - 50, 150, 44, this);
        g.drawImage(pistol, 120, screenHeight - 37, 65, 20, this);
        g.drawImage(bulletsKit, 225, screenHeight - 45, 35, 35, this);
        g.drawString(String.valueOf(weaponLoad), 210, screenHeight - 25);
        g.drawImage(playerIcon, 10, screenHeight - 100, 100, 100, this);
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(3));
        g.setColor(Color.BLACK);
        g.drawRect(10, screenHeight - 100, 100, 100);
        g.setStroke(old);
        
        if(showHackingMonitor)
        	showHackingMonitor(g);
        
        if(!gateClosed && obscureMap < 255) {
        	g.setColor(new Color(0, 0, 0, obscureMap));
        	g.fillRect(0, 0, getWidth(), getHeight());
        	obscureMap+=3;
        	
        	if(obscureMap >= 255)
        		startNewLevel();	
        }        
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	
    	if(!gameStarted)
    		return;
    	
        // Make adjustments to the speed of the sprite due to gravity
    	if(isFalling)
    		player.setVelocityY(player.getVelocityY()+(gravity*elapsed));
    	    	
       	
        // Now update the sprites animation and position

    	/* Check sprite movement flags */
    	updatePlayer(player);
        player.update(elapsed);
        
        /* Update parallax background */
        parallax.update(elapsed);
       
        /* Update enemies sprite */
        for(int i = firstEnemyIndex ; i <= lastEnemyIndex; i++ ) {
        	Sprite enemy = enemies.get(i);
        	
        	if(enemy == null)
        		continue;
        	
        	/* Update enemy positions */
        	enemy.update(elapsed);
        	
        	/* End death animation */
        	if(enemy.getLife() == 0 && enemy.getAnimation().getAnimationFrame() == enemy.getAnimation().getColumns() - 1) 
        		enemy.hide();
        	
        	/* Check collision with enemies displayed on the screen */
        	if(enemy.getLife() > 0) {
    			checkTileCollision(enemy, tmap, elapsed);
    			
            	/* Check for collisions between player and sprites */
    			if(boundingBoxCollision(player, enemy))
            		restartLevel();
        	}	
        }
        
        /* Update Bullet positions */
        for(Iterator<Sprite> iterator = bullets.iterator(); iterator.hasNext();) {
        	Sprite bullet = iterator.next();
        	bullet.update(elapsed);
        	
        	/* Check collision with map */
        	if(checkTileCollision(bullet, tmap, elapsed))
        		iterator.remove();
        	
        	/* Check collision with enemies */
        	for(int i = firstEnemyIndex; i <= lastEnemyIndex; i++) {
        		Sprite enemy = enemies.get(i);
        		
        		/* Don't update null values */
        		if(enemy == null)
        			continue;
        		
        		/* Check collisions with bullets */
        		if(enemy.getLife() > 0 && boundingBoxCollision(bullet, enemy)) {
        			iterator.remove();
        			enemy.reduceLife(34);
        			
        			/* Set a death animation for each enemy */
        			if(enemy.getLife() == 0 && (enemy.getAnimation() == robotWalking || enemy.getAnimation() == robotWalking2)) {
        				
        				if(enemy.getAnimation() == robotWalking) {
            				robotDead.setAnimationFrame(0);
                			enemy.setAnimation(robotDead);
                			enemy.setVelocity(0, 0);
            			}
            				
            			else if(enemy.getAnimation() == robotWalking2) {
            				robotDead2.setAnimationFrame(0);
            				enemy.setAnimation(robotDead2);
            				enemy.setVelocity(0, 0);
            			}
        	
        				/* Work out distance between the robot that just died and the player */
        				float distance = (float) Math.sqrt(Math.pow(player.getX() - enemy.getX(), 2) 
        									+ Math.pow(player.getY() - enemy.getY(), 2)) - tmap.getTileWidth();
        				
        				Sound robotScreaming = new Sound("sounds/robot-dead.wav", distance, screenWidth/2);
            			robotScreaming.start();
        						
        			}
        		}
        	}
        }
        
        /* Play soundtrack */
        if(!sequencer.isRunning()) {
			try {
				Sequence soundtrack = MidiSystem.getSequence(new File("sounds/Track 1.mid"));
				sequencer.setSequence(soundtrack);
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	sequencer.start();
        }

        
        
        // Then check for any collisions that may have occurred
        handleScreenEdge(player, tmap, elapsed);
        checkTileCollision(player, tmap, elapsed);
                
    }
    
    /**
     *  Update player's velocity based on flags
     * 	
     * 	@param s player's sprite
     */
    
    public void updatePlayer(Sprite s) {
    	
    	/**
    	 *  ===============================
    	 * 		  HORIZONTAL MOVEMENT
    	 *  ===============================
    	 */
    	
    	if(moveRight && moveLeft || !canMove) {
    		if(s.getAnimation() != reloading)
    			s.setAnimation(idle);
    		else
    			reloadWeapon();
    		s.setVelocityX(0);
    		return;
    	}
    	
    	if(moveRight && s.getVelocityX() != velocityX) {
    		s.setVelocityX(velocityX);
    		
    		
    		if(s.getScale() == -1) {
    			s.setX(s.getX() - tmap.getTileWidth());
    		}
    		
    		s.setScale(1);
    	}
    	
    	if(moveLeft && s.getVelocityX() != -velocityX) {
    		s.setVelocityX(- velocityX);
    		
    		if(s.getScale() == 1) {
    			s.setX(s.getX() + tmap.getTileWidth());
    		}
    		
    		s.setScale(-1);
    	}

    	/**
    	 *  ============================
    	 * 		      SHOOTING
    	 *  ============================
    	 */

    	if(shoot && System.currentTimeMillis() - lastShot >= shootingDelay && player.getAnimation() != reloading) {
    		
    		/* In case there are bullets available */
    		if(weaponLoad > 0) {
    			/* Add new bullet to the array */
        		Sprite newBullet = new Sprite(bulletSprite);
        		int offset = (player.getScale() == 1) ? 0 : -player.getWidth();
        		
        		/* Initial position of the bullet */
    	   		newBullet.setPosition(player.getX() + offset, player.getY() + (player.getHeight()/2));
    	   		newBullet.setVelocity(bulletVelocityX * player.getScale(), 0);
    	   		
    	   		/* Record last shot to control delay */
    	   		lastShot = System.currentTimeMillis();
    	   		bullets.add(newBullet);
    	   		
    	   		/* Play gunshot sound */
    	   		Sound gunshot = new Sound("sounds/gunshot.wav");
    	   		gunshot.start();
    	   		
    	   		if(player.getAnimation() == shooting)
    	   			shooting.setAnimationFrame(0);
    	   		else {
    	   			shooting.setAnimationFrame(0);
    	   			player.setAnimation(shooting);	
    	   		}
    	   		
    	   		weaponLoad --;
    	   		
    	   		/* Reload Weapon */
    	   		if(weaponLoad <= 0) {
    	   			player.setAnimation(reloading);
    	   			lastReload = System.currentTimeMillis();
    	   			Sound reloading = new Sound("sounds/reload.wav");
    	   			reloading.start(); 
    	   		}
    		}
    		
    	}
    	
    	reloadWeapon();
    	
    	/* End shooting animation */
    	if(player.getAnimation() == shooting && player.getAnimation().getAnimationFrame() == 7) {
    		shooting.setAnimationFrame(0);
    		
    		if(player.getVelocityX() == 0)
    			player.setAnimation(idle);
    		else
    			player.setAnimation(walking);
    	}
    	
    	/**
    	 *  ==============================
    	 * 		       JUMPING
    	 *  ==============================
    	 */
    	
    	if(jump && !inTheAir) {
    		s.setVelocityY(velocityY);
    		inTheAir = true;
    	}
    	
    	if(player.getVelocityX() == 0 && player.getAnimation() == walking)
    		player.setAnimation(idle);
    }
    
    
    /**
     * Checks and handles collisions with the edge of the screen
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     * @param elapsed	How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed)
    {
    	// This method just checks if the sprite has gone off the bottom screen.
    	
        if (s.getY() + s.getHeight() > tmap.getPixelHeight())
        	restartLevel();
    }
    
    /**
     *  Show hacking monitor that allows to pass to next level
     * 
     * @param g
     */
    public void showHackingMonitor(Graphics2D g) {
    	
    	int width = 350;
    	int height = 200;
    	/* Monitor background and border */
    	g.drawImage(monitorBackground, (screenWidth - width)/2, 70, width, height, this);
    	g.drawImage(monitorBorder, (screenWidth - width)/2, 70, width, height, this);
    	
		
    	if(gateClosed) {
    		g.setColor(new Color(255, 66, 66, 200));
    		g.fillRect(175, 100, 170, 40);
    		g.setColor(Color.WHITE);
    		g.setFont(new Font("Arial Black", Font.PLAIN, 20));
    		g.drawString("Gate Closed", 195, 125);
    	}else {
    		g.setColor(new Color(37, 248, 62, 200));
    		g.fillRect(175, 100, 170, 40);
    		g.setColor(Color.WHITE);
    		g.setFont(new Font("Arial Black", Font.PLAIN, 20));
    		g.drawString("Gate Open", 200, 127);
    	}
    	
    	int progressWidth = 160;
    	
    	/* draw hacking progress bar */
    	g.setFont(getFont());
    	g.drawString("Hacking Gate...", 220, 188);
    	g.drawRect((screenWidth - progressWidth)/2 -1, 200-1, progressWidth+1, 20+1);
    	g.setColor(Color.GREEN);
    	g.fillRect((screenWidth - progressWidth)/2, 200, (int) hackingProgress * progressWidth/100, 20);
    	g.setFont(new Font("Arial Black", Font.PLAIN, 10));
    	g.setColor(Color.BLACK);
    	g.drawString(Math.round(hackingProgress) + "%", 244, 213);
    	
    	if(hackingProgress<100)
    		hackingProgress+= .20;
    	else 
    		gateClosed=false;
    	
    	
    }
    
    private void reloadWeapon() {
     	/* Reload weapon when delay has passed */
    	if(player.getAnimation() == reloading && player.getAnimation().getAnimationFrame() == 16) {
    		weaponLoad = bulletCharger;
    		reloading.setAnimationFrame(0);
    		
    		if(player.getVelocityX() == 0)
    			player.setAnimation(idle);
    		else
    			player.setAnimation(walking);
    	}
    }
    
    /**
     * 
     *  Restart level when the player dies
     * 
     */
    private void restartLevel() {
    	
    	/* Return to start */
        player.setPosition(startPosX, startPosY);
        weaponLoad = bulletCharger;
        
        for(Sprite enemy: enemies) {
        	if(enemy != null) {
        		enemy.setLife(100);
        		enemy.show();
        		
        		if(enemy.getAnimation() == robotDead)
        			enemy.setAnimation(robotWalking);
        		else if(enemy.getAnimation() == robotDead2)
        			enemy.setAnimation(robotWalking2);
        		
        		enemy.setVelocityX(enemy.getScale() * -0.1f);
        	}
        }
    }
    
    /**
     *  Start new level
     * 
     */
    private void startNewLevel() {
    	
    	/* Restart variables */
        canMove = true;
        gateClosed = true;
        showHackingMonitor = false;
        obscureMap = 0;
        hackingProgress = 0;
        level++;
        
        switch (level) {
        	case 1:
        		/* Load map for level 1 */
        		tmap.loadMap("maps", "map.txt");
        		
        		/* Starting positions for level 1 */
                startPosX = 64;
                startPosY = 150;
                
                /* Load enemies for this map */
                levelSprites = new ArrayList<Animation>();
                levelSprites.add(robotWalking);
                enemies = createEnemies(level1Enemies, levelSprites);
        		break;
        	case 2:
        		
        		/* Load map for level 2 */
        		tmap.loadMap("maps", "map2.txt");
        		
        		/* Starting positions for level 1 */
        		startPosY = 100;
        		
        		/* Load enemies for this level */
        		levelSprites = new ArrayList<Animation>();
        		levelSprites.add(robotWalking);
        		levelSprites.add(robotWalking2);
        		enemies = createEnemies(level2Enemies, levelSprites);
        		break;
        	case 3:
        		/* Load map for level 2 */
        		tmap.loadMap("maps", "map3.txt");
        		
        		/* Starting positions for level 1 */
        		startPosY = 150;
        		
        		/* Load enemies for this level */
        		levelSprites = new ArrayList<Animation>();
        		levelSprites.add(robotWalking);
        		levelSprites.add(robotWalking2);
        		enemies = createEnemies(level3Enemies, levelSprites);
        		break;
        		
        	default:
        		gameEnded = true;
        		break;
        }
        
        restartLevel();
    }
    
    
    
    /**
     *  Calculate collisions between two sprites
     * 
     * @param s1
     * @param s2
     * @return
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	//TODO: Fix the offsets for each character
    	/* Check for collisions between 2 different sprites */
    	float s1X = (s1.getScale() == 1) ? s1.getX() : s1.getX() - tmap.getTileWidth();
    	float s2X = (s2.getScale() == 1) ? s2.getX() : s2.getX() - tmap.getTileWidth();
    	
    	return ((s1X + tmap.getTileWidth()) >= s2X) && (s1X <= s2X + s2.getWidth()) 
    				&& (s1.getY() + s1.getHeight() >= s2.getY()) && (s1.getY() <= s2.getY() + s2.getHeight());   	
    }
    
    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     */
    public boolean checkTileCollision(Sprite s, TileMap tmap, long elapsedTime)
    {
    	// Take a note of a sprite's current position
    	float offset = (s.getScale() == 1) ? 0 : tmap.getTileWidth();
    	float sx = s.getX() - offset;
    	float sy = s.getY();
    	    	
    	boolean collided = false;
    	
    	// Find out how wide and how tall a tile is
    	float tileWidth = tmap.getTileWidth();
    	float tileHeight = tmap.getTileHeight();
    	
    	// Divide the sprite’s x coordinate by the width of a tile, to get
    	// the number of tiles across the x axis that the sprite is positioned at 
    	
    	/* Current velocity of the sprite before collisions */
    	float velY = s.getVelocityY();
 		float velX = s.getVelocityX();
    	
    	/**
    	 *  =========================
    	 * 		  TILE CORNERS
    	 * 	==========================
    	 */
    	
    	/* Left-side X */
    	int	xtileLeft = (int)(sx / tileWidth);
    	
    	/* Top-side Y */
    	int ytileTop  = (int)(sy / tileHeight);
    	
    	/* Right-side X */
    	int xtileRight = (int)((sx + tileWidth) / tileWidth);
    	
    	/* Bottom-side Y */
    	int ytileBottom = (int)((s.getY() + (s.getHeight() - (s.getHeight()/6)))/ tileHeight);
    	
    	/* TOP-LEFT collision */
    	char chTopLeft = tmap.getTileChar(xtileLeft, ytileTop);
    	/* BOTTOM-LEFT collision */
    	char chBottomLeft = tmap.getTileChar(xtileLeft, ytileBottom);
    	/* BOTTOM-RIGHT collision */
    	char chBottomRight = tmap.getTileChar(xtileRight, ytileBottom);
    	/* TOP-RIGHT collision */
    	char chTopRight = tmap.getTileChar(xtileRight, ytileTop);
    	    	
    	/**
    	 *  =========================
    	 * 		PLAYER COLLISION
    	 *  =========================
    	 */
    	if(s == player) {
    		
    		char underLeft = tmap.getTileChar((int)((sx + offset) / tileWidth), ytileBottom+1);
        	char underRight = tmap.getTileChar((int)(((sx + offset) + (tileWidth * s.getScale())) / tileWidth), ytileBottom+1);
        	
        	if((underLeft == '.' && underRight == '.') || (underLeft == '?' && underRight == '?'))
        		isFalling = true;
    		
    		/* Level End */
        	if(chTopLeft == 't' || chBottomLeft == 't' || chTopRight == 't' || chBottomRight == 't') {
        		canMove = false;
        		showHackingMonitor = true;
        		Sound hackMonitor = new Sound("sounds/hacking.wav");
        		hackMonitor.start();
        	}
        	
        	/**
        	 *  =========================
        	 * 		FIX LEFT COLLISION
        	 * 	=========================
        	 */
        	if((chTopLeft != '.' || chBottomLeft != '.') && velX != 0) {
        		
        		/* Place the sprite in a position where it is not colliding */
        		sx += offset - velX * elapsedTime;
         		s.setX(sx);
         		s.setVelocityX(0);
         			
         		/* Update position */
         		chTopLeft = tmap.getTileChar((int)((sx - offset) / tileWidth), ytileTop);
         		chBottomLeft = tmap.getTileChar((int)((sx - offset) / tileWidth), ytileBottom);
         		collided = true;	
        	}
        	
        	/**
        	 *  ==========================
        	 * 		FIX RIGHT COLLISION
        	 *  ==========================
        	 */
        	
        	if((chTopRight != '.' || chBottomRight != '.') && velX != 0) {
        		float offsetRight = (collided) ? 0 : offset;
        		
        		sx -= velX * elapsedTime - offsetRight;
        		s.setX(sx);
         		s.setVelocityX(0);
         		
         		/* Update position */
         		chTopRight = tmap.getTileChar((int)((sx + tileWidth) / tileWidth), ytileTop);
         		chBottomRight = tmap.getTileChar((int)((sx + tileWidth) / tileWidth), ytileBottom);
         		collided = true;	
        	}
        	
        	/**
        	 *  ===========================
        	 * 		FIX BOTTOM COLLISION
        	 *  ===========================
        	 */
        	
        	if((chBottomLeft != '.' || chBottomRight != '.') && velY != 0) {
        		
        		/* Fix position */
         		//sy -= (velY * elapsedTime);
         		s.setY((ytileBottom-.90f) * tileHeight);
         		s.setVelocityY(0);
         		isFalling = false;

         		/* When collides with the ground the player is not longer in the air */
         		inTheAir = false;
        	}
        	
        	/**
        	 * 	============================
        	 * 		 FIX TOP COLLISION
        	 *  ============================
        	 */
        	
        	if((chTopRight != '.' || chTopLeft != '.') && velY != 0) {
        		/* Fix position */
         		sy -= (velY * elapsedTime);
         		s.setY(sy);
         		s.setVelocityY(0);
         		isFalling = false;
         		//s.setVelocityY(0);

        	}
         	     	
         	/* Set Animations when the player is not colliding */
         	if(s.getVelocityX() != 0 && !collided && !moving) {  
         		if(s.getAnimation() != shooting && s.getAnimation() != reloading)
         			s.setAnimation(walking);
        		moving = true;
        	}
        	else if (s.getVelocityX() == 0 && moving) {
        		if(s.getAnimation() != shooting && s.getAnimation() != reloading)
        			s.setAnimation(idle);
        		moving = false;
        	}
    		
    	}
    	/**
    	 *  =========================
    	 * 		 ENEMY COLLISION
    	 *  =========================
    	 */
    	else if(s.getAnimation() == robotWalking || s.getAnimation() == robotWalking2) {
    		
    		/* Check if the enemy has reached the end of the ground */
        	char leftLimit = tmap.getTileChar(xtileLeft, ytileBottom + 1);
        	char rightLimit = tmap.getTileChar(xtileRight, ytileBottom + 1);
        	
        	//System.out.println("Pos left: " + (xtileLeft) + ", " + ytileBottom + " tile: " + leftLimit + " Pos right: " + (xtileRight) + ", " + ytileBottom + " tile: " + rightLimit);

        	if(leftLimit == '.') {
        		
        		/* Fix position in case there was a different scale */
        		if(s.getScale() != -1) {
        			/* Reverse direction and scale */
        			float widthOffset = (s.getWidth() * s.getScale());
            		s.setX(s.getX()+widthOffset);
            		s.setScale(-1);
        		}
        		
        		s.setVelocityX(0.1f);
        	}
        	
        	if(rightLimit == '.') {
        		
        		if(s.getScale() != 1) {
        			/* Reverse direction and scale */
        			float widthOffset = (s.getWidth() * s.getScale());
            		s.setX(s.getX()+widthOffset);
            		s.setScale(1);
        		}
        		
        		s.setVelocityX(-0.1f);
        	}
        	
    	}
    	/**
    	 *  =========================
    	 * 		BULLET COLLISION
    	 *  =========================
    	 */
    	else if(s.getAnimation() == bulletSprite) {
    		
    		if(chTopLeft != '.' || chBottomLeft != '.' || chBottomRight != '.' || chTopRight != '.')
        		return true;
        	else
        		return false;
    	}
    	
    	return false;
    	
    }


    /**
     *  Create the enemies object with the positions and animations given
     *  If the length of the Animation is 1 then every sprite will have the same
     *  animation
     * 
     * @param positions
     * @param anims
     * @return
     */
    public ArrayList<Sprite> createEnemies(int [][] positions, ArrayList<Animation>anims){
    	
    	/* Object that'll be returned */
    	ArrayList<Sprite> enemiesList = new ArrayList<Sprite>();
    	int animCount = -1;
    	
    	/* Create sprites with the data given */
    	for(int i = 0; i < positions.length; i++) {
    		
    		if(positions[i] == null) {
    			enemiesList.add(null);
    			continue;
    		}
    		
    		animCount++;
    		
    		Sprite robot = new Sprite(anims.get(animCount));
    		robot.setPosition(positions[i][0], positions[i][1]);
    		robot.setVelocityX(-0.1f);
    		
    		enemiesList.add(robot);
    		
    		/* If it reaches the end then start */
    		if(animCount == anims.size()-1)
    			animCount = -1;
    	}
    	
    	return enemiesList;
    }
    
   /**
    * Override of the keyPressed event defined in GameCore to catch our
    * own events
    * 
    *  @param e The event that has been generated
    */
   public void keyPressed(KeyEvent e) 
   {   
	   	if(!gameStarted) {
	   		gameStarted = true;
	   		return;
	   	}
	   	int key = e.getKeyCode();
	   	
	   	if (key == KeyEvent.VK_ESCAPE) stop();
	   	
	   	if(key == KeyEvent.VK_9) gameEnded = true;
	   	
	   	if (key == KeyEvent.VK_UP) flap = true;
	   	
	   	if (key == KeyEvent.VK_RIGHT) 
	   		moveRight = true;
	   	
	   	if (key == KeyEvent.VK_LEFT) 
	       	moveLeft = true;
	   	
	   	if(key == KeyEvent.VK_A) 
	   		shoot = true;
	   	
	   	if(key == KeyEvent.VK_SPACE)
	   		jump = true;
	   	
	   	if(key == KeyEvent.VK_F && gameEnded) {
	   		level = 0;
	   		gameEnded = false;
	   		startNewLevel();
	   	}
	   	
   }

    
	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key)
		{
			case KeyEvent.VK_ESCAPE : stop(); break;
			case KeyEvent.VK_RIGHT: 
				moveRight = false;
				if(player.getVelocityX() != 0)
					player.setVelocityX(player.getVelocityX() - velocityX);
				break;
			case KeyEvent.VK_LEFT:
				moveLeft = false;
				if(player.getVelocityX() != 0)
					player.setVelocityX(player.getVelocityX() + velocityX);
				break;
			case KeyEvent.VK_SPACE:
				jump = false;
				break;
			case KeyEvent.VK_A:
				shoot = false;
				break;
			default :  break;
		}
	}

}
