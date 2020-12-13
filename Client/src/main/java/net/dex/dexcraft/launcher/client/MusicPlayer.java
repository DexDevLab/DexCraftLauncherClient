package net.dex.dexcraft.launcher.client;


import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.dex.dexcraft.commons.tools.DexCraftFiles;


/**
 *
 */
public class MusicPlayer extends Thread
{
  public File musicFile = new File ("");
  public AudioInputStream audioInput;
  public Clip clip;

  public float VOLUME = 0.7f;

  @Override
  public void run()
  {
    Client.logger.log("INFO", "Iniciando Thread MusicPlayer...");
    if (DexCraftFiles.resFolder.exists())
    {
      while (DexCraftFiles.resFolder.exists())
      {
        int random = (new Random().nextInt(12))+1;
        musicFile = new File (DexCraftFiles.resFolder.toString() + "/sound/" + random + ".wav");
        playMusic(musicFile);
      }
    }
    else
    {
      Client.logger.log("ERRO", "BACKGROUND PLAYER: NÃO É POSSÍVEL ENCONTRAR ARQUIVOS DE MÍDIA NO DIRETÓRIO DE RECURSOS.");
    }
    Client.logger.log("INFO", "Encerrando Thread MusicPlayer...");
  }


  public void playMusic(File musicFile)
  {
    try
    {
      Client.logger.log("INFO", "BACKGROUND PLAYER: Reproduzindo arquivo de áudio " + musicFile + "...");
      audioInput = AudioSystem.getAudioInputStream(musicFile);
      clip = AudioSystem.getClip();
      clip.open(audioInput);
      FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      float range = gainControl.getMaximum() - gainControl.getMinimum();
      float gain = (range * VOLUME) + gainControl.getMinimum();
      gainControl.setValue(gain);
      clip.start();
      Thread.sleep(10000);
      while (clip.isRunning())
      {
        Thread.sleep(500);
      }
      clip.stop();
      clip.close();
      clip.flush();
    }
    catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException ex)
    {
      Client.alerts.exceptionHandler(ex, "EXCEÇÃO EM BackgroundPlayer.playMusic(File)");
    }
  }



}
