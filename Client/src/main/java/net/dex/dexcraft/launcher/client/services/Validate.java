package net.dex.dexcraft.launcher.client.services;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.dex.dexcraft.commons.check.AdminExecution;
import net.dex.dexcraft.commons.check.PreventSecondInstance;
import net.dex.dexcraft.commons.check.ProvisionedPackage;
import net.dex.dexcraft.commons.check.SystemRequirements;
import net.dex.dexcraft.commons.dao.JsonDAO;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.dto.SystemDTO;
import net.dex.dexcraft.commons.dto.UrlsDTO;
import net.dex.dexcraft.commons.dto.VersionsDTO;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.commons.tools.Download;
import net.dex.dexcraft.commons.tools.FileIO;
import net.dex.dexcraft.commons.tools.Install;
import net.dex.dexcraft.commons.tools.ZipUtils;
import net.dex.dexcraft.launcher.client.Client;
import static net.dex.dexcraft.launcher.client.Client.alerts;
import static net.dex.dexcraft.launcher.client.Client.logger;


/**
 * Class for program validations
 */
public class Validate
{
  public static String bgImageRandomizerCaller = "null";
  public static boolean isPingServiceOnLoginRunning = false;
  public static boolean isPingServiceOnMainWindowRunning = false;
  public static boolean isJavaVerified = false;

  private static String currentGame = "";
  private static String GAME_CACHE_PASSWORD = "DEXCRAFTCACHE";
  private static List<String> taskList = new ArrayList<>();

  /**
  * Validates Launcher instance, preventing users from<br>
  * opening Launcher without Init.
  * @param instanceName the name os instance (Init, Client
  * or DCBS).
  */
  public static void instance(String instanceName)
  {
    // Read the session assets from JSON properties file
    SessionDTO.parseSessionAssets();
    //Check if program is running as Admin
    AdminExecution.AdminExecution();
    boolean isInstanceInvalid = true;
    switch (instanceName)
    {
      case "Init":
        if (DexCraftFiles.logLock.exists())
        {
          SessionDTO.setDexCraftLauncherInitInstance(true);
          isInstanceInvalid = true;
        }
        else
        {
          isInstanceInvalid = false;
        }
        break;
      case "Client":
        if (PreventSecondInstance.isThereAnotherInstance("Init"))
        {
          isInstanceInvalid = PreventSecondInstance.isThereAnotherInstance("Client");
        }
        break;
      case "DCBS":
        if ( (PreventSecondInstance.isThereAnotherInstance("Init"))
              && PreventSecondInstance.isThereAnotherInstance("Client") )
        {
          isInstanceInvalid = PreventSecondInstance.isThereAnotherInstance("DCBS");
        }
        break;
      default:
        break;
    }
    if (isInstanceInvalid)
    {
      System.out.println("Foi encontrada uma instância do programa na memória.");
      alerts.doubleInstance();
    }
    else
    {
      System.out.println("Não foi encontrada uma instância do programa na memória.");
    }
  }

  /**
   * Validates a provisionedComponent.<br>
   * 1 - Check if the version is outdated or not installed<br>
   * 2 - Perform the download of the update with progress to the UI<br>
   * 3 - Perform the installation of the update with progress to the UI<br>
   * @param ui the User Interface which will be updated<br>
   * @param componentName the name of the component.
   * @param uiProgressValue the progress percent which represents this task.
   * @return if the component was validated successfully (true) or not (false)
   */
  public static boolean provisionedComponent(DexUI ui, String componentName, int uiProgressValue)
  {
    // Read current version assets
    VersionsDTO.parseVersions();
    VersionsDTO.parseProvisionedVersions();
    String programName = "";
    switch (componentName)
    {
      case "Resources":
        programName = "Recursos";
        break;
      case "Init":
        programName = "DexCraft Launcher Init";
        break;
      case "Client":
        programName = "DexCraft Launcher";
        break;
      case "DCBS":
        programName = "DexCraft Background Services";
        break;
      case "DCGame":
        programName = "DexCraft Factions Client";
        break;
      case "DCPXGame":
        programName = "DexCraft Pixelmon Client";
        break;
      case "DCVNGame":
        programName = "DexCraft Vanilla Client";
        break;
      case "DCBGame":
        programName = "DexCraft Beta Client";
        break;
      case "DCPatchGame":
        programName = "DexCraft Factions Patch";
        break;
      case "DCPXPatchGame":
        programName = "DexCraft Pixelmon Patch";
        break;
      case "DCVNPatchGame":
        programName = "DexCraft Vanilla Patch";
        break;
      case "DCBPatchGame":
        programName = "DexCraft Beta Patch";
        break;
      default:
        break;
    }
    if (!SessionDTO.isOfflineModeOn())
    {
      if ( (ProvisionedPackage.isOutdated(componentName)) || (!ProvisionedPackage.isInstalled(componentName)) )
      {
        downloadComponent(ui, uiProgressValue, componentName, programName);
        installComponent(ui, componentName, programName);
        updateVersionValues(componentName);
      }
    }
    else
    {
      if (!ProvisionedPackage.isInstalled(componentName))
      {
        alerts.noComponents();
      }
    }
    return true;
  }


