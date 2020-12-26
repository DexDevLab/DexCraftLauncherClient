package net.dex.dexcraft.launcher.client.services;


import java.io.File;
import java.util.Random;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import static net.dex.dexcraft.launcher.client.MainWindowController.mainUI;


/**
 * Randomly changes background image on Scene.
 */
public class BgImageRandomService extends Task<Void>
{
  public long BG_COUNTDOWN_SECS = 45;

  /**
   * Caller and main method.
   * @return null (ignored)
   */
  @Override
  protected Void call()
  {
    logger.log("INFO", "SERVIÇO: Iniciando o Serviço " + "BackgroundRandomizer" + "...");
    Thread thread = new Thread(new BackgroundRandomizer());
    thread.setDaemon(true);
    thread.start();
    while (!Validate.bgImageRandomizerCaller.equals("null"))
    {
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException ex)
      {
        // Thread interruption ignored
//        alerts.exceptionHandler(ex, "EXCEÇÃO em BgImageRandomService.call()");
      }
      if (Validate.bgImageRandomizerCaller.equals("null"))
      {
        thread.interrupt();
      }
    }
    return null;
  }


  /**
   * Randomizes and shows a different background wallpaper in window.
   */
  public class BackgroundRandomizer extends Thread
  {
    /**
     * Main method.
     */
    @Override
    public void run()
    {
      try
      {
        while (!Validate.bgImageRandomizerCaller.equals("null"))
        {
          int random = (new Random().nextInt(20))+1;
          File testRandom = new File (DexCraftFiles.resFolder.toString() + "/bg/bg" + random + ".jpg");
          final Image bg = new Image(testRandom.toURI().toString());

          // Descomente a linha abaixo para testar um BG específico
  //          Image bg = new Image(getClass().getResourceAsStream("backgrounds/bg4.jpg"));

          Platform.runLater(()->
          {
            mainUI.setImage(bg);
          });
          logger.log("INFO", "Aplicado background bg" + random);
          Thread.sleep(BG_COUNTDOWN_SECS * 1000);
        }
      }
      catch (InterruptedException ex)
      {
        logger.log("INFO", "SERVIÇO: Thread BackgroundRandomizer interrompida.");
      }
      logger.log("INFO", "SERVIÇO: Serviço " + "BackgroundRandomizer" + " terminado.");
    }
  }

}
