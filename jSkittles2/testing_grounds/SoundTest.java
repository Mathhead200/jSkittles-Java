package testing_grounds;

import java.io.*;
import javax.sound.sampled.*;


public class SoundTest
{
	public static void main(String[] args) throws Exception {
		File file = new File("rsc/sounds/eating.wav");
		AudioInputStream ain = AudioSystem.getAudioInputStream(file);
		DataLine.Info info = new DataLine.Info( Clip.class, ain.getFormat() );
		Clip clip = (Clip)AudioSystem.getLine(info);
		clip.open(ain);
		for( int i : new int[5] ) {
			clip.start();
			do
				Thread.sleep(100);
			while( clip.isRunning() );
			clip.setFramePosition(0);
		}
		clip.close();
	}
}
