package com.mathhead200.jskittles2.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import com.mathhead200.jskittles2.*;


public class JSkittles2
{
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		File settingsFile = new File("settings.properties");
		Properties settings = new Properties();
		if( settingsFile.isFile() ) {
			FileInputStream fin = new FileInputStream( settingsFile );
			settings.load( fin );
			fin.close();
		} else {
			settings.put( "width", "18" );
			settings.put( "height", "11" );
			settings.put( "speed.jays", "150" );
			settings.put( "speed.skittles", "500" );
			settings.put( "refreash", "40" );
			FileOutputStream fout = new FileOutputStream( settingsFile );
			settings.store( fout, "Settings for JSkittles II" );
			fout.close();
		}
		JSkittlesCanvas canvas;
		{
			int width = Integer.parseInt( settings.getProperty("width", "18") ),
			    height = Integer.parseInt( settings.getProperty("height", "11") );
			canvas = new JSkittlesCanvas( new Dimension(width, height) );
		}
		int jaySpeed = Integer.parseInt( settings.getProperty("speed.jays", "150") ),
		    skittleSpeed = Integer.parseInt( settings.getProperty("speed.skittles", "500") ),
		    refreash = Integer.parseInt( settings.getProperty("refreash", "40") );
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if( screenSize.width < canvas.getWidth() + 100 || screenSize.height < canvas.getHeight() + 100 ) {
			JOptionPane.showConfirmDialog( null,
			                               "This program can not run on rezolutions less then "
			                               	+ (canvas.getWidth() + 100) + " by " + (canvas.getHeight() + 100)
			                               	+ "\nTry reducing the width or height in the "
			                               	+ "\"settings.properties\" file",
			                               "Error",
			                               JOptionPane.OK_OPTION,
			                               JOptionPane.ERROR_MESSAGE );
			System.exit(1);
		}
		Thread moveLefty = new Thread( canvas.new MoveLefty(jaySpeed) ),
		       moveRighty = new Thread( canvas.new MoveRighty(jaySpeed) ),
	           genSkittles = new Thread( canvas.new GenerateEdibles(skittleSpeed) );
		JFrame frame = new JFrame("jSkittles II");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane( new JPanel(new GridBagLayout()) );
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			frame.getContentPane().add(canvas, gbc);
		}
		frame.getContentPane().setBackground(Color.BLACK);
		frame.pack();
		frame.setSize( frame.getSize().width + 50, frame.getSize().height + 50 );
		frame.setLocation( (screenSize.width - frame.getSize().width) / 2,
		                   (screenSize.height - frame.getSize().height) / 2 );
		// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.addKeyListener(canvas);
		moveLefty.start();
		moveRighty.start();
		genSkittles.start();
		while( (moveLefty.isAlive() || moveRighty.isAlive()) && genSkittles.isAlive() ) {
			try {
				canvas.repaint();
				Thread.sleep(refreash);
			} catch (InterruptedException e) {
				break;
			}
		}
		canvas.repaint();
		if( !genSkittles.isAlive() ) {
			if( moveRighty.isAlive() )
				moveRighty.interrupt();
			if( moveLefty.isAlive() )
				moveLefty.interrupt();
			//board filled up, too many skittles
			System.out.println("Board filled...");
		} else if( !(moveLefty.isAlive() && moveLefty.isAlive()) ) {
			if( genSkittles.isAlive() )
				genSkittles.interrupt();
			//both Jays died, Game Over!
			System.out.println("Both Lefty and Righty died, aww...");
		} else {
			//...Interrupted?
		}
	}
}
