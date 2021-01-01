package net.dex.dexcraft.launcher.client.services;


import java.io.File;
import java.util.List;
import javafx.concurrent.Task;
import static net.dex.dexcraft.commons.Commons.alerts;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.dao.JsonDAO;
import net.dex.dexcraft.commons.dto.FtpDTO;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.dto.SystemDTO;
import net.dex.dexcraft.commons.tools.Crypto;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.commons.tools.FileIO;
import net.dex.dexcraft.commons.tools.FtpUtils;
import net.dex.dexcraft.commons.tools.ZipUtils;
import net.dex.dexcraft.launcher.client.MainWindowController;


/**
 * Account verification and syncronization Service.
 */
public class AccountSyncService extends Task<Void>
{
  // Variable retrieving from Controller Caller
  public String serviceName = MainWindowController.serviceName;

  public File tempSyncProps = new File (DexCraftFiles.tempFTPFolder + "/syncproperties.json");
  public boolean tempSyncPropsIsDownloaded = false;
  public File localSyncProps = new File(DexCraftFiles.gameCache + "/" + SessionDTO.getSessionUser() + "/syncproperties.json");

  public FtpUtils ftp = new FtpUtils();
  public DexUI ui;
  public String component;
  public boolean isBackupValidated = false;

  /**
   * SET the User Interface instance for using on service.
   * @param userInterface the User Interface.
   */
  public void setUI(DexUI userInterface)
  {
    this.ui = userInterface;
  }

  /**
   * GET the User Interface instance used on service.
   * @return the User Interface.
   */
  public DexUI getUI()
  {
    return this.ui;
  }


  /**
   * Main method.
   * @return ignored
   */
  @Override
  protected Void call()
  {
    logger.log("INFO", "SERVIÇO: Iniciando o Serviço " + "AccountSyncService" + "...");
    MainWindowController.isAccountSyncDone = false;
    if (serviceName.equals("PlayGame"))
    {
      serviceName = "Verify";
    }
    if (!SessionDTO.isOfflineModeOn())
    {
      switch (SessionDTO.getLastServer())
      {
        case "0":
          component = "dc";
          break;
        case "1":
          component = "dcpx";
          break;
        case "2":
          component = "dcvn";
          break;
        case "3":
          component = "dcb";
          break;
        default:
          break;
      }
      ftp.setAddress(FtpDTO.getFtpAddress());
      ftp.setPort(FtpDTO.getFtpPort());
      ftp.setUser(FtpDTO.getFtpUser());
      ftp.setPassword(FtpDTO.getFtpPassword());
      ftp.setWorkingDir(FtpDTO.getFtpWorkingDir());
      ftp.connect();
      ui.setMainButtonDisable(true);
      ui.getProgressBar().setVisible(true);
      Thread thread = new Thread(new AccountSync());
      thread.setDaemon(true);
      thread.start();
      while (!serviceName.equals("null"))
      {
        try
        {
          Thread.sleep(1000);
        }
        catch (InterruptedException ex)
        {
          // Thread interruption ignored
  //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
        }
      }
      ftp.disconnect();
      ui.changeMainLabel("");
      ui.changeSecondaryLabel("");
      ui.changeProgress(true, 100, 30);
      ui.getProgressBar().setVisible(false);
      ui.setMainButtonDisable(false);
    }
    MainWindowController.isAccountSyncDone = true;
    logger.log("INFO", "SERVIÇO: Finalizando o Serviço " + "AccountSyncService" + "...");
    return null;
  }


  /**
   * Main Account Synchronizing Thread.
   * Performs the following tasks:<br>
   * "Verify" - Checks if the local data is
   * older than the remote data.<br>
   * If true, the client is updated. If false,
   * the remote data is updated.<br>
   * "Backup" - backup operation.
   * "Restore" - restore operation.
   */
  public class AccountSync extends Thread
  {

