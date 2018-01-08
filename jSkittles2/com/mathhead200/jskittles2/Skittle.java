package com.mathhead200.jskittles2;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;


public class Skittle implements Edible
{
	private static Random rand = new Random();
	private static final Color[] COLORS = { Color.RED, Color.GREEN, Color.BLUE,
	                                        Color.ORANGE, Color.MAGENTA };
	public final ColoringImage img;

	public Skittle() throws IOException {
		img = new ColoringImage( ImageIO.read(new File("rsc/icons/skittle.dmi")) );
		img.colorIn( COLORS[rand.nextInt(COLORS.length)] );
	}

	public void eatenBy(final Jay jay) {
		if( jay.getImg().getColor().equals(getImg().getColor()) ) {
			jay.killWithSound();
		} else {
			jay.getImg().colorIn( getImg().getColor() );
		}
	}

	public ColoringImage getImg() {
		return img;
	}
}
