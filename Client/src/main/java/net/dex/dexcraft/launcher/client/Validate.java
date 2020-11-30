package net.dex.dexcraft.launcher.client;

import java.io.File;
import java.io.IOException;
import net.dex.dexcraft.launcher.check.ProvisionedPackage;
import static net.dex.dexcraft.launcher.client.Client.alerts;
import static net.dex.dexcraft.launcher.client.Client.changeStatus;
import static net.dex.dexcraft.launcher.client.Client.logger;
import static net.dex.dexcraft.launcher.client.Client.ui;
import net.dex.dexcraft.launcher.tools.DexCraftFiles;
import net.dex.dexcraft.launcher.tools.Download;
import net.dex.dexcraft.launcher.tools.FileIO;
import net.dex.dexcraft.launcher.tools.Install;
import net.dex.dexcraft.launcher.tools.JSONUtility;
import org.apache.commons.io.*;

/**
 * Wrap the update check, download, install and
 * verify processes in one single class, with UI
 * update.
 */
public class Validate
{

  public static String dexCraftLauncherInitVersion;
  public static String dexCraftLauncherClientVersion;
  public static String dexCraftBackgroundServicesVersion;
  public static String dexCraftFactionsPatchVersion;
  public static String dexCraftPixelmonPatchVersion;
  public static String dexCraftVanillaPatchVersion;
  public static String dexCraftBetaPatchVersion;
  public static boolean offlineMode;

  private static JSONUtility ju = new JSONUtility();


