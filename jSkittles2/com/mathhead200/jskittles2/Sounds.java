package com.mathhead200.jskittles2;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class Sounds
{
	public static Clip openClip(String path) throws UnsupportedAudioFileException, IOException {
		AudioInputStream audioIn =  AudioSystem.getAudioInputStream( new File(path) );
		DataLine.Info info = new DataLine.Info( Clip.class, audioIn.getFormat() );
		Clip clip = null;
		try {
			clip = (Clip)AudioSystem.getLine(info);
			clip.open(audioIn);
			return clip;
		} catch(LineUnavailableException e) {
			e.printStackTrace();
		} catch(IOException e) {
			clip.close();
			e.printStackTrace();
		}
		return clip;
	}
}