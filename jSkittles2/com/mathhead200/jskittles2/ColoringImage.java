package com.mathhead200.jskittles2;

import java.awt.Color;
import java.awt.image.BufferedImage;


public class ColoringImage extends BufferedImage
{
	private Color color;

	public ColoringImage(BufferedImage img, Color color) {
		super( img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR );
		for( int y = 0; y < getHeight(); y++ )
			for( int x = 0; x < getWidth(); x++ )
				setRGB( x, y, img.getRGB(x, y) );
		this.color = color;
	}

	public ColoringImage(BufferedImage img) {
		this( img, Color.WHITE );
	}

	public Color getColor() {
		return color;
	}

	public synchronized void colorIn(Color newColor) {
		if( color.equals(newColor) )
			return;
		for( int y = 0; y < getHeight(); y++ )
			for( int x = 0; x < getWidth(); x++ )
				if( getRGB(x, y) == color.getRGB() )
					setRGB( x, y, newColor.getRGB() );
		this.color = newColor;
	}
}