  public static void versions()
  {
    dexCraftLauncherInitVersion = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "DexCraftLauncherInitVersion");
    dexCraftLauncherClientVersion = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "DexCraftLauncherVersion");
    dexCraftBackgroundServicesVersion = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "DexCraftBackgroundServicesVersion");
    dexCraftFactionsPatchVersion = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "DCPatchVersion");
    dexCraftPixelmonPatchVersion = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "DCPXPatchVersion");
    dexCraftVanillaPatchVersion = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "DCVNPatchVersion");
    dexCraftBetaPatchVersion = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "DCBPatchVersion");
  }

  public static String getLastServer()
  {
    return ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "LastServerIndex");
  }

  public static void setLastServer(Integer index)
  {
    ju.editValue(DexCraftFiles.launcherProperties, "LauncherProperties", "LastServerIndex", Integer.toString(index));
  }

  public static void setOfflineMode()
  {
    offlineMode = ju.readValue(DexCraftFiles.launcherProperties, "LauncherProperties", "OfflineMode").equals("true");
  }

  /**
   * Validate the Launcher resources, which are the
   * program backgrounds, runtime sounds etc.
   */
  public static void resources()
  {
    //Check if resource folder already exist. If not, download the resources again
    if ( (!DexCraftFiles.resFolder.exists()) || (DexCraftFiles.resFolder.listFiles().length == 0) )
    {
      changeStatus("Baixando recursos...");
      String resURL = ju.readValue(DexCraftFiles.coreFile, "LauncherUpdates", "LauncherResourceFile");
      Download downloadRes = new Download();
      //Start a separated thread for download
      Thread threadDownloadRes = new Thread(()->
      {
        downloadRes.zipResource(resURL, DexCraftFiles.tempFolder, DexCraftFiles.resZip);
      });
      threadDownloadRes.start();
      while(threadDownloadRes.isAlive())
      {
        //UI output during download process
        try
        {
          Thread.sleep(1100);
        }
        catch (InterruptedException ex)
        {
          logger.log(ex, "EXCEÇÃO em Validate.resources()");
        }
        logger.log("INFO", downloadRes.getTimeEstimatedMsg());
        changeStatus("Baixando recursos... " + downloadRes.getProgressPercent() + "% concluído");
      }
      ui.changeProgress(true, 60, 40);
      changeStatus("Instalando recursos...");
      Install installRes = new Install();
      //Start a separated thread for installing (zip file extraction)
      Thread threadInstallRes = new Thread(()->
      {
        installRes.downloadedZipResource(DexCraftFiles.resZip, DexCraftFiles.resFolder);
      });
      threadInstallRes.start();
      String checkFile = " ";
      while(threadInstallRes.isAlive())
      {
        //UI output during installing process
        if (!(installRes.getInstallingFileName()).equals(""))
        {
          if (!installRes.getInstallingFileName().equals(checkFile))
          {
            changeStatus("Instalando recursos... " + installRes.getInstallingFilePosition() + " / " + installRes.getTotalFilesQuantity());
            checkFile = installRes.getInstallingFileName();
          }
          if (Integer.parseInt(installRes.getInstallingFilePosition()) == (Integer.parseInt(installRes.getTotalFilesQuantity())-1))
          {
            changeStatus("Instalando recursos... " + installRes.getTotalFilesQuantity() + " / " + installRes.getTotalFilesQuantity());
          }
        }
        try
        {
          Thread.sleep(25);
        }
        catch (InterruptedException ex)
        {
          logger.log(ex, "EXCEÇÃO em Validate.resources()");
        }
      }
      // throws an error if something happened and the resource folder isn't in its place
      if ( (!DexCraftFiles.resFolder.exists()) || (DexCraftFiles.resFolder.listFiles().length == 0) )
      {
        logger.log("***ERRO***", "RECURSO PROVISIONADO INDISPONÍVEL");
        alerts.tryAgain();
      }
    }
    logger.log("INFO", "Recursos instalados. Validando atalhos...");
    /** Check if program shortcuts were created properly. If not, they will be installed:
        desktop for all users, start menu and current user's desktop **/
    if ( (!DexCraftFiles.shortcutDefaultDesktop.exists()) | (!DexCraftFiles.shortcutProgramFolder.exists()) | (!DexCraftFiles.shortcutUserDesktop.exists()) )
    {
      FileIO file = new FileIO();
      file.copiar(DexCraftFiles.shortcutSrc, DexCraftFiles.shortcutDefaultDesktop);
      file.copiar(DexCraftFiles.shortcutSrc, DexCraftFiles.shortcutProgramFolder);
      try
      {
        FileUtils.copyFile(DexCraftFiles.shortcutSrc, DexCraftFiles.shortcutUserDesktop);
      }
      catch (IOException ex)
      {
        logger.log(ex, "EXCEÇÃO em Validate.resources()");
      }
    }
  }


  /**
   * Validates a provisioned component.<br>
   * A provisioned component is a package with data
   * which needs to be updated or compared with some
   * local version.<br>
   * This method checks if the current component is
   * updated, and if not, downloads and install it.
   * @param coreFile the script file containing the version data
   * @param versionFile the local script file containing the current
   * version data
   * @param componentName the provisioned component's name
   * @param objectName the provisioned component's JSON key. <br>
   * Both script files (the online one and the local one)
   * MUST use the same category, with the exactly same name.
   * @param coreFileCategory the provisioned component's JSON object
   * (category), on coreFile.
   * @param versionFileCategory the provisioned component's JSON object
   * (category), on the versionFile.
   * @param componentCategoryURL the provisioned component's JSON object
   * (category) on the coreFile, which contains the objectURL.
   * @param objectURL the provisioned component's JSON key
   * (category) on the coreFile,<br> which contains the update URL to
   * download.
   * @param destinationDownloadDir the folder which the file will be
   * downloaded to
   * @param destinationDownloadFile the component update file
   * @param destinationInstallDir the directory to install the update
   */
  public static void provisionedComponent(File coreFile, File versionFile, String componentName,
                                          String objectName, String coreFileCategory,
                                          String versionFileCategory,
                                          String componentCategoryURL,
                                          String objectURL,
                                          File destinationDownloadDir,
                                          File destinationDownloadFile, File destinationInstallDir)
  {
    String getProvisionedVersion = ju.readValue(coreFile, coreFileCategory , objectName);
    if(ProvisionedPackage.isOutdated(versionFile,versionFileCategory, objectName, getProvisionedVersion ))
    {
      changeStatus("Baixando atualização - " + componentName + "...");
      String componentURL = ju.readValue(coreFile, componentCategoryURL , objectURL);
      Download downloadComponent = new Download();
      Thread threadDownloadComponent = new Thread(()->
      {
        downloadComponent.zipResource(componentURL, destinationDownloadDir, destinationDownloadFile);
      });
      threadDownloadComponent.start();
      while(threadDownloadComponent.isAlive())
      {
        try
        {
          Thread.sleep(1100);
        }
        catch (InterruptedException ex)
        {
          logger.log(ex, "EXCEÇÃO em Validate.launcher()");
        }
        logger.log("INFO", downloadComponent.getTimeEstimatedMsg());
        changeStatus("Baixando " + componentName + "..." + downloadComponent.getProgressPercent() + "% concluído");
      }
      changeStatus("Baixando " + componentName + "..." + "100% concluído");
      ui.changeProgress(true, 80, 40);
      Install installComponent = new Install();
      Thread threadInstallComponent = new Thread(()->
      {
        installComponent.downloadedZipResource(destinationDownloadFile, destinationInstallDir);
      });
      threadInstallComponent.start();
      String checkFile = " ";
      while(threadInstallComponent.isAlive())
      {
        if (!(installComponent.getInstallingFileName()).equals(""))
        {
          if (!installComponent.getInstallingFileName().equals(checkFile))
          {
            changeStatus("Instalando - " + componentName + "..." + installComponent.getInstallingFilePosition() + " / " + installComponent.getTotalFilesQuantity());
            checkFile = installComponent.getInstallingFileName();
          }
        }
        try
        {
          Thread.sleep(10);
        }
        catch (InterruptedException ex)
        {
          logger.log(ex,"EXCEÇÃO em Validate.provisionedComponent()");
        }
      }
      changeStatus("Instalando - " + componentName + "..." + installComponent.getTotalFilesQuantity() + " / " + installComponent.getTotalFilesQuantity());
      ju.editValue(versionFile, versionFileCategory, objectName, getProvisionedVersion);
    }
  }

}
