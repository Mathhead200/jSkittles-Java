package com.mathhead200.jskittles2;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;


public final class Jay
{
	public static final int
		RIGHT = 0,
		DOWN = 1,
		LEFT = 2,
		UP = 3;

	private ColoringImage img;
	private int dir;
	private boolean dying = false;
	private boolean dead = false;
	private ColoringImage eatingImg;
	private Clip eatingClip;
	private Clip dyingClip;
	public Point loc = new Point(0, 0);
	public Point offset = new Point(0, 0);
	public final int minOffsetX;
	public final int minOffsetY;
	public final int maxOffsetX;
	public final int maxOffsetY;

	private Jay(int dir) throws IOException, UnsupportedAudioFileException {
		img = new ColoringImage( ImageIO.read(new File("rsc/icons/jay.dmi")) );
		eatingImg = new ColoringImage( ImageIO.read(new File("rsc/icons/jay_eating.dmi")) );
		this.dir = dir;
		minOffsetX = -img.getWidth() / 2;
		minOffsetY = -img.getHeight() / 2;
		maxOffsetX = (img.getWidth() + 1) / 2;
		maxOffsetY = (img.getHeight() + 1) / 2;
		eatingClip = Sounds.openClip("rsc/sounds/eating.wav");
		eatingClip.setFramePosition(0);
		dyingClip = Sounds.openClip("rsc/sounds/pac man dies.wav");
		dyingClip.setFramePosition(0);
	}


	public static Jay makeLefty(Dimension canvasDim) throws IOException, UnsupportedAudioFileException {
		Jay lefty = new Jay(RIGHT);
		lefty.loc = new Point( canvasDim.width / 4, canvasDim.height / 2 );
		return lefty;
	}

	public static Jay makeRighty(Dimension canvasDim) throws IOException, UnsupportedAudioFileException {
		Jay righty = new Jay(LEFT);
		for( int y = 0; y < righty.img.getHeight(); y++ )
			for( int x = 0; x < righty.img.getWidth() / 2; x++ ) {
				int temp = righty.img.getRGB( righty.img.getWidth() - 1 - x, y );
				righty.img.setRGB( righty.img.getWidth() - 1 - x, y, righty.img.getRGB(x, y) );
				righty.img.setRGB( x, y, temp );
			}
		righty.loc = new Point( 3 * canvasDim.width / 4, canvasDim.height / 2 );
		return righty;
	}


	public ColoringImage getImg() {
		return img;
	}

	public int getDir() {
		return dir;
	}

	public void changeDir(int newDir) {
		if( dir == newDir )
			return;
		AffineTransform tx = new AffineTransform();
		tx.rotate( (newDir - dir) * Math.PI / 2, img.getWidth() / 2, img.getHeight() / 2 );
		AffineTransformOp op = new AffineTransformOp( tx, AffineTransformOp.TYPE_BILINEAR );
		img = new ColoringImage( op.filter(img, null), img.getColor() );
		dir = newDir;
	}

	public boolean isDead() {
		return dead;
	}

	public boolean isDying() {
		return dying;
	}

	public void killWithSound() {
		Thread dyingThread = new Kill();
		dyingThread.start();
		dyingClip.start();
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while( dyingClip.isActive() );
		dyingThread.interrupt();
	}

	public void eatWithSound() {
		new Thread() {
			public void run() {
				Thread eatingThread = new Eat();
				eatingThread.start();
				synchronized(eatingClip) {
					eatingClip.start();
					do {
						try {
							Thread.sleep(33);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} while( eatingClip.isActive() );
					eatingClip.setFramePosition(0);
				}
				eatingThread.interrupt();
			}
		}.start();
	}

	public class Kill extends Thread
	{
		public void run() {
			dying = true;
			Random rand = new Random();
			try {
				while(true) {
					img.colorIn( new Color(rand.nextInt(0xffffff)) );
					Thread.sleep(100);
				}
			} catch(InterruptedException e) {
			} finally {
				dead = true;
				dyingClip.close();
				eatingClip.close();
			}
		}
	}

	public class Eat extends Thread
	{
		public void run() {
			synchronized(eatingImg) {
				final ColoringImage backupImg = img;
				eatingImg.colorIn( img.getColor() );
				img = eatingImg;
				try {
					while(true)
						Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
				} finally {
					backupImg.colorIn( img.getColor() );
					img = backupImg;
				}
			}
		}
	}
}
