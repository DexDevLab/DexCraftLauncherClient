package net.dex.dexcraft.launcher.client.services;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.concurrent.Task;
import static net.dex.dexcraft.commons.Commons.alerts;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.check.SystemRequirements;
import net.dex.dexcraft.commons.dao.JsonDAO;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.dto.SystemDTO;
import net.dex.dexcraft.commons.dto.UrlsDTO;
import net.dex.dexcraft.commons.tools.Close;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.commons.tools.Download;
import net.dex.dexcraft.commons.tools.FileIO;
import net.dex.dexcraft.commons.tools.Install;
import net.dex.dexcraft.launcher.client.MainWindowController;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
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

  public String resourcePackToKeep = "";
  public File pack = new File ("C:/null.dc");
  public String packUrl;

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
        Thread.sleep(200);
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
    ui.setMenuBarDisable(true);
    ui.getProgressBar().setVisible(true);
    ui.resetProgress();
    ui.changeProgress(false, -1, 10);
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
        break;
      case "ChangeSkin":
        // Runs Thread to install a specific skin
        Thread thread2 = new Thread(new ChangeSkin());
        thread2.setDaemon(true);
        thread2.start();
        break;
      case "InstallSoundDCPXChocoboV2":
        // Runs Thread to install a specific skin
        Thread thread3 = new Thread(new InstallResourcePack());
        thread3.setDaemon(true);
        thread3.start();
        break;
      case "InstallMinCfg":
        // Runs Thread to install a specific config profile
        Thread thread4 = new Thread(new InstallConfig());
        thread4.setDaemon(true);
        thread4.start();
        break;
      case "InstallMedCfg":
        // Runs Thread to install a specific config profile
        Thread thread5 = new Thread(new InstallConfig());
        thread5.setDaemon(true);
        thread5.start();
        break;
      case "InstallMaxCfg":
        // Runs Thread to install a specific config profile
        Thread thread6 = new Thread(new InstallConfig());
        thread6.setDaemon(true);
        thread6.start();
        break;
      case "InstallNoJVMArgs":
        // Runs Thread to install a specific config profile
        Thread thread8 = new Thread(new InstallConfig());
        thread8.setDaemon(true);
        thread8.start();
        break;
      default:
        alerts.tryAgain();
        break;
    }
    while (!serviceName.equals("null"))
    {
      try
      {
        Thread.sleep(500);
      }
      catch (InterruptedException ex)
      {
        // Thread interruption ignored
//        alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().mainRoutine()");
      }
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
        alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.applySettingsProfile(String)");
      }
    });
    apply.start();
    while (apply.isAlive())
    {
      ui.changeProgress(false, -1, 10);
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
      ui.changeProgress(true, 10, 10);
      ui.changeMainLabel("Aguarde...");
      ui.changeSecondaryLabel("Verificando assets e carregando launcher interno...");
      File runtimeDir = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft");
      File launcherJSON = new File(runtimeDir + "/launcher_profiles.json");
      if (!launcherJSON.exists())
      {
        logger.log("INFO", "PREPARELAUNCHER: Assets de launcher não encontrado. Ajustes de performance serão feitos do zero.");
        ui.changeProgress(true, 20, 10);
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
        ui.changeProgress(true, 40, 10);
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
      ui.changeProgress(true, 80, 10);
      if(!SessionDTO.isOfflineModeOn())
      {
        applySettingsProfile("serverdat");
      }
      try
      {
        parseResourcePacks();
        File parseResources = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/resources.dc");
        List<String> readList = FileUtils.readLines(parseResources, "UTF-8");
        readList.forEach((item)->
        {
          if (component.equals("dcpx"))
          {
            if (item.contains("No Batidao do Chocobo (1.12.2-v2)"))
            {
              serviceName = "InstallSoundDCPXChocoboV2";
            }
          }
        });
        Thread thread = new Thread(new InstallResourcePack());
        thread.setDaemon(true);
        thread.start();
        while (thread.isAlive())
        {
          Thread.sleep(1000);
        }
        logger.log("INFO", "PREPARELAUNCHER: Finalizando...");
        ui.changeMainLabel("Concluído!");
        ui.changeSecondaryLabel("Concluído");
        ui.changeProgress(true, 100, 10);
        ui.changeMainLabel("");
        ui.changeSecondaryLabel("");
        ui.getProgressBar().setVisible(false);
        new ProcessBuilder("cmd", "/c", "javaw.exe -jar DexCraftBackgroundServices.jar").directory(DexCraftFiles.launcherFolder).start();
        logger.log("INFO", "PREPARELAUNCHER: DCBS inicializado." );
        Close.client();
      }
      catch (IOException | InterruptedException ex)
      {
        alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().PrepareGameAndRun Thread");
      }
    }
  }

  /**
   * Install a specific configuration preset to the player account
   */
  public class InstallConfig extends Thread
  {

    /**
     * Main method.
     */
    @Override
    public void run()
    {
      switch (serviceName)
      {
        case "InstallMinCfg":
          applySettingsProfile("min");
          break;
        case "InstallMedCfg":
          applySettingsProfile("med");
          break;
        case "InstallMaxCfg":
          applySettingsProfile("max");
          break;
        case "InstallNoJVMArgs":
          applySettingsProfile("nojvm");
          break;
        default:
          alerts.tryAgain();
          break;
      }
      ui.changeMainLabel("");
      ui.changeSecondaryLabel("");
      ui.getProgressBar().setVisible(false);
      ui.getMainButton().setDisable(false);
      ui.setMenuBarDisable(false);
      MainWindowController.isAccountSyncDone = false;
      serviceName = "null";
    }
  }

  /**
   * Install a specific skin to the player account
   */
  public class ChangeSkin extends Thread
  {

    /**
     * Main method.
     */
    @Override
    public void run()
    {
      installSkin();
      ui.changeMainLabel("");
      ui.changeSecondaryLabel("");
      ui.getProgressBar().setVisible(false);
      ui.getMainButton().setDisable(false);
      ui.setMenuBarDisable(false);
      MainWindowController.isAccountSyncDone = false;
      serviceName = null;
    }
  }


  /**
   * Install a specific resource pack to the profile.
   */
  public class InstallResourcePack extends Thread
  {
    /**
     * Main method.
     */
    @Override
    public void run()
    {
      ui.changeProgress(true, 10, 10);
      ui.changeMainLabel("Aguarde...");
      ui.changeSecondaryLabel("Verificando...");

      if (serviceName.equals("InstallSoundDCPXChocoboV2"))
      {
        pack = new File(DexCraftFiles.soundDCPXChocoboV2.toString());
        packUrl = UrlsDTO.getSoundDCPXChocoboV2();
      }
      ui.changeProgress(true, 30, 10);
      File resourcePackFolder = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/resourcepacks");
      if ( (!pack.getName().equals("null.dc")) && (!pack.exists()) )
      {
        ui.resetProgress();
        ui.changeMainLabel("Baixando soundpack...");

        Download download = new Download();
        Thread downloadThread = new Thread(()->
        {
          download.zipResource(packUrl,resourcePackFolder, pack);
        });
        while (downloadThread.isAlive())
        {
          ui.changeProgress(true, Double.parseDouble(download.getProgressPercent().replace(",",".")), 10);
          ui.changeSecondaryLabel(download.getTimeEstimatedMsg());
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // ignored
          }
        }

        ui.changeProgress(true, 100, 10);
        ui.changeSecondaryLabel("Concluído!");
      }
      if (!pack.getName().equals("null.dc"))
      {
        installResourcePack(pack.getName(), true);
      }
      ui.changeMainLabel("");
      ui.changeSecondaryLabel("");
      ui.getProgressBar().setVisible(false);
      ui.getMainButton().setDisable(false);
      ui.setMenuBarDisable(false);
      serviceName = null;
    }
  }


  /**
   * Parse currently installed resourcePacks and export it
   * to a file.
   */
  public void parseResourcePacks()
  {
    try
    {
      File optionsTxt = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/options.txt");
      if (!optionsTxt.exists())
      {
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
      }
      List<String> readList = FileUtils.readLines(optionsTxt, "UTF-8");
      readList.forEach((item)->
      {
        if (item.contains("resourcePacks:"))
        {
          try
          {
            item = item.substring(14);
            item = item.replace("[", "");
            item = item.replace("]", "");
            String[] resourcePackResult = item.split(",");
            File parseResources = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/resources.dc");
            if (parseResources.exists())
            {
              FileUtils.deleteQuietly(parseResources);
            }
            FileUtils.touch(parseResources);
            List<String> output = new ArrayList<>();
            for (String result : resourcePackResult)
            {
              if (result.contains(" \""))
              {
                result = result.substring(1);
              }
              if ((result.contains("default.zip")) || (result.contains("16x")) || (result.contains("512x")))
              {
                output.add("TexturePack: " + result);
              }
              else
              {
                output.add("SoundPack: " + result);
              }
            }
            output.forEach((item2)->
            {
              try
              {
                FileUtils.writeStringToFile(parseResources, item2 + "\n", "UTF-8", true);
              }
              catch (IOException ex)
              {
                alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
              }
            });
          }
          catch (IOException ex)
          {
            alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
          }
        }
      });
    }
    catch (IOException ex)
    {
      alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
    }
  }

  /**
   * Install a specific resource pack.
   * @param pack the sound pack to be installed. Must be a String<br>
   * with the proper pack name.
   * @param isSoundpack true if the resource is a soundpack, false if it's a texturepack.
   */
  public void installResourcePack(String pack, boolean isSoundpack)
  {
    try
    {
      ui.changeProgress(true, 20, 10);
      ui.changeMainLabel("Aguarde...");
      ui.changeSecondaryLabel("Verificando...");

      parseResourcePacks();
      File optionsTxt = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/options.txt");
      File parseResources = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/resources.dc");
      List<String> readList = FileUtils.readLines(optionsTxt, "UTF-8");
      FileUtils.deleteQuietly(optionsTxt);
      FileUtils.touch(optionsTxt);

      ui.changeProgress(true, 50, 10);
      ui.changeSecondaryLabel("Aplicando configurações...");

      try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(optionsTxt))))
      {
        readList.forEach((item)->
        {
          try
          {
            if ( (!item.contains("incompatibleResourcePacks:")) && (item.contains("resourcePacks:")) )
            {
              List<String> read = FileUtils.readLines(parseResources, "UTF-8");
              read.forEach((item2)->
              {
                if ( (isSoundpack) && (item2.contains("TexturePack: ")) )
                {
                  item2 = item2.replace("TexturePack: ", "");
                  resourcePackToKeep = item2;
                }
                else if ( (!isSoundpack) && (item2.contains("SoundPack: ")) )
                {
                  item2 = item2.replace("SoundPack: ", "");
                  resourcePackToKeep = item2;
                }
              });
              String add = "";
              if (resourcePackToKeep.equals(""))
              {
                add = "]";
              }
              else
              {
                add = ", " + resourcePackToKeep + "]";
              }
              item = "resourcePacks:" + "[" + "\"" + pack + "\"" + add;
            }
            bw.write(item);
            bw.newLine();
          }
          catch (IOException ex)
          {
            alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
          }
        });
        ui.changeProgress(true, 100, 10);
        ui.changeMainLabel("Concluído");
        ui.changeSecondaryLabel("Concluído");
      }
      installSkin();
    }
    catch (IOException ex)
    {
      alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
    }
  }


  /**
   * Install a skin, injecting it inside the resource pack.
   */
  public void installSkin()
  {
    try
    {
      ui.changeProgress(true, 10, 10);
      ui.changeMainLabel("Aguarde...");
      ui.changeSecondaryLabel("Verificando skin...");

      File skinFolder = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/skin");
      File skinFile1 = new File(skinFolder + "/steve.png");
      File skinFile2 = new File(skinFolder + "/alex.png");
      FileUtils.deleteQuietly(skinFile1);
      FileUtils.deleteQuietly(skinFile2);
      Collection<File> skinFile = FileUtils.listFiles(skinFolder, null, true);
      if (!skinFile.isEmpty())
      {
        skinFile.forEach((item)->
        {
          if (item.getName().contains(".png"))
          {
            try
            {
              FileUtils.copyFile(item, skinFile1);
              FileUtils.copyFile(item, skinFile2);
            }
            catch (IOException ex)
            {
              alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
            }
          }
          else
          {
            logger.log("***ERRO***", "NÃO FOI POSSÍVEL ENCONTRAR SKINS NO REPOSITÓRIO SELECIONADO!");
          }
        });
        ui.changeProgress(true, 30, 10);
        ui.changeMainLabel("Aguarde...");
        ui.changeSecondaryLabel("Alterando configurações...");

        File parseResources = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/resources.dc");
        parseResourcePacks();

        ui.changeProgress(true, 50, 10);

        List<String> read = FileUtils.readLines(parseResources, "UTF-8");
        read.forEach((item2)->
        {
          if (item2.contains("TexturePack: "))
          {
            item2 = item2.replace("TexturePack: ", "");
            resourcePackToKeep = item2;
          }
        });
        resourcePackToKeep = resourcePackToKeep.replace("\"", "");
        File resourcePack = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/resourcepacks/" + resourcePackToKeep);
        File dest = new File(DexCraftFiles.tempFolder + "/resourcepack");
        if (dest.exists())
        {
          dest.mkdirs();
        }
        File skinDest = new File(dest + "/assets/minecraft/textures/entity/");

        ui.changeProgress(true, 60, 10);
        ui.changeMainLabel("Aguarde...");
        ui.changeSecondaryLabel("Aplicando skin no perfil de jogo...");

        Install extractResourcePack = new Install();
        Thread extractThread = new Thread(()->
        {
          extractResourcePack.downloadedZipResource(resourcePack, dest);
        });
        extractThread.start();

        ui.resetProgress();

        while (extractThread.isAlive())
        {
          ui.changeProgress(true, -1, 10);
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // ignored
          }
        }

        ui.resetProgress();
        ui.changeProgress(true, 60, 10);

        FileIO fio = new FileIO();
        fio.copiar(skinFile1,skinDest);
        fio.copiar(skinFile2, skinDest);

        ui.changeProgress(true, 80, 10);
        ui.changeMainLabel("Aguarde...");
        ui.changeSecondaryLabel("Aplicando skin no perfil de jogo...");

        Thread zipThread = new Thread(()->
        {
          createZipFile();
        });
        zipThread.start();

        ui.resetProgress();

        while (zipThread.isAlive())
        {
          ui.changeProgress(true, -1, 10);
          try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // ignored
          }
        }

        ui.resetProgress();
        ui.changeMainLabel("");
        ui.changeSecondaryLabel("Concluído");
        ui.changeProgress(true, 100, 10);
      }
    }
    catch (IOException ex)
    {
      alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
    }
  }


  /**
   * Recriates resource pack in zip file form.
   */
  public void createZipFile()
  {
    try
    {
      File fil = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/resourcepacks/" + resourcePackToKeep);
      FileUtils.deleteQuietly(fil);
      FileUtils.touch(fil);
      // Create zip file stream.
      try (ZipArchiveOutputStream archive = new ZipArchiveOutputStream(new FileOutputStream(fil)))
      {
        File folderToZip = new File(DexCraftFiles.tempFolder + "/resourcepack");
        Files.walk(folderToZip.toPath()).forEach(p ->
        {
          File file = p.toFile();
          if (!file.isDirectory())
          {
            String filepath = file.toString();
            filepath = filepath.substring(filepath.lastIndexOf(folderToZip.getName()), filepath.length());
            filepath = filepath.replace(folderToZip.getName(), "");
            filepath = filepath.substring(1);
            ZipArchiveEntry entry_1 = new ZipArchiveEntry(file, filepath);
            try (FileInputStream fis = new FileInputStream(file))
            {
              archive.putArchiveEntry(entry_1);
              IOUtils.copy(fis, archive);
              archive.closeArchiveEntry();
            }
            catch (IOException e)
            {
              alerts.exceptionHandler(e, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
            }
          }
        });
        archive.finish();
      }
      catch (IOException e)
      {
        alerts.exceptionHandler(e, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
      }
    }
    catch (IOException ex)
    {
      alerts.exceptionHandler(ex, "EXCEÇÃO em PrepareLauncherService.call().ChangeSkin Thread");
    }
  }

}
