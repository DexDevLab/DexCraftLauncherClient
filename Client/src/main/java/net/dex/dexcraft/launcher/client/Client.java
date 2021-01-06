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
import javafx.geometry.Pos;
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
import net.dex.dexcraft.commons.Commons;
import net.dex.dexcraft.commons.check.OfflineMode;
import net.dex.dexcraft.commons.dto.FtpDTO;
import net.dex.dexcraft.commons.dto.SqlDTO;
import net.dex.dexcraft.commons.dto.UrlsDTO;
import net.dex.dexcraft.commons.dto.VersionsDTO;
import net.dex.dexcraft.commons.tools.Close;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.commons.tools.ErrorAlerts;
import net.dex.dexcraft.commons.tools.Logger;
import net.dex.dexcraft.launcher.client.services.MusicPlayerService;
import net.dex.dexcraft.launcher.client.services.Validate;

/**
  * @author Dex
  * @since 30/04/2020
  * @version v11.0.3-210106-2732
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
  public static DexUI preloaderUI = new DexUI();

  static String clientVersionForTitle = "";


  /**
   * Dynamic label change in UI with additional logging.
   * @param ui the DexUI instance
   * @param mainLabelText the main label text (used to the logger aswell)
   * @param secLabelText the secondary label text. It can't be null but can be empty.
   */
  public static void changeStatus(DexUI ui, String mainLabelText, String secLabelText)
  {
    ui.changeMainLabel(mainLabelText);
    if (!secLabelText.equals(""))
    {
      ui.changeSecondaryLabel(secLabelText);
    }
    logger.log("INFO", mainLabelText);
  }

  /**
   * Starts the preloader screen stage.
   * @param primaryStage the stage itself (no need to specify)
   * @throws java.lang.Exception when can't load the Stack Pane
   */
  @Override
  public void start(Stage primaryStage) throws Exception
  {
    //Icon set for alerts's window
    alerts.setImage(new Image(Client.class.getResourceAsStream("icon1.jpg")));
    //DexCraft Commons alerts binding
    Commons.setErrorAlerts(alerts);

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
    preloaderLabel.getStyleClass().add("mainlabel");
    preloaderLabel.setMaxSize(480, 20);
    preloaderLabel.setTranslateY(135);
    preloaderLabel.setAlignment(Pos.CENTER);
    preloaderLabel.toFront();
    pbar.setMaxSize(480, 11);
    pbar.setTranslateY(150);
    pbar.getStylesheets().add(getClass().getResource("gradientprogressbar2.css").toExternalForm());
    pbar.toFront();
    splashPane.getChildren().add(preloaderLabel);
    splashPane.getChildren().add(pbar);
    preloaderUI.setProgressBar(pbar);
    preloaderUI.setMainLabel(preloaderLabel);


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
            //DexCraft Commons logger binding
            Commons.setLogger(logger);

            changeStatus(preloaderUI, "Iniciando...", "");
            preloaderUI.changeProgress(true, 10, 35);

            // Starts music playback
            Thread musicPlayer = new Thread(new MusicPlayerService());
            musicPlayer.setDaemon(true);
            musicPlayer.start();

            preloaderUI.changeProgress(true, 20, 35);
            changeStatus(preloaderUI, "Validando instância...", "");

            // Validates current instance
            Validate.instance("Client");

            preloaderUI.changeProgress(true, 30, 35);
            changeStatus(preloaderUI, "Coletando dados...", "");

            //Read URLs for downloads and updates
            UrlsDTO.parseURLs();

            preloaderUI.changeProgress(true, 40, 35);

            // Do tasks below only if launcher isn't on Offline Mode
            if(!OfflineMode.IsRunning())
            {
              changeStatus(preloaderUI, "Verificando versão do DexCraft Launcher Init...", "");

              // Check and update Init version if needed
              Validate.provisionedComponent(preloaderUI, "Init", 50);

              changeStatus(preloaderUI, "Verificando banco de dados...", "");

              // Read the connection assets from JSON properties file
              SqlDTO.parseSQLAssets();

              preloaderUI.changeProgress(true, 60, 35);
              changeStatus(preloaderUI, "Verificando comunicação com Servidor FTP...", "");

              // Read the connection assets from JSON properties file
              FtpDTO.parseFTPAssets();

              preloaderUI.changeProgress(true, 70, 35);
            }
            preloaderUI.changeProgress(true, 80, 35);

            clientVersionForTitle = VersionsDTO.getDexCraftLauncherClientVersion().substring(0, VersionsDTO.getDexCraftLauncherClientVersion().indexOf("-"));

            preloaderUI.changeProgress(true, 90, 35);
            changeStatus(preloaderUI, "Abrindo DexCraft Launcher...", "");
            preloaderUI.changeProgress(true, 100, 35);
            logger.log("INFO", "JAVAFX: Splash Screen terminada");
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
        logger.log("INFO", "JAVAFX: Janela de login aberta");
      }
    };
    splashService.start();
  }

  /**
   * Define the current Parent according to<br>
   * desired Stage to show on current Scene.
   * @param fxml the FXML file from the Controller.
   */
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
          logger.log("INFO", "JAVAFX: Janela " + fxml + " encerrada.");
          switchStage.close();
        });
      }
      else
      {
        switchStage.setOnCloseRequest((e) ->
        {
          logger.log("INFO", "JAVAFX: Janela " + fxml + " encerrada.");
          switchStage.close();
          Close.quit();
        });
      }
      if (fxml.equals("LoginScreen"))
      {
        switchStage.setTitle("DexCraft Launcher " + clientVersionForTitle + " - Login");
      }
      else if (fxml.equals("MainWindow"))
      {
        switchStage.setTitle("DexCraft Launcher " + clientVersionForTitle);
      }
      switchStage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
      switchStage.setScene(switchScene);
      switchStage.setResizable(false);
      logger.log("INFO", "JAVAFX: Abrindo janela " + fxml + "...");
      switchStage.show();
    }
    catch (IOException e)
    {
      alerts.exceptionHandler(e, "EXCEÇÃO EM Client.setParent(String)");
    }
  }

}