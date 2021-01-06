package net.dex.dexcraft.launcher.client.services;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javafx.concurrent.Task;
import static net.dex.dexcraft.commons.Commons.alerts;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.check.SystemRequirements;
import net.dex.dexcraft.commons.dao.JsonDAO;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.dto.SystemDTO;
import net.dex.dexcraft.commons.tools.Close;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.commons.tools.FileIO;
import net.dex.dexcraft.launcher.client.MainWindowController;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;


/**
 * This Service prepares the launcher to run the
 * Background Services program and prepare the internal
 * launcher assets.<br>
 * Also it can define specific profile configuration.
 */
public class PrepareLauncherService extends Task<Void>
{
  // Variable retrieving from Controller Caller
  public String serviceName = MainWindowController.serviceName;

  public DexUI ui;
  public String component;

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
    logger.log("INFO", "PREPARELAUNCHER: Inicializando Serviço...");
    while (!MainWindowController.isAccountSyncDone)
    {
      try
      {
        Thread.sleep(400);
      }
      catch (InterruptedException ex)
      {
        //
      }
    }
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
    ui.setMainButtonDisable(true);
    ui.changeMainLabel("");
    ui.changeSecondaryLabel("");
    ui.getMenuBar().setDisable(true);
    ui.getProgressBar().setVisible(true);
    ui.resetProgress();
    ui.changeProgress(false, -1, 20);
    mainRoutine();
    return null;
  }

  /**
   * Service's main method.
   */
  public void mainRoutine()
  {
    switch (serviceName)
    {
      case "PlayGame":
        // Runs Thread to prepare to launch the game
        Thread thread = new Thread(new PrepareGameAndRun());
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
    //        alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().mainRoutine()");
          }
        }
        break;
      case "":

        break;
      default:
        alerts.tryAgain();
        break;
    }
  }

  /**
   * Apply specific setting profile to internal launcher's assets.
   * @param profile the profile to apply. The profile is defined
   * by the main directory's name, which contains files to be applied.
   */
  public void applySettingsProfile(String profile)
  {
    logger.log("INFO", "PREPARELAUNCHER: Aplicando profile " + profile);
    File src = new File(DexCraftFiles.srcFolder + "/" + component + "/" + component + "-" + profile);
    if (!src.exists())
    {
      logger.log("***ERRO***", "PREPARELAUNCHER: Diretório de profiles não encontrado. Requer novo download do patch.");
      JsonDAO json = new JsonDAO();
      switch (SessionDTO.getLastServer())
      {
        case "0":
          json.editValue(DexCraftFiles.launcherProperties, "Versions", "DexCraftFactionsPatchVersion", "v0");
          break;
        case "1":
          json.editValue(DexCraftFiles.launcherProperties, "Versions", "DexCraftPixelmonPatchVersion", "v0");
          break;
        case "2":
          json.editValue(DexCraftFiles.launcherProperties, "Versions", "DexCraftVanillaPatchVersion", "v0");
          break;
        case "3":
          json.editValue(DexCraftFiles.launcherProperties, "Versions", "DexCraftBetaPatchVersion", "v0");
          break;
        default:
          break;
      }
      alerts.tryAgain();
    }
    Thread apply = new Thread(()->
    {
      try
      {
        FileUtils.copyDirectory(src, new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft"));
      }
      catch (IOException ex)
      {
        System.out.println("ERRO");
      }
    });
    apply.start();
    while (apply.isAlive())
    {
      ui.changeProgress(false, -1, 20);
      ui.changeMainLabel("Aguarde...");
      ui.changeSecondaryLabel("Aguarde...");
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException ex)
      {
        //
      }
    }
  }


  /**
   * Assures the JRE mentioned in the internal launcher's asset file<br>
   * is the correct one.
   * @param source the json file to modify
   * @param categoryName the category containing the entry to be changed
   */
  public void editJavaVersionOnJSON(File source, String categoryName)
  {
    logger.log("INFO", "PREPARELAUNCHER: Editando especificações Java no asset do Shiginima...");
    FileWriter file = null;
    try
    {
      String getSource = source.toString();
      File destination = new File (getSource);
      File renameSource = new File(source + ".delete");
      source.renameTo(renameSource);
      String jsonString = FileUtils.readFileToString(renameSource, "UTF-8");
      JSONObject obj = new JSONObject (jsonString);
      JSONObject parentObj = obj.getJSONObject("profiles");
      JSONObject childObj = parentObj.getJSONObject(categoryName);
      childObj.put("javaDir", "C:\\Program Files\\Java\\" + SystemDTO.getJavaVersion() + "\\bin\\javaw.exe");
      FileIO fio = new FileIO();
      fio.excluir(renameSource, false);
      file = new FileWriter(destination);
      file.write(obj.toString(4));
    }
    catch (IOException e)
    {
      alerts.exceptionHandler(e, "EXCEÇÃO EM editJSON() - FALHA AO CRIAR ARQUIVO JSON");
    }
    finally
    {
      try
      {
        file.flush();
        file.close();
      } catch (IOException e){}
    }
    logger.log("INFO", "PREPARELAUNCHER: Alterações feitas.");
  }


  /**
   * Edit internal launcher's asset file.
   * @param shiginimaFile the asset file to modify
   */
  public void editShiginimaAsset(File shiginimaFile)
  {
    logger.log("INFO", "PREPARELAUNCHER: Alterando assets do Shig.inima...");
    editSpecificLine(shiginimaFile, "username.lastused:", "username.lastused: " + SessionDTO.getSessionUser());
    editSpecificLine(shiginimaFile, "theme.dark:", "theme.dark: true");
    editSpecificLine(shiginimaFile, "auto.name: ", "auto.name: " + SessionDTO.getSessionUser());
    editSpecificLine(shiginimaFile, "language: ", "language: pt");
    editSpecificLine(shiginimaFile, "auto.language: ", "auto.language: false");
    editSpecificLine(shiginimaFile, "auto.enabled: ", "auto.enabled: true");
  }


  /**
   * Edit specific line in a text file.
   * @param file the text file
   * @param searchText entry to search
   * @param replaceText entry to replace
   */
  public void editSpecificLine(File file, String searchText, String replaceText)
  {
    try
    {
      java.util.List<String> readList = FileUtils.readLines(file, "UTF-8");
      int i = 0;
      while (i < readList.size())
      {
        if (readList.get(i).contains(searchText))
        {
          readList.remove(i);
          readList.add(i, replaceText);
        }
        i++;
      }
      FileUtils.deleteQuietly(file);
      FileUtils.touch(file);
      i = 0;
      try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
      {
        while (i < readList.size())
        {
          bw.write(readList.get(i));
          if (i+1 != readList.size())
          {
            bw.newLine();
          }
          i++;
        }
      }
    }
    catch (IOException ex)
    {
      alerts.exceptionHandler(ex, "EXCEÇÃO EM editSpecificLine(File file, String searchText, String replaceText) - FALHA AO MANIPULAR O ARQUIVO " + file);
    }
  }


  /**
   * Prepare the internal launcher assets and run the
   * Background Services in order to initialize the game.
   */
  public class PrepareGameAndRun extends Thread
  {

    /**
     * Main method.
     */
    @Override
    public void run()
    {
      logger.log("INFO", "PREPARELAUNCHER: Verificando assets do Shiginima...");
      ui.changeProgress(true, 10, 20);
      ui.changeMainLabel("Aguarde...");
      ui.changeSecondaryLabel("Verificando assets e carregando launcher interno...");
      File runtimeDir = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft");
      File launcherJSON = new File(runtimeDir + "/launcher_profiles.json");
      if (!launcherJSON.exists())
      {
        logger.log("INFO", "PREPARELAUNCHER: Assets de launcher não encontrado. Ajustes de performance serão feitos do zero.");
        ui.changeProgress(true, 20, 20);
        SystemRequirements req = new SystemRequirements();
        int ramMB = req.checkSystemRAMGB() * 1000;
        if (ramMB >= Integer.parseInt(SystemDTO.getReqsMaximumRAM()))
        {
          applySettingsProfile("max");
        }
        else if (ramMB >= Integer.parseInt(SystemDTO.getReqsMediumRAM()))
        {
          applySettingsProfile("med");
        }
        else
        {
          applySettingsProfile("min");
        }
        ui.changeProgress(true, 40, 20);
      }
      String category = "";
      switch (SessionDTO.getLastServer())
      {
        case "0":
          category = "DexCraft Factions";
          break;
        case "1":
          category = "DexCraft Pixelmon";
          break;
        case "2":
          category = "DexCraft Vanilla";
          break;
        case "3":
          category = "DexCraft Beta";
          break;
        default:
          break;
      }
      editJavaVersionOnJSON(launcherJSON, category);
      File shiginimaFile = new File(runtimeDir + "/shig.inima");
      editShiginimaAsset(shiginimaFile);
      ui.changeProgress(true, 80, 20);
      if(!SessionDTO.isOfflineModeOn())
      {
        applySettingsProfile("serverdat");
      }
      try
      {
        logger.log("INFO", "PREPARELAUNCHER: Finalizando...");
        ui.changeMainLabel("Concluído!");
        ui.changeSecondaryLabel("Concluído");
        ui.changeProgress(true, 100, 20);
        ui.changeMainLabel("");
        ui.changeSecondaryLabel("");
        ui.getProgressBar().setVisible(false);
        new ProcessBuilder("cmd", "/c", "javaw.exe -jar DexCraftBackgroundServices.jar").directory(DexCraftFiles.launcherFolder).start();
        logger.log("INFO", "PREPARELAUNCHER: DCBS inicializado." );
        Close.client();
      }
      catch (IOException ex)
      {
        alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().PrepareGameAndRun Thread");
      }
    }
  }

}
