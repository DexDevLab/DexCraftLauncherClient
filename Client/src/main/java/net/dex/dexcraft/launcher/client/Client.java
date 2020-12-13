/**
 * DexCraft Launcher Client. This program is the main application
 * of the DexCraft Launcher. It is responsible for user login,
 * account management and minecraft client selection.
 */
package net.dex.dexcraft.launcher.client;

import java.io.IOException;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dex.dexcraft.commons.check.OfflineMode;
import net.dex.dexcraft.commons.tools.Close;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.commons.tools.ErrorAlerts;
import net.dex.dexcraft.commons.tools.Logger;

/**
  * @author Dex
  * @since 30/04/2020
  * @version v11.0.0-201213-2416
  *
  * Preloader Class with splash screen.
  */
public class Client extends Application
{

  static Scene preloaderScene;
  public static Stage preloaderStage;
  static Label preloaderLabel;
  static ProgressBar pbar;

  static Scene switchScene;
  public static Stage switchStage;
  static AnchorPane switchAnchorPane = new AnchorPane();

  public static ErrorAlerts alerts = new ErrorAlerts();
  public static Logger logger = new Logger();
  public static DexUI clientUI = new DexUI();


  /**
   * Dynamic label change in UI with additional logging.
   * @param text the text to be shown and logged.
   */
  public static void changeStatus(String text)
  {
    clientUI.changeMainLabel(text);
    logger.log("INFO", text);
  }

  /**
   * Starts the preloader screen stage.
   * @param primaryStage the stage itself (no need to specify)
   * @throws java.lang.Exception when can't load the Stack Pane
   */
  @Override
  public void start(Stage primaryStage) throws Exception
  {
    alerts.setImage(new Image(Client.class.getResourceAsStream("icon1.jpg")));
    FXMLLoader splashLoader = new FXMLLoader(Client.class.getResource("Preloader.fxml"));
    StackPane splashPane = splashLoader.load();
    preloaderStage = new Stage(StageStyle.TRANSPARENT);
    //Remove comment on next line to force focus on the Splash Screen
//    preloaderStage.setAlwaysOnTop(true);
    preloaderScene = new Scene(splashPane);
    preloaderStage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
    preloaderScene.setFill(Color.TRANSPARENT);
    Font.loadFont(Client.class.getResource("Minecrafter.Alt.ttf").toExternalForm(), 10);
    preloaderScene.getStylesheets().add(getClass().getResource("fxmlFont1.css").toExternalForm());
    preloaderStage.setScene(preloaderScene);
    preloaderStage.setResizable(false);
    pbar = new ProgressBar(0.0);
    preloaderLabel = new Label("Iniciando...");
    splashPane.getChildren().add(preloaderLabel);
    splashPane.getChildren().add(pbar);
    preloaderLabel.getStyleClass().add("mainlabel");
    pbar.setMaxSize(606, 11);
    pbar.setTranslateY(190);
    pbar.getStylesheets().add(getClass().getResource("gradientprogressbar2.css").toExternalForm());
    clientUI.setProgressBar(pbar);
    clientUI.setMainLabel(preloaderLabel);


    /** Opens a Service to show Splash before initialize the application **/
    Service<Boolean> splashService = new Service<Boolean>()
    {
      /** Show Splash Screen Stage **/
      @Override
      public void start()
      {
        preloaderStage.show();
        super.start();
      }

      /** Create a Task inside Service to interact with the UI Thread **/
      @Override
      protected Task<Boolean> createTask()
      {
        return new Task<Boolean>()
        {

          @Override
          protected Boolean call() throws Exception
          {
            //Logger settings
            logger.setLogLock(DexCraftFiles.logLock);
            logger.setMessageFormat("yyyy/MM/dd HH:mm:ss");
            logger.setLogNameFormat("yyyy-MM-dd--HH.mm.ss");
            logger.setLogDir(DexCraftFiles.logFolder);

            changeStatus("Iniciando...");
            clientUI.changeProgress(true, 10, 30);
            Thread musicPlayer = new Thread(new MusicPlayer());
            musicPlayer.setDaemon(true);
            musicPlayer.start();

            Validate.instance("Client");

            clientUI.changeProgress(true, 20, 30);

            if(!OfflineMode.IsRunning())
            {
              clientUI.changeProgress(true, 30, 30);
              changeStatus("Verificando versão do DexCraft Launcher Init...");

              Validate.provisionedComponent(DexCraftFiles.coreFile, DexCraftFiles.launcherProperties, "DexCraft Launcher Init",
                       "DexCraftLauncherInitVersion", "Versions", "LauncherProperties", "LauncherUpdates", "InitUpdate",
                       DexCraftFiles.tempFolder, DexCraftFiles.updateInitZip, DexCraftFiles.launcherFolder);
              clientUI.changeProgress(true, 40, 30);

              Validate.connectionAssets();
            }
            clientUI.changeProgress(true, 50, 30);

            Validate.versions();

            clientUI.changeProgress(true, 70, 30);
            if(!DexCraftFiles.coreFile.exists())
            {
              logger.log("***ERRO***", "COREFILE NÃO ENCONTRADO.");
              alerts.noCoreFile();
            }

            Validate.setOfflineMode();

            clientUI.changeProgress(true, 80, 30);

            Validate.getLastServer();

            clientUI.changeProgress(true, 90, 30);

            Validate.getLastUser();

            changeStatus("Abrindo DexCraft Launcher...");
            clientUI.changeProgress(true, 100, 30);
            logger.log("INFO", "Splash Screen terminada");
            return true;
          }
        };
      }

      /** When task above retuns "true", close the Splash Screen Stage and call Application. **/
      @Override
      protected void succeeded()
      {
        preloaderStage.close();
        setParent("LoginScreen");
        logger.log("INFO", "Janela de login aberta");
      }
    };
    splashService.start();
  }


  public static void setParent(String fxml)
  {
    try
    {
      Font.loadFont(Client.class.getResource("Minecrafter.Alt.ttf").toExternalForm(), 10);
      FXMLLoader load = new FXMLLoader(Client.class.getResource(fxml + ".fxml"));
      switchAnchorPane = load.load();
      switchScene = new Scene(switchAnchorPane);
      switchScene.getStylesheets().add(Client.class.getResource("fxmlFont1.css").toExternalForm());
      switchStage = new Stage();
      if (fxml.equals("AboutWindow"))
      {
        switchStage.setTitle("Sobre DexCraft Launcher");
        switchStage.setOnCloseRequest((e) ->
        {
          logger.log("INFO", "Janela " + fxml + " encerrada.");
          switchStage.close();
        });
      }
      else
      {
        switchStage.setOnCloseRequest((e) ->
        {
          logger.log("INFO", "Janela " + fxml + " encerrada.");
          switchStage.close();
          Close.close(1);
        });
      }
      if (fxml.equals("LoginScreen"))
      {
        switchStage.setTitle("DexCraft Launcher - Login");
      }
      switchStage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
      switchStage.setScene(switchScene);
      switchStage.setResizable(false);
      logger.log("INFO", "Abrindo janela " + fxml + "...");
      switchStage.show();
    }
    catch (IOException e)
    {
      alerts.exceptionHandler(e, "EXCEÇÃO EM Client.setParent(String)");
    }
  }

}