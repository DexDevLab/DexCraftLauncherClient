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
import net.dex.dexcraft.launcher.check.AdminExecution;
import net.dex.dexcraft.launcher.check.OfflineMode;
import net.dex.dexcraft.launcher.tools.Alerts;
import net.dex.dexcraft.launcher.tools.DexCraftFiles;
import net.dex.dexcraft.launcher.tools.DexUI;
import net.dex.dexcraft.launcher.tools.Logger;

/**
  * @author Dex
  * @since 30/04/2020
  * @version v11.0.0-201130-2392
  *
  * Preloader Class with splash screen.
  */
public class Client extends Application
{

  static Scene preloaderScene;
  static Stage preloaderStage;
  static Label preloaderLabel;
  static ProgressBar pbar;

  static Scene switchScene;
  static Stage switchStage;
  static AnchorPane switchAnchorPane = new AnchorPane();


  public static Alerts alerts = new Alerts();
  public static Logger logger = new Logger();
  public static DexUI ui = new DexUI();


  /**
   * Dynamic label change in UI with additional logging.
   * @param text the text to be shown and logged.
   */
  public static void changeStatus(String text)
  {
    ui.changeMainLabel(text);
    logger.log("INFO", text);
  }

  /**
   * Starts the preloader screen stage.
   * @param primaryStage the stage itself (no need to specify)
   * @throws java.lang.Exception when can't load the Stack Pane
   * @see #callMain()
   */
  @Override
  public void start(Stage primaryStage) throws Exception
  {
    //Logger settings
    logger.setLogLock(DexCraftFiles.logLock);
    logger.setMessageFormat("yyyy/MM/dd HH:mm:ss");
    logger.setLogNameFormat("yyyy-MM-dd--HH.mm.ss");
    logger.setLogDir(DexCraftFiles.logFolder);
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
    ui.setProgressBar(pbar);
    ui.setMainLabel(preloaderLabel);


    /** Opens a Service to show Splash before initialize the application **/
    Service<Boolean> splashService = new Service<Boolean>()
    {
      /** Show Splash Screen Stage **/
      @Override
      public void start()
      {
        preloaderStage.show();
        logger.log("INFO", "Logger inicializado.");
        logger.log("INFO", "Preloader inicializado");
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
//            PreventSecondInstance.PreventSecondInstance();
            changeStatus("Iniciando...");
            ui.changeProgress(true, 10, 30);
            AdminExecution.AdminExecution();
//            logger.log("INFO", "Preparando Cache...");
//            FileUtils.deleteQuietly(DexCraftFiles.tempFolder);
            ui.changeProgress(true, 20, 30);
            if(!OfflineMode.IsRunning())
            {
              ui.changeProgress(true, 30, 30);
//              changeStatus("Baixando Corefile...");
//              Download downloadCf = new Download();
//              downloadCf.coreFile();
              ui.changeProgress(true, 40, 30);
//              changeStatus("Verificando o sistema. Aguarde...");
//              SystemRequirements req = new SystemRequirements();
//              req.checkRequirements();
              ui.changeProgress(true, 50, 30);
//              changeStatus("Verificando recursos...");
//              Validate.resources();
              ui.changeProgress(true, 70, 30);
//              changeStatus("Verificando versão do DexCraft Launcher...");
//              Validate.provisionedComponent(DexCraftFiles.coreFile, DexCraftFiles.launcherProperties, "DexCraft Launcher",
//                       "DexCraftLauncherVersion", "Versions", "LauncherProperties", "LauncherUpdates", "DCLUpdate",
//                       DexCraftFiles.tempFolder, DexCraftFiles.updateLauncherZip, DexCraftFiles.launcherFolder);
              ui.changeProgress(true, 80, 30);
//              changeStatus("Verificando versão do DexCraft Background Services...");
//              Validate.provisionedComponent(DexCraftFiles.coreFile, DexCraftFiles.launcherProperties, "DexCraft Background Services",
//                       "DexCraftBackgroundServicesVersion", "Versions", "LauncherProperties", "LauncherUpdates", "DCBSUpdate",
//                       DexCraftFiles.tempFolder, DexCraftFiles.updateDCBSZip, DexCraftFiles.launcherFolder);
              changeStatus("Verificando versão do DexCraft Launcher Init...");
              Validate.provisionedComponent(DexCraftFiles.coreFile, DexCraftFiles.launcherProperties, "DexCraft Launcher Init",
                       "DexCraftLauncherInitVersion", "Versions", "LauncherProperties", "LauncherUpdates", "InitUpdate",
                       DexCraftFiles.tempFolder, DexCraftFiles.updateInitZip, DexCraftFiles.launcherFolder);
            }
            Validate.setOfflineMode();
            Validate.versions();
            ui.changeProgress(true, 90, 30);
            if(!DexCraftFiles.coreFile.exists())
            {
              logger.log("***ERRO***", "COREFILE NÃO ENCONTRADO.");
              alerts.noCoreFile();
            }
            changeStatus("Abrindo DexCraft Launcher...");
            ui.changeProgress(true, 100, 30);
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
      switchStage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
      switchStage.setScene(switchScene);
      switchStage.setResizable(false);
      if (fxml.equals("LoginScreen"))
      {
        switchStage.setTitle("DexCraft Launcher - Login");
        switchStage.setOnCloseRequest((e) ->
        {
          logger.log("INFO", "Janela de login encerrada.");
          switchStage.close();
        });
      }
      else if (fxml.equals("AboutWindow"))
      {
        logger.log("INFO", "Abrindo janela \"Sobre\"...");
        switchStage.setTitle("Sobre DexCraft Launcher");
        switchStage.setOnCloseRequest((e) ->
        {
          logger.log("INFO", "Janela \"Sobre\" encerrada.");
          switchStage.close();
        });
      }
      switchStage.show();
    }
    catch (IOException e)
    {
      alerts.exceptionHandler(e, "EXCEÇÃO EM Client.setParent(String)");
    }
  }

//////    private static Scene scene;
//////
//////    @Override
//////    public void start(Stage stage) throws IOException {
//////        scene = new Scene(loadFXML("primary"), 640, 480);
//////        stage.setScene(scene);
//////        stage.show();
//////    }
//////
//////    static void setRoot(String fxml) throws IOException {
//////        scene.setRoot(loadFXML(fxml));
//////    }
//////
//////    private static Parent loadFXML(String fxml) throws IOException {
//////        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource(fxml + ".fxml"));
//////        return fxmlLoader.load();
//////    }
//////
//////    public static void main(String[] args) {
//////        launch();
//////    }

}