  /**
   * Downloads the provisioned component.
   * @param ui the User Interface instance
   * @param uiProgressValue the progress which represents this step
   * @param componentName the name of the component to be downloaded
   * @param programName the name of the package (external name used on user interface)
   */
  private static void downloadComponent(DexUI ui, int uiProgressValue, String componentName, String programName)
  {
    if (componentName.contains("Game"))
    {
      ui.resetProgress();
    }
    Client.changeStatus(ui, "Baixando " + programName + "...", "");
    Download downloadComponent = new Download();
    Thread threadDownloadComponent = new Thread(()->
    {
      switch (componentName)
      {
        case "Resources":
          downloadComponent.zipResource(UrlsDTO.getLauncherResourceFile(), DexCraftFiles.tempFolder, DexCraftFiles.resZip);
          break;
        case "Init":
          downloadComponent.zipResource(UrlsDTO.getInitUpdate(), DexCraftFiles.tempFolder, DexCraftFiles.updateInitZip);
          break;
        case "Client":
          downloadComponent.zipResource(UrlsDTO.getDCLUpdate(), DexCraftFiles.tempFolder, DexCraftFiles.updateLauncherZip);
          break;
        case "DCBS":
          downloadComponent.zipResource(UrlsDTO.getDCBSUpdate(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCBSZip);
          break;
        case "DCGame":
          downloadComponent.zipResource(UrlsDTO.getClientDC(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCZip);
          break;
        case "DCPXGame":
          downloadComponent.zipResource(UrlsDTO.getClientDCPX(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCPXZip);
          break;
        case "DCVNGame":
          downloadComponent.zipResource(UrlsDTO.getClientDCVN(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCVNZip);
          break;
        case "DCBGame":
          downloadComponent.zipResource(UrlsDTO.getClientDCB(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCBZip);
          break;
        case "DCPatchGame":
          downloadComponent.zipResource(UrlsDTO.getPatchDC(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCPatchZip);
          break;
        case "DCPXPatchGame":
          downloadComponent.zipResource(UrlsDTO.getPatchDCPX(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCPXPatchZip);
          break;
        case "DCVNPatchGame":
          downloadComponent.zipResource(UrlsDTO.getPatchDCVN(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCVNPatchZip);
          break;
        case "DCBPatchGame":
          downloadComponent.zipResource(UrlsDTO.getPatchDCB(), DexCraftFiles.tempFolder, DexCraftFiles.updateDCBPatchZip);
          break;
        default:
          break;
      }
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
      if (componentName.contains("Game"))
      {
        Client.changeStatus(ui, "Baixando " + programName + "... "
                , downloadComponent.getTimeEstimatedMsg() );
        ui.changeProgress(true, Double.parseDouble(downloadComponent.getProgressPercent().replace(",",".")), 35);
      }
      else
      {
        Client.changeStatus(ui, "Baixando " + programName + "..."
                + downloadComponent.getProgressPercent() + "% concluído", "");
      }
    }
    if (componentName.contains("Game"))
    {
      Client.changeStatus(ui, "Baixando " + programName + "...", "100% concluído");
      ui.changeProgress(true, 100, 35);
      ui.resetProgress();
    }
    else
    {
      Client.changeStatus(ui, "Baixando " + programName + "..." + "100% concluído", "");
      ui.changeProgress(true, uiProgressValue, 30);
    }
  }


  /**
   * Installs the provisioned component.
   * @param ui the User Interface instance
   * @param uiProgressValue the progress which represents this step
   * @param componentName the name of the component to be installed
   * @param programName the name of the package (external name used on user interface)
   */
  private static void installComponent(DexUI ui, String componentName, String programName)
  {
    Client.changeStatus(ui, "Instalando " + programName + "... ", "");
    Install installComponent = new Install();
    Thread threadInstallComponent = new Thread(()->
    {
      switch (componentName)
      {
        case "Resources":
          installComponent.downloadedZipResource(DexCraftFiles.resZip, DexCraftFiles.resFolder);
          break;
        case "Init":
          installComponent.downloadedZipResource(DexCraftFiles.updateInitZip, DexCraftFiles.launcherFolder);
          break;
        case "Client":
          installComponent.downloadedZipResource(DexCraftFiles.updateLauncherZip, DexCraftFiles.launcherFolder);
          break;
        case "DCBS":
          installComponent.downloadedZipResource(DexCraftFiles.updateDCBSZip, DexCraftFiles.launcherFolder);
          break;
        case "DCGame":
          installComponent.downloadedZipResource(DexCraftFiles.updateDCZip, DexCraftFiles.launcherFolder);
          break;
        case "DCPXGame":
          installComponent.downloadedZipResource(DexCraftFiles.updateDCPXZip, DexCraftFiles.launcherFolder);
          break;
        case "DCVNGame":
          installComponent.downloadedZipResource(DexCraftFiles.updateDCVNZip, DexCraftFiles.launcherFolder);
          break;
        case "DCBGame":
          installComponent.downloadedZipResource(DexCraftFiles.updateDCBZip, DexCraftFiles.launcherFolder);
          break;
        case "DCPatchGame":
          installComponent.downloadedZipPatch(DexCraftFiles.updateDCPatchZip, DexCraftFiles.launcherFolder);
          break;
        case "DCPXPatchGame":
          installComponent.downloadedZipPatch(DexCraftFiles.updateDCPXPatchZip, DexCraftFiles.launcherFolder);
          break;
        case "DCVNPatchGame":
          installComponent.downloadedZipPatch(DexCraftFiles.updateDCVNPatchZip, DexCraftFiles.launcherFolder);
          break;
        case "DCBPatchGame":
          installComponent.downloadedZipPatch(DexCraftFiles.updateDCBPatchZip, DexCraftFiles.launcherFolder);
          break;
        default:
          break;
      }
    });
    threadInstallComponent.start();
    String checkFile = " ";
    while(threadInstallComponent.isAlive())
    {
      if (!(installComponent.getInstallingFileName()).equals(""))
      {
        if (!installComponent.getInstallingFileName().equals(checkFile))
        {
          if (componentName.contains("Game"))
          {
            Client.changeStatus(ui, "Instalando " + programName + "... "
                    , installComponent.getInstallingFileName() + ", "
                    + installComponent.getInstallingFilePosition() + " / "
                    + installComponent.getTotalFilesQuantity());
            ui.changeProgress(true, Double.parseDouble(installComponent.getProgressPercent().replace(",",".")), 35);
          }
          else
          {
            Client.changeStatus(ui, "Instalando " + programName + "... "
                    + installComponent.getInstallingFilePosition() + " / "
                    + installComponent.getTotalFilesQuantity(), "");
          }
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
    if (componentName.contains("Game"))
    {
      Client.changeStatus(ui, "Instalando " + programName + "... "
                    , installComponent.getTotalFilesQuantity() + " / "
                    + installComponent.getTotalFilesQuantity());
      ui.changeProgress(true, 100, 35);
      ui.resetProgress();
    }
    else
    {
      Client.changeStatus(ui, "Instalando " + programName + "... "
              + installComponent.getTotalFilesQuantity() + " / "
              + installComponent.getTotalFilesQuantity() , "");
    }
  }


  /**
   * Change version assets to finalize component update
   * @param componentName the component name that was updated
   */
  private static void updateVersionValues(String componentName)
  {
    JsonDAO json = new JsonDAO();
    File timestampFile = new File(DexCraftFiles.gameCache + "/" + SessionDTO.getSessionUser() + "/syncproperties.json");
    switch (componentName)
    {
      case "Resources":
        break;
      case "Init":
        VersionsDTO.setDexCraftLauncherInitVersion(VersionsDTO.getProvisionedInitVersion());
        break;
      case "Client":
        VersionsDTO.setDexCraftLauncherClientVersion(VersionsDTO.getProvisionedClientVersion());
        break;
      case "DCBS":
        VersionsDTO.setDexCraftBackgroundServicesVersion(VersionsDTO.getProvisionedBackgroundServicesVersion());
        break;
      case "DCGame":
        // Game Clients don't need versioning
        // But needs to change the backup value so it can be downloaded again.
        if(timestampFile.exists())
        {
          json.editValue(timestampFile, "DC", "BackupTimestamp", "0");
        }
        VersionsDTO.setDexCraftFactionsPatchVersion("v0");
        break;
      case "DCPXGame":
        // Game Clients don't need versioning
        // But needs to change the backup value so it can be downloaded again.
        if(timestampFile.exists())
        {
          json.editValue(timestampFile, "DCPX", "BackupTimestamp", "0");
        }
        VersionsDTO.setDexCraftPixelmonPatchVersion("v0");
        break;
      case "DCVNGame":
        // Game Clients don't need versioning
        // But needs to change the backup value so it can be downloaded again.
        if(timestampFile.exists())
        {
          json.editValue(timestampFile, "DCVN", "BackupTimestamp", "0");
        }
        VersionsDTO.setDexCraftVanillaPatchVersion("v0");
        break;
      case "DCBGame":
        // Game Clients don't need versioning
        // But needs to change the backup value so it can be downloaded again.
        if(timestampFile.exists())
        {
          json.editValue(timestampFile, "DCB", "BackupTimestamp", "0");
        }
        VersionsDTO.setDexCraftBetaPatchVersion("v0");
        break;
      case "DCPatchGame":
        VersionsDTO.setDexCraftFactionsPatchVersion(VersionsDTO.getProvisionedFactionsPatchVersion());
        break;
      case "DCPXPatchGame":
        VersionsDTO.setDexCraftPixelmonPatchVersion(VersionsDTO.getProvisionedPixelmonPatchVersion());
        break;
      case "DCVNPatchGame":
        VersionsDTO.setDexCraftVanillaPatchVersion(VersionsDTO.getProvisionedVanillaPatchVersion());
        break;
      case "DCBPatchGame":
        VersionsDTO.setDexCraftBetaPatchVersion(VersionsDTO.getProvisionedBetaPatchVersion());
        break;
      default:
        break;
    }
  }


  /**
   * Validates and prepares the game session.
   * @param ui the User Interface instance
   * @param currentUser the user currently logged in
   */
  public static void gameCache(DexUI ui, String currentUser)
  {
    switch (SessionDTO.getLastServer())
    {
      case "0":
        currentGame = "dc";
        break;
      case "1":
        currentGame = "dcpx";
        break;
      case "2":
        currentGame = "dcvn";
        break;
      case "3":
        currentGame = "dcb";
        break;
      default:
        break;
    }
    String oldUser = SessionDTO.getSessionUser();
    Client.changeStatus(ui, "Preparando perfil. Aguarde...", "");
    if ( (!oldUser.equals(currentUser)) && (!oldUser.equals("")) )
    {
      logger.log("INFO", "A conta logada atualmente "
             + "é diferente daquela da última sessão. Salvando "
             + "os dados atuais em Cache...");
      FileIO fio = new FileIO();
      File gameCache = new File(DexCraftFiles.gameCache.toString() + "/" + oldUser + "/" + currentGame);
      if (gameCache.exists())
      {
        fio.excluir(gameCache, false);
      }
      gameCache.mkdirs();
      File syncProps = new File(DexCraftFiles.gameCache.toString() + "/" + oldUser + "/syncproperties.json");
      if(!syncProps.exists())
      {
        fio.copiar(DexCraftFiles.syncPropsRoot, syncProps);
      }
      Thread backupTask = new Thread(()->
      {
        backupCache(ui, oldUser, currentGame);
      });
      backupTask.start();
      while (backupTask.isAlive())
      {
        Client.changeStatus(ui, "Preparando perfil. Aguarde...", "");
        try
        {
          Thread.sleep(500);
        }
        catch (InterruptedException ex)
        {
          logger.log(ex,"EXCEÇÃO em Validate.gameCache(DexUI, String)");
        }
      }
      gameCache = new File(DexCraftFiles.gameCache.toString() + "/" + currentUser + "/" + currentGame + ".7z");
      if (gameCache.exists())
      {
        Thread restoreTask = new Thread(()->
        {
          restoreCache(currentUser, currentGame);
        });
        restoreTask.start();
        String statusMsg = "";
        while (restoreTask.isAlive())
        {
          if(!ZipUtils.statusMessage.equals(statusMsg))
          {
            Client.changeStatus(ui, ZipUtils.statusMessage, "");
          }
          try
          {
            Thread.sleep(100);
          }
          catch (InterruptedException ex)
          {
            logger.log(ex,"EXCEÇÃO em Validate.gameCache(DexUI, String)");
          }
          statusMsg = ZipUtils.statusMessage;
        }
      }
    }
    else
    {
      File gameCache = new File(DexCraftFiles.gameCache.toString() + "/" + currentUser + "/" + currentGame);
      if (!gameCache.exists())
      {
        logger.log("INFO","Diretório de cache de jogo não encontrado. Gerando um novo...");
        gameCache.mkdirs();
      }
      File syncProps = new File(DexCraftFiles.gameCache.toString() + "/" + currentUser + "/syncproperties.json");
      if(!syncProps.exists())
      {
        FileIO fio = new FileIO();
        logger.log("INFO","SyncProps não encontrado. Gerando um novo...");
        fio.copiar(DexCraftFiles.syncPropsRoot, syncProps);
      }
    }
    logger.log("INFO","Cache validado.");
  }


  /**
   * Performs the backup preparation to change the logged account.
   * @param ui the User Interface instance
   * @param user the username needed to backup
   * @param game the client game that need to be backuped
   */
  public static void backupCache(DexUI ui, String user, String game)
  {
    taskList = SystemDTO.getDCLBkpDirectivesFull();
    FileIO fio = new FileIO();
    taskList.forEach((item)->
    {
      if (item.lastIndexOf(game) != -1)
      {
        String fileSrc = item.replace("\"", "");
        Client.changeStatus(ui, "Preparando perfil... " + (taskList.indexOf(item)+1) + " / " + taskList.size(), "");
        File destination = new File (DexCraftFiles.gameCache.toString() + "/" + user + "/" + fileSrc);
        File src = new File(DexCraftFiles.launcherFolder.toString() + "/" + fileSrc);
        if (src.exists())
        {
          fio.copiar(src, destination);
          fio.excluir(src, false);
        }
      }
    });
    Client.changeStatus(ui, "Preparando perfil... " + taskList.size() + " / " + taskList.size(), "");
    ZipUtils.compressWithPassword(new File (DexCraftFiles.gameCache.toString() + "/" + user + "/" + game), GAME_CACHE_PASSWORD);
    fio.excluir(new File (DexCraftFiles.gameCache.toString() + "/" + user + "/" + game), true);
  }


  /**
   * Performs the account restoration from changed logged account.
   * @param user the username that needs to have their data restored
   * @param game the game client data
   */
  public static void restoreCache(String user, String game)
  {
    File zipDir = new File (DexCraftFiles.gameCache.toString() + "/" + user + "/" + game);
    File zipFile = new File(zipDir.toString() + ".7z");
    if (zipFile.exists())
    {
      ZipUtils.extractWithPassword(game, zipDir, GAME_CACHE_PASSWORD);
      FileIO fio = new FileIO();
      fio.copiar(zipDir, new File(DexCraftFiles.launcherFolder + "/" + game));
      fio.excluir(zipDir, true);
      fio.excluir(zipFile, false);
    }
    else
    {
      logger.log("***ERRO***", "EXCEÇÃO em Validate.restoreCache() - ARQUIVO DE BACKUP NÃO ENCONTRADO." );
      alerts.tryAgain();
    }
  }


  /**
   * Validates JRE Version.
   * @param ui DexUI instance
   * @return true if the method was performed with no errors.
   */
  public static boolean javaVersion(DexUI ui)
  {
    ui.resetProgress();
    if (!isJavaVerified)
    {
      Client.changeStatus(ui, "Verificando Java...", "");
      SystemRequirements req = new SystemRequirements();
      if (!req.checkJavaVersion().equals(SystemDTO.getJavaVersion()))
      {
        logger.log("INFO", "Java desatualizado! (" + req.checkJavaVersion() + ")");
        if (SessionDTO.isOfflineModeOn())
        {
          alerts.noComponents();
        }
        else
        {
          Download downloadJRE = new Download();
          Thread threadDownloadJRE = new Thread(()->
          {
            downloadJRE.zipResource(UrlsDTO.getJREInstaller(), DexCraftFiles.tempFolder, DexCraftFiles.updateJREZip);
          });
          threadDownloadJRE.start();
          while(threadDownloadJRE.isAlive())
          {
            try
            {
              Thread.sleep(1100);
            }
            catch (InterruptedException ex)
            {
              logger.log(ex, "EXCEÇÃO em Validate.launcher()");
            }
            logger.log("INFO", downloadJRE.getTimeEstimatedMsg());
            Client.changeStatus(ui, "Baixando atualização do Java... "
                      , downloadJRE.getTimeEstimatedMsg() );
            ui.changeProgress(true, Double.parseDouble(downloadJRE.getProgressPercent().replace(",",".")), 35);
          }
          Client.changeStatus(ui, "Baixando atualização do Java... ", "100% concluído");
          ui.changeProgress(true, 100, 35);
          ui.resetProgress();
          Client.changeStatus(ui, "Instalando Java...", "");
          Install installJRE = new Install();
          Thread threadInstallJRE = new Thread(()->
          {
            installJRE.downloadedZipResource(DexCraftFiles.updateJREZip, DexCraftFiles.tempFolder);
          });
          threadInstallJRE.start();
          String checkFile = " ";
          while(threadInstallJRE.isAlive())
          {
            if (!(installJRE.getInstallingFileName()).equals(""))
            {
              if (!installJRE.getInstallingFileName().equals(checkFile))
              {
                Client.changeStatus(ui, "Instalando Java... "
                          , installJRE.getInstallingFileName() + ", "
                          + installJRE.getInstallingFilePosition() + " / "
                          + installJRE.getTotalFilesQuantity());
                ui.changeProgress(true, Double.parseDouble(downloadJRE.getProgressPercent().replace(",",".")), 35);
                checkFile = installJRE.getInstallingFileName();
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
          Thread jreInstallerThread = new Thread(()->
          {
            try
            {
              new ProcessBuilder("cmd", "/c", "start", "/wait", "jre.exe", "INSTALLCFG=C:\\DexCraft\\launcher\\temp\\java.settings.cfg")
                    .directory(DexCraftFiles.tempFolder).start().waitFor();
            }
            catch (IOException | InterruptedException ex)
            {
              alerts.exceptionHandler(ex, "EXCEÇÃO em Validate.javaVersion(DexUI).jreInstallerThread()");
            }
          });
          jreInstallerThread.start();
          while(threadInstallJRE.isAlive())
          {
            Client.changeStatus(ui, "Executando atualização. Aguarde... "
                          , installJRE.getTotalFilesQuantity() + " / "
                          + installJRE.getTotalFilesQuantity());
            ui.changeProgress(true, -1, 35);
            try
            {
              Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
              logger.log(ex,"EXCEÇÃO em Validate.provisionedComponent()");
            }
          }
          ui.resetProgress();
        }
      }
      logger.log("INFO", "Java Atualizado! (" + req.checkJavaVersion() + ")");
      isJavaVerified = true;
    }
    return true;
  }


}
