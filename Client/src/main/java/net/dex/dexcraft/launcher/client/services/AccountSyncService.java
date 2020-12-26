package net.dex.dexcraft.launcher.client.services;


import javafx.concurrent.Task;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.tools.DexUI;


/**
 *
 */
public class AccountSyncService extends Task<Void>
{
  public DexUI ui;
  public String component;
  public boolean isBackupValidated = false;

  public void setUI(DexUI userInterface)
  {
    this.ui = userInterface;
  }

  public void setComponentName(String name)
  {
    this.component = name;
  }



  @Override
  protected Void call()
  {
    logger.log("INFO", "SERVIÇO: Iniciando o Serviço " + "AccountSyncService" + "...");
    mainRoutine();

    logger.log("INFO", "SERVIÇO: Finalizando o Serviço " + "AccountSyncService" + "...");
    return null;
  }

  public void mainRoutine()
  {

  }


}
