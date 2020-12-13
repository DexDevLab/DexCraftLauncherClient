package net.dex.dexcraft.launcher.client;


import java.io.File;
import java.util.Random;
import javafx.scene.image.Image;
import net.dex.dexcraft.commons.tools.DexCraftFiles;


/**
 *
 *
 */
public class BackgroundRandomizer extends Thread
{

  @Override
  public void run()
  {
    try
    {
      Image bg = null;
      while (bg == null)
      {
        int random = (new Random().nextInt(29))+1;
        File testRandom = new File (DexCraftFiles.resFolder.toString() + "/bg/bg" + random + ".jpg");
        while (!testRandom.exists())
        {
          random = (new Random().nextInt(29))+1;
          testRandom = new File (DexCraftFiles.resFolder.toString() + "/bg" + random + ".jpg");
        }
        bg = new Image(testRandom.toURI().toString());
        // Descomente a linha abaixo para testar um BG específico
//          Image bg = new Image(getClass().getResourceAsStream("backgrounds/bg4.jpg"));





//////////////////////////        bgimage.setPreserveRatio(false);
//////////////////////////        bgimage.setFitWidth(Client.switchStage.getWidth());
//////////////////////////        bgimage.setFitHeight(Client.switchStage.getHeight());
//////////////////////////        bgimage.setImage(bg);





        Client.logger.log("INFO", "Aplicado background bg" + random);
        Thread.sleep(40000);
        bg = null;
      }
    }
    catch (InterruptedException ex)
    {
      Client.alerts.exceptionHandler(ex, "EXCEÇÃO EM BackgroundRandomizer.run()");
    }
  }



}