    /**
     * Main thread method.
     */
    @Override
    public void run()
    {
      if (serviceName.equals("Verify"))
      {
        logger.log("INFO", "SERVIÇO: Verificando...");
        ftp.checkFolder(FtpDTO.getFtpWorkingDir() + "/" + SessionDTO.getSessionUser());
        checkRemoteSyncProps();
        JsonDAO json = new JsonDAO();
        String localTimestamp = json.readValue(localSyncProps, component.toUpperCase(), "BackupTimestamp");
        String remoteTimestamp = json.readValue(tempSyncProps, component.toUpperCase(), "BackupTimestamp");
        if (Long.parseLong(localTimestamp) == Long.parseLong(remoteTimestamp))
        {
          logger.log("INFO", "SERVIÇO: Os dados remotos e os locais possuem o mesmo timestamp. Nada será alterado.");
        }
        else if (Long.parseLong(localTimestamp) > Long.parseLong(remoteTimestamp))
        {
          logger.log("INFO", "SERVIÇO: Os dados do cliente estão mais atualizados que o remoto. Preparando backup para upload...");
          serviceName = "Backup";
        }
        else
        {
          logger.log("INFO", "SERVIÇO: Os dados do remoto estão mais atualizados que o do cliente. Preparando download do backup...");
          serviceName = "Restore";
        }
      }
      if (serviceName.equals("Backup"))
      {
        // upload
        checkRemoteSyncProps();
        Thread prepare = new Thread(()->
        {
          prepareFiles(true);
        });
        prepare.start();
        ui.changeProgress(true, -1, 30);
        while (prepare.isAlive())
        {
          ui.changeMainLabel("Compilando arquivos, aguarde...");
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // Thread interruption ignored
    //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
          }
        }
        ui.changeMainLabel("Compilando arquivos... Concluído!");
        ui.resetProgress();
        File backupZip = new File (DexCraftFiles.tempFTPFolder + "/" + component + ".7z");
        ftp.checkFolder(FtpDTO.getFtpWorkingDir() + "/" + SessionDTO.getSessionUser());
        Thread uploadBackupZip = new Thread(()->
        {
          ftp.uploadFileWithProgress(SessionDTO.getSessionUser(), backupZip.toString());
        });
        uploadBackupZip.start();
        while (uploadBackupZip.isAlive())
        {
          ui.changeMainLabel("Enviando backup... " + ftp.getProgressPercent() + " % concluído");
          ui.changeSecondaryLabel(ftp.getTimeEstimatedMsg());
          ui.changeProgress(true, Double.parseDouble(ftp.getProgressPercent().replace(",",".")), 30);
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // Thread interruption ignored
    //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
          }
        }
        ui.changeMainLabel("Enviando backup... 100 % concluído");
        ui.changeSecondaryLabel("");
        ui.changeProgress(true, 100, 30);
        FileIO fio = new FileIO();
        fio.excluir(backupZip, false);
        JsonDAO json = new JsonDAO();
        long timestamp = System.currentTimeMillis();
        json.editValue(tempSyncProps, component.toUpperCase(), "BackupTimestamp", Long.toString(timestamp));
        json.editValue(localSyncProps, component.toUpperCase(), "BackupTimestamp", Long.toString(timestamp));
        Thread uploadSyncProps = new Thread(()->
        {
          ftp.uploadFileWithProgress(SessionDTO.getSessionUser(), tempSyncProps.toString());
        });
        uploadSyncProps.start();
        ui.changeProgress(true, -1, 30);
        while (uploadSyncProps.isAlive())
        {
          ui.changeMainLabel("Enviando assets...");
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // Thread interruption ignored
      //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
          }
        }
        ui.changeMainLabel("Enviando assets...concluído");
        ui.resetProgress();
      }
      if (serviceName.equals("Restore"))
      {
        // download
        checkRemoteSyncProps();
        ftp.checkFolder(FtpDTO.getFtpWorkingDir() + "/" + SessionDTO.getSessionUser());
        File backupZip = new File (DexCraftFiles.tempFTPFolder.toString() + "/" + component + ".7z");
        File check = new File(FtpDTO.getFtpWorkingDir() + "/" + SessionDTO.getSessionUser() + "/" + component + ".7z");
        if (ftp.fileExists(check))
        {
          Thread downloadBackup = new Thread(()->
          {
            ftp.downloadFileWithProgress(SessionDTO.getSessionUser(), backupZip);
          });
          downloadBackup.start();
          while (downloadBackup.isAlive())
          {
            ui.changeMainLabel("Baixando backup... " + ftp.getProgressPercent() + " % concluído");
            ui.changeSecondaryLabel(ftp.getTimeEstimatedMsg());
            ui.changeProgress(true, Double.parseDouble(ftp.getProgressPercent().replace(",",".")), 30);
            try
            {
              Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
              // Thread interruption ignored
      //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
            }
          }
          ui.changeMainLabel("Baixando backup... 100 % concluído");
          ui.changeSecondaryLabel("");
          ui.changeProgress(true, 100, 30);
          File destination = new File(DexCraftFiles.tempFTPFolder + "/" + component);
          Thread extractBackup = new Thread(()->
          {
            ZipUtils.extractWithPassword(component, destination, Crypto.decrypt(SessionDTO.getSessionPassword()));
          });
          extractBackup.start();
          ui.changeProgress(true, -1, 30);
          while (extractBackup.isAlive())
          {
            ui.changeMainLabel("Extraindo backup...");
            ui.changeSecondaryLabel(ZipUtils.statusMessage);
            try
            {
              Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
              // Thread interruption ignored
      //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
            }
          }
          ui.changeMainLabel("Baixando backup...concluído");
          ui.changeSecondaryLabel("");
          ui.resetProgress();
          Thread copyBackup = new Thread(()->
          {
            prepareFiles(false);
          });
          copyBackup.start();
          ui.changeProgress(true, -1, 30);
          while (copyBackup.isAlive())
          {
            ui.changeMainLabel("Compilando arquivos, aguarde...");
            try
            {
              Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
              // Thread interruption ignored
      //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
            }
          }
          JsonDAO json = new JsonDAO();
          String timestamp = json.readValue(tempSyncProps, component.toUpperCase(), "BackupTimestamp");
          json.editValue(localSyncProps, component.toUpperCase(), "BackupTimestamp", timestamp);
          ui.changeMainLabel("Compilando arquivos... Concluído!");
          ui.resetProgress();
        }
        else
        {
          logger.log("***ERRO***", "ARQUIVO DE BACKUP NÃO ENCONTRADO NO SERVIDOR REMOTO");
          alerts.tryAgain();
        }
      }
      serviceName = "null";
    }
  }

  /**
   * Check for the remote sync properties file.<br>
   * If the user folder on FTP server does not
   * contains the syncproperties.json,<br>
   * it will be created and uploaded.
   */
  public void checkRemoteSyncProps()
  {
    if (!tempSyncPropsIsDownloaded)
    {
      ui.changeMainLabel("Verificando...");
      File check = new File(FtpDTO.getFtpWorkingDir() + "/" + SessionDTO.getSessionUser() + "/syncproperties.json");
      if (ftp.fileExists(check))
      {
        Thread downloadSyncProps = new Thread(()->
        {
          ftp.downloadFileWithProgress(SessionDTO.getSessionUser(), tempSyncProps);
        });
        downloadSyncProps.start();
        ui.changeProgress(true, -1, 30);
        while (downloadSyncProps.isAlive())
        {
          ui.changeMainLabel("Baixando assets...");
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // Thread interruption ignored
    //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
          }
        }
        ui.changeMainLabel("Baixando assets...concluído");
        ui.resetProgress();
      }
      else
      {
        cloneSyncProps();
      }
      tempSyncPropsIsDownloaded = true;
    }

  }

  /**
   * Creates and uploads the syncproperties.json file.
   */
  public void cloneSyncProps()
  {
    logger.log("INFO", "SERVIÇO: Não foi encontrado syncproperties.json no servidor remoto. "
                  + "O backup remoto será refeito.");
    Thread uploadSyncProps = new Thread(()->
    {
      ftp.uploadFileWithProgress(SessionDTO.getSessionUser(), DexCraftFiles.syncPropsRoot.toString());
    });
    uploadSyncProps.start();
    ui.changeProgress(true, -1, 30);
    while (uploadSyncProps.isAlive())
    {
      ui.changeMainLabel("Enviando assets...");
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException ex)
      {
        // Thread interruption ignored
  //        alerts.exceptionHandler(ex, "EXCEÇÃO em AccountSyncService.call()");
      }
    }
    ui.changeMainLabel("Enviando assets... concluído");
    ui.resetProgress();
    logger.log("INFO", "SERVIÇO: Excluindo backup anterior...");
    File check = new File(FtpDTO.getFtpWorkingDir() + "/" + SessionDTO.getSessionUser() + "/" + component +  ".7z");
    if (ftp.fileExists(check))
    {
      ftp.deleteFile(check.toString());
    }
    FileIO fio = new FileIO();
    fio.copiar(DexCraftFiles.syncPropsRoot, tempSyncProps);
  }

  /**
   * Prepare files to perform backup or restore.
   * @param toBackup true if the files will be prepared
   * to compile the backup zip file to upload, false
   * otherwise.
   */
  public void prepareFiles(boolean toBackup)
  {
    if (toBackup)
    {
      logger.log("INFO", "SERVIÇO: Realizando transferência de arquivos e preparando backup...");
    }
    else
    {
      logger.log("INFO", "SERVIÇO: Realizando transferência de arquivos...");
    }
    List<String> taskList;
    if (SessionDTO.isBackupSingleplayerMapsEnabled())
    {
      taskList = SystemDTO.getDCLBkpDirectivesFull();
    }
    else
    {
      taskList = SystemDTO.getDCLBkpDirectivesPartial();
    }
    FileIO fio = new FileIO();
    taskList.forEach((item)->
    {
      if (item.lastIndexOf(component) != -1)
      {
        String fileSrc = item.replace("\"", "");
        ui.changeMainLabel("Preparando... " + (taskList.indexOf(item)+1) + " / " + taskList.size());
        File destination = new File (DexCraftFiles.tempFTPFolder.toString() + "/" + fileSrc);
        File src = new File(DexCraftFiles.launcherFolder.toString() + "/" + fileSrc);
        if (!toBackup)
        {
          destination = new File(DexCraftFiles.launcherFolder.toString() + "/" + fileSrc);
          src = new File (DexCraftFiles.tempFTPFolder.toString() + "/" + fileSrc);
        }
        if (src.exists())
        {
          fio.copiar(src, destination);
        }
      }
    });
    ui.changeMainLabel("Preparando... " + taskList.size() + " / " + taskList.size());
    if (toBackup)
    {
      logger.log("INFO", "SERVIÇO: Comprimindo arquivos...");
      ZipUtils.compressWithPassword(new File (DexCraftFiles.tempFTPFolder.toString() + "/" + component), Crypto.decrypt(SessionDTO.getSessionPassword()));
    }
    else
    {
      fio.excluir(new File (DexCraftFiles.tempFTPFolder.toString() + "/" + component + ".7z"), false);
    }
    fio.excluir(new File (DexCraftFiles.tempFTPFolder.toString() + "/" + component), true);
    logger.log("INFO", "SERVIÇO: Preparação concluída.");
  }
}
