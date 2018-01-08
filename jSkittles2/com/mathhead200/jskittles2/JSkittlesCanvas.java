package com.mathhead200.jskittles2;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;


@SuppressWarnings("serial")
public class JSkittlesCanvas extends Canvas implements KeyListener
{
	private Jay lefty;
	private Jay righty;
	private Map<Point, Edible> edibles;
	private BufferedImage imageBuffer;
	public final Dimension dim;

	public JSkittlesCanvas(Dimension dim) throws IOException, UnsupportedAudioFileException {
		this.dim = dim;
		lefty = Jay.makeLefty(dim);
		righty = Jay.makeRighty(dim);
		edibles = new HashMap<Point, Edible>( dim.width * dim.height );
		setBackground( Color.WHITE );
		setSize( new Dimension(dim.width * squareWidth(), dim.height * squareHeight()) );
		imageBuffer = new BufferedImage( getSize().width, getSize().height, BufferedImage.TYPE_4BYTE_ABGR );
	}

	private void drawJay(Graphics2D g, Jay jay) {
		if( !jay.isDead() ) {
			ColoringImage img = jay.getImg();
			g.drawImage( img, jay.loc.x * img.getWidth() + jay.offset.x,
		             jay.loc.y * img.getHeight() + jay.offset.y, null );
		}
	}

	protected void repaintBuffer() {
		synchronized(imageBuffer) {
			Graphics2D g = imageBuffer.createGraphics();
			g.setBackground( getBackground() );
			g.clearRect( 0, 0, imageBuffer.getWidth(), imageBuffer.getHeight() );
			drawJay(g, lefty);
			drawJay(g, righty);
			synchronized(edibles) {
				for( Point loc : edibles.keySet() ) {
					BufferedImage img = edibles.get(loc).getImg();
					g.drawImage( img, loc.x * img.getWidth(), loc.y * img.getHeight(), null );
				}
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		switch( e.getKeyCode() ) {
		 case 87: //w
			lefty.changeDir(Jay.UP);
			break;
		 case 65: //a
			lefty.changeDir(Jay.LEFT);
			break;
		 case 83: //s
			lefty.changeDir(Jay.DOWN);
			break;
		 case 68: //d
			lefty.changeDir(Jay.RIGHT);
			break;
		 case 73: //i
		 case 38: //(up arrow)
			righty.changeDir(Jay.UP);
			break;
		 case 74: //j
		 case 37: //(left arrow)
			righty.changeDir(Jay.LEFT);
			break;
		 case 75: //k
		 case 40: //(down arrow)
			righty.changeDir(Jay.DOWN);
			break;
		 case 76: //l
		 case 39: //(right arrow)
			righty.changeDir(Jay.RIGHT);
			break;
		}
		repaintBuffer();
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public void update(Graphics g) {
		synchronized(imageBuffer) {
			g.drawImage( imageBuffer, 0, 0, null );
		}
		getToolkit().sync();
	}

	public void paint(Graphics g) {
		update(g);
	}

	public int squareWidth() {
		return lefty.getImg().getWidth();
	}

	public int squareHeight() {
		return lefty.getImg().getHeight();
	}

	public abstract class MoveJay implements Runnable
	{
		public final Jay jay;
		public final int speed;

		public MoveJay(Jay jay, int speed) {
			this.jay = jay;
			this.speed = speed;
		}

		private void changedLoc(Jay jay) {
			Point jLoc = jay.loc;
			if( jLoc.x < 0 || jLoc.y < 0 || jLoc.x >= dim.width || jLoc.y >= dim.height )
				jay.killWithSound();
			synchronized(edibles) {
				if( edibles.containsKey(jLoc) ) {
					jay.eatWithSound();
					edibles.get(jLoc).eatenBy(jay);
					edibles.remove(jLoc);
				}
			}
		}

		public void run() {
			while( !jay.isDying() ) {
				try {
					switch( jay.getDir() ) {
					 case Jay.UP:
						Thread.sleep( speed / jay.getImg().getHeight() );
						jay.offset.y--;
						if( jay.offset.y < jay.minOffsetY ) {
							jay.offset.y = jay.maxOffsetY;
							jay.loc.y--;
							changedLoc(jay);
						}
						break;
					 case Jay.LEFT:
						Thread.sleep( speed / jay.getImg().getWidth() );
						jay.offset.x--;
						if( jay.offset.x < jay.minOffsetX ) {
							jay.offset.x = jay.maxOffsetX;
							jay.loc.x--;
							changedLoc(jay);
						}
						break;
					 case Jay.DOWN:
						Thread.sleep( speed / jay.getImg().getHeight() );
						jay.offset.y++;
						if( jay.offset.y > jay.maxOffsetY ) {
							jay.offset.y = jay.minOffsetY;
							jay.loc.y++;
							changedLoc(jay);
						}
						break;
					 case Jay.RIGHT:
						Thread.sleep( speed / jay.getImg().getWidth() );
						jay.offset.x++;
						if( jay.offset.x > jay.maxOffsetX ) {
							jay.offset.x = jay.minOffsetX;
							jay.loc.x++;
							changedLoc(jay);
						}
						break;
					}
					repaintBuffer();
				} catch(InterruptedException e) {
					return;
				}
			}
			while( !jay.isDead() ) {
				try {
					Thread.sleep(100);
					repaintBuffer();
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	public class MoveLefty extends MoveJay
	{
		public MoveLefty(int speed) {
			super( lefty, speed );
		}
	}

	public class MoveRighty extends MoveJay
	{
		public MoveRighty(int speed) {
			super( righty, speed );
		}
	}

	public class GenerateEdibles implements Runnable
	{
		private Random rand = new Random();
		public final int speed;

		public GenerateEdibles(int speed) {
			this.speed = speed;
		}

		public void run() {
			while(true) {
				try {
					Thread.sleep(speed);
					synchronized(edibles) {
						int pos = 0;
						try {
							pos = rand.nextInt(dim.width * dim.height - edibles.size());
						} catch(IllegalArgumentException e) {
							break;
						}
						for( int sPos = 0; sPos < dim.width * dim.height; sPos++ ) {
							Point sLoc = new Point( sPos % dim.width, sPos / dim.width );
							if( edibles.containsKey(sLoc) && pos >= sPos )
								pos++;
						}
						Point loc = new Point( pos % dim.width, pos / dim.width );
						Skittle skittle = new Skittle();
						edibles.put( loc, skittle );
					}
					repaintBuffer();
				} catch(InterruptedException e) {
					break;
				} catch(IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}
}
