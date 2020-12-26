package net.dex.dexcraft.launcher.client.services;


import javafx.application.Platform;
import javafx.concurrent.Task;
import static net.dex.dexcraft.commons.Commons.alerts;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.launcher.client.MainWindowController;


/**
 * Main Window Service. Perform all client-side verifications,<br>
 */
public class MainService extends Task<Void>
{
  public DexUI ui;
  public int serverIndex = MainWindowController.serverIndex;
  private String componentName;
  private static boolean isReady = false;

  /**
   * Set UI which Service will interact.
   * @param userInterface the DexUI instance.
   */
  public void setUI(DexUI userInterface)
  {
    this.ui = userInterface;
  }

  /**
   * Caller method.
   * @return null (ignored).
   */
  @Override
  protected Void call()
  {
    logger.log("INFO", "SERVIÇO: Iniciando o Serviço " + "MainService" + "...");
    switch (serverIndex)
    {
      case 0:
        componentName = "DCGame";
        break;
      case 1:
        componentName = "DCPXGame";
        break;
      case 2:
        componentName = "DCVNGame";
        break;
      case 3:
        componentName = "DCBGame";
        break;
      default:
        break;
    }
    mainRoutine();
    logger.log("INFO", "SERVIÇO: Finalizando o Serviço " + "MainService" + "...");
    return null;
  }

  /**
   * Main service method.
   */
  private void mainRoutine()
  {
    // Verify Java
    isReady = Validate.javaVersion(ui);
    waitService();
    isReady = false;
    // Verify Game Client version
    isReady = Validate.provisionedComponent(ui, componentName, 0);
    waitService();
    isReady = false;
    // Changes the component name, according to the selected server
    //  index.
    //  0 - DexCraft Factions Game Patch
    //  1 - DexCraft Pixelmon Game Patch
    //  2 - DexCraft Vanilla Game Patch
    //  3 - DexCraft Beta Game Patch
    //
    switch (serverIndex)
    {
      case 0:
        componentName = "DCPatchGame";
        break;
      case 1:
        componentName = "DCPXPatchGame";
        break;
      case 2:
        componentName = "DCVNPatchGame";
        break;
      case 3:
        componentName = "DCBPatchGame";
        break;
      default:
        break;
    }
    // Verify Game Patch version
    isReady = Validate.provisionedComponent(ui, componentName, 0);
    waitService();
    isReady = false;
    // Changes the component name, according to the selected server
    //  index, to match properly with the FTP directory name
    //  0 - DexCraft Factions Game Patch
    //  1 - DexCraft Pixelmon Game Patch
    //  2 - DexCraft Vanilla Game Patch
    //  3 - DexCraft Beta Game Patch
    //

//    switch (serverIndex)
//    {
//      case 0:
//        componentName = "dc";
//        break;
//      case 1:
//        componentName = "dcpx";
//        break;
//      case 2:
//        componentName = "dcvn";
//        break;
//      case 3:
//        componentName = "dcb";
//        break;
//      default:
//        break;
//    }
//    AccountSyncService sync = new AccountSyncService();
//    sync.setUI(ui);
//    sync.setComponentName(componentName);
//    new Thread(sync).start();
    waitService();
//    while (sync.isRunning())
//    {
//      try
//      {
//        Thread.sleep(1000);
//      }
//      catch (InterruptedException ex)
//      {
//        logger.log(ex,"EXCEÇÃO em MainService.mainRoutine()");
//      }
//    }
    Platform.runLater(() ->
    {
      ui.setMainButtonDisable(false);
      ui.getMenuBar().setDisable(false);
      ui.resetProgress();
      ui.changeMainLabel("");
      ui.changeSecondaryLabel("");
      ui.getProgressBar().setVisible(false);
    });
  }

  /**
   * Simple method to make a waiter to service.
   */
  public void waitService()
  {
    while (!isReady)
    {
      try
      {
        Thread.sleep(1000);
        logger.log("INFO", "SERVIÇO: Aguardando status do Serviço...");
      }
      catch (InterruptedException ex)
      {
        alerts.exceptionHandler(ex, "EXCEÇÃO em MainService.waitService()");
      }
    }
  }


}
