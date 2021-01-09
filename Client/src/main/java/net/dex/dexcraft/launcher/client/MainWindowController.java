package net.dex.dexcraft.launcher.client;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.tools.Close;
import net.dex.dexcraft.commons.tools.DexCraftFiles;
import net.dex.dexcraft.commons.tools.DexUI;
import static net.dex.dexcraft.launcher.client.Client.switchStage;
import net.dex.dexcraft.launcher.client.services.AccountSyncService;
import net.dex.dexcraft.launcher.client.services.BgImageRandomService;
import net.dex.dexcraft.launcher.client.services.MainService;
import net.dex.dexcraft.launcher.client.services.PingService;
import net.dex.dexcraft.launcher.client.services.PrepareLauncherService;
import net.dex.dexcraft.launcher.client.services.Validate;
import org.apache.commons.io.FileUtils;


/**
 * FXML Controller class
 */
public class MainWindowController implements Initializable
{

  @FXML
  private AnchorPane mainWindowAnchorPane;

  // Easter Egg
  @FXML
  private Label mainWindowTitle;

  @FXML
  public ImageView mainWindowBGImage;

  @FXML
  private Button mainWindowPlayButton;

  // MenuBar
  @FXML
  private MenuBar mainWindowMenuBar;

  //Menu Conta
  @FXML
  private Menu menuConta;

  @FXML
  private CustomMenuItem menuItemDoChangeSkin;

  @FXML
  private Label labelDoChangeSkin;


  @FXML
  private CheckBox checkBoxBackupSingleplayer;

  @FXML
  private CustomMenuItem menuItemDoBackup;

  @FXML
  private Label labelDoBackup;

  @FXML
  private CustomMenuItem menuItemRestoreBackup;

  @FXML
  private Label labelRestoreBackup;

  @FXML
  private CustomMenuItem menuItemLogout;

  @FXML
  private Label labelLogout;

  //Menu Extras
  @FXML
  private Menu menuExtras;

  @FXML
  private Menu menuSoundpacks;

  @FXML
  private CustomMenuItem menuItemExtraSoundChocoboV2;

  @FXML
  private Label labelSoundChocoboV2;

  @FXML
  private CustomMenuItem menuItemExtraSoundDCDeluxeV2;

  @FXML
  private Label labelSoundDCDeluxeV2;

  @FXML
  private Menu menuShaders;

  @FXML
  private CustomMenuItem menuItemShader1;

  @FXML
  private Label labelShader1;

  //Menu Avançado
  @FXML
  private Menu menuAvancado;

  @FXML
  private CustomMenuItem menuItemMinCfg;

  @FXML
  private Label labelMinCfg;

  @FXML
  private CustomMenuItem menuItemMaxCfg;

  @FXML
  private Label labelMaxCfg;

  @FXML
  private CustomMenuItem menuItemRemoveJVMArgs;

  @FXML
  private Label labelRemoveJVMArgs;

  @FXML
  private CustomMenuItem menuItemInstallDefultCfg;

  @FXML
  private Label labelInstallDefaultCfg;

  @FXML
  private CustomMenuItem menuItemOfflineMode;

  @FXML
  private CheckBox checkBoxOfflineMode;

  //Menu Ajuda
  @FXML
  private Menu menuAjuda;

  @FXML
  private CustomMenuItem menuItemReinstallClient;

  @FXML
  private Label labelReinstallClient;

  @FXML
  private CustomMenuItem menuItemCleanLogs;

  @FXML
  private Label labelCleanLogs;

  @FXML
  private CustomMenuItem menuItemAbout;

  @FXML
  private Label labelAbout;

  // Selected server icon
  @FXML
  public ImageView mainWindowServerIcon;

  // Selected server description
  @FXML
  private Label mainWindowServerDesc;

  //Current task explanation
  @FXML
  private Label mainWindowTaskStatus;

  //Progress Bar
  @FXML
  private ProgressBar mainWindowPBar1;

  // Progress Info
  @FXML
  private Label mainWindowProgressOverall;

  @FXML
  private Label mainWindowLabelMS;

  @FXML
  private Label mainWindowLabelPing;

  @FXML
  private ImageView mainWindowIconPing;

  public static DexUI mainUI = new DexUI();

  public static String serviceName = "";

  public static int serverIndex = 0;

  public static boolean isAccountSyncDone = true;

  public static String logDirSize = "";


  /**
   * Opens browser to show URL
   * @param event the mouse event.
   */
  @FXML
  void doCallEasterEgg(MouseEvent event)
  {
    try
    {
      new ProcessBuilder("cmd", "/c", "start " + SessionDTO.getEasterEggURL()).start();
    }
    catch (IOException ex)
    {
      logger.log(ex, "EXCEÇÃO em MainWindowController.doCallEasterEgg(MouseEvent)");
    }
  }

  /**
   * Performs Skin change.
   * @param event the action event
   */
  @FXML
  void doChangeSkin(ActionEvent event)
  {
    String component = "";
    switch (serverIndex)
    {
      case 0:
        component = "dc";
        break;
      case 1:
        component = "dcpx";
        break;
      case 2:
        component = "dcvn";
        break;
      case 3:
        component = "dcb";
        break;
      default:
        break;
    }
    File skinFolder = new File(DexCraftFiles.launcherFolder + "/" + component + "/.minecraft/skin");
    if (skinFolder.exists())
    {
      FileUtils.deleteQuietly(skinFolder);
      skinFolder.mkdirs();
    }
    isAccountSyncDone = false;

    FileChooser chooser = new FileChooser();
    chooser.setTitle("Selecione a Skin");
    File file = chooser.showOpenDialog(switchStage);
    File destination = new File(skinFolder + "/" + file.getName());
    if (file != null)
    {
      try
      {
        Files.copy(file.toPath(), destination.toPath());
      }
      catch (IOException ex)
      {
        Alert alerts = new Alert(Alert.AlertType.ERROR);
        alerts.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) alerts.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
        stage.setOnCloseRequest((e) -> {Close.withErrors();});
        alerts.getButtonTypes().clear();
        alerts.setTitle("Erro no Client");
        alerts.setHeaderText("");
        alerts.setContentText("Erro crítico durante a implantação da skin.\n"
                              + "Reinicie o Launcher novamente.");
        ButtonType btnok = new ButtonType("OK");
        alerts.getButtonTypes().add(btnok);
        Optional<ButtonType> result = alerts.showAndWait();
        if (result.get() == btnok)
        {
          Close.withErrors();
        }
      }
      serviceName = "ChangeSkin";
      isAccountSyncDone = true;
      callPrepareLauncherService();
    }
  }


  /**
   * Forces backup, pushing data to the FTP Server.
   * @param event the action event.
   */
  @FXML
  void doBackup(ActionEvent event)
  {
    Alert alerts = new Alert(Alert.AlertType.CONFIRMATION);
    alerts.initModality(Modality.APPLICATION_MODAL);
    Stage stage = (Stage) alerts.getDialogPane().getScene().getWindow();
    stage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
    stage.setOnCloseRequest((e) -> {e.consume();});
    alerts.getButtonTypes().clear();
    alerts.setTitle("Realizar backup");
    alerts.setHeaderText("");
    alerts.setContentText("Você deseja realizar o backup?\n"
                         +"Você irá sobrescrever quaisquer backups seus.");
    ButtonType btnsim = new ButtonType("Sim");
    ButtonType btnnao = new ButtonType("Não");
    alerts.getButtonTypes().add(btnsim);
    alerts.getButtonTypes().add(btnnao);
    Optional<ButtonType> result = alerts.showAndWait();
    if (result.get() == btnsim)
    {
      serviceName = "Backup";
      callAccountSyncService();
    }
  }

  /**
   * Forces restore, pulling data from the FTP Server.
   * @param event the action event.
   */
  @FXML
  void doBackupRestore(ActionEvent event)
  {
    Alert alerts = new Alert(Alert.AlertType.CONFIRMATION);
    alerts.initModality(Modality.APPLICATION_MODAL);
    Stage stage = (Stage) alerts.getDialogPane().getScene().getWindow();
    stage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
    stage.setOnCloseRequest((e) -> {e.consume();});
    alerts.getButtonTypes().clear();
    alerts.setTitle("Realizar backup");
    alerts.setHeaderText("");
    alerts.setContentText("Você deseja restaurar seus dados por um backup no servidor?\n"
                         +"Você irá sobrescrever quaisquer dados seus.");
    ButtonType btnsim = new ButtonType("Sim");
    ButtonType btnnao = new ButtonType("Não");
    alerts.getButtonTypes().add(btnsim);
    alerts.getButtonTypes().add(btnnao);
    Optional<ButtonType> result = alerts.showAndWait();
    if (result.get() == btnsim)
    {
      serviceName = "Restore";
      callAccountSyncService();
    }
  }

  /**
   * Calls About Window.
   * @param event the action event.
   */
  @FXML
  void doOpenAboutWindow(ActionEvent event)
  {
    Client.setParent("AboutWindow");
  }

  /**
   * Perform log folder cleaning.
   * @param event the action event.
   */
  @FXML
  void doCleanLogs(ActionEvent event)
  {
    Alert alerts = new Alert(Alert.AlertType.CONFIRMATION);
    alerts.initModality(Modality.APPLICATION_MODAL);
    Stage stage = (Stage) alerts.getDialogPane().getScene().getWindow();
    stage.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
    stage.setOnCloseRequest((e) -> {e.consume();});
    alerts.getButtonTypes().clear();
    alerts.setTitle("Limpar arquivos de log");
    alerts.setHeaderText("");
    alerts.setContentText("Você deseja limpar o diretório de logs?\n"
                         + "Essa ação NÃO poderá ser desfeita.");
    ButtonType btnsim = new ButtonType("Sim");
    ButtonType btnnao = new ButtonType("Não");
    alerts.getButtonTypes().add(btnsim);
    alerts.getButtonTypes().add(btnnao);
    Optional<ButtonType> result = alerts.showAndWait();
    if (result.get() == btnsim)
    {
      logDirSize = logger.cleanLogs();
      Alert alerts2 = new Alert(Alert.AlertType.INFORMATION);
      alerts2.initModality(Modality.APPLICATION_MODAL);
      Stage stage2 = (Stage) alerts2.getDialogPane().getScene().getWindow();
      stage2.getIcons().add(new Image(Client.class.getResourceAsStream("icon1.jpg")));
      stage2.setOnCloseRequest((e) -> {e.consume();});
      alerts2.getButtonTypes().clear();
      alerts2.setTitle("Limpeza Concluída");
      alerts2.setHeaderText("");
      alerts2.setContentText(logDirSize + " de dados foram limpos.");
      ButtonType btnok = new ButtonType("OK");
      alerts2.getButtonTypes().add(btnok);
      Optional<ButtonType> result2 = alerts2.showAndWait();
      if (result2.get() == btnok){}
    }
  }

  /**
   * Perform the installation of the default
   * configuration presets<br> for running the
   * selected game client.
   * @param event the action event.
   */
  @FXML
  void doInstallDefaultCfg(ActionEvent event)
  {
    // TODO
  }

  /**
   * Perform the installation of the maximum
   * configuration presets<br> for running the
   * selected game client.
   * @param event the action event.
   */
  @FXML
  void doInstallMaxCfg(ActionEvent event)
  {
    // TODO
  }

  /**
   * Perform the installation of the minimum
   * configuration presets<br> for running the
   * selected game client.
   * @param event the action event.
   */
  @FXML
  void doInstallMinCfg(ActionEvent event)
  {
    // TODO
  }

  /**
   * Remove all JVM args from game client execution.
   * @param event the action event.
   */
  @FXML
  void doInstallNoJVMArgs(ActionEvent event)
  {
    // TODO
  }

  /**
   * Install a shader.
   * @param event the action event.
   */
  @FXML
  void doInstallShader1(ActionEvent event)
  {
    // TODO
  }

  /**
   * Install a soundpack.
   * @param event the action event.
   */
  @FXML
  void doInstallSoundChocoboV2(ActionEvent event)
  {
    serviceName = "InstallSoundDCPXChocoboV2";
    callPrepareLauncherService();
  }

  /**
   * Install a soundpack.
   * @param event the action event.
   */
  @FXML
  void doInstallSoundDCDeluxeV2(ActionEvent event)
  {
    // TODO
  }

  /**
   * Performs logout and shows the login screen.
   * @param event the action event.
   */
  @FXML
  void doLogout(ActionEvent event)
  {
    SessionDTO.setSessionPassword("null");
    Validate.bgImageRandomizerCaller = "null";
    Validate.isPingServiceOnMainWindowRunning = false;
    switchStage.close();
    Client.setParent("LoginScreen");
  }

  /**
   * Calls backup sync and run the game.
   * @param event the action event.
   */
  @FXML
  void doPlay(ActionEvent event)
  {
    serviceName = "PlayGame";
    callAccountSyncService();
    callPrepareLauncherService();
  }

  /**
   * Performs game client reinstall.
   * @param event the action event.
   */
  @FXML
  void doReinstallClient(ActionEvent event)
  {
    // TODO
  }


  /**
   * Initializes the controller class.
   * @param url ignored
   * @param rb ignored
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    serverIndex = Integer.parseInt(SessionDTO.getLastServer());

    // Load Ping Service
    callPingService();

    // Start background image randomizer service
    callBackgroundImageService();

    // Define options according to selected server on login
    setSelectedServer(serverIndex);

    // Load tooltips
    setTooltips();

    // Load Menu Styles
    setMenuStyles();

    // Backup SinglePlayer Maps Listener
    checkBoxBackupSingleplayer.setSelected(SessionDTO.isBackupSingleplayerMapsEnabled());
    checkBoxBackupSingleplayer.selectedProperty().addListener(new ChangeListener<Boolean>()
    {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
      {
        SessionDTO.setBackupSingleplayerMaps(newValue);
      }
    });

    // Disable menu bar in order to wait for Main Service
    mainWindowMenuBar.setDisable(true);

    // Load Main Service
    callMainService();
  }

  /**
   * Calls the Account Sync Service.
   */
  public void callAccountSyncService()
  {
    AccountSyncService account = new AccountSyncService();
    account.setUI(mainUI);
    new Thread(account).start();
  }

  /**
   * Calls the Prepare Launcher Service.
   */
  public void callPrepareLauncherService()
  {
    PrepareLauncherService prepare = new PrepareLauncherService();
    prepare.setUI(mainUI);
    new Thread(prepare).start();
  }

  /**
   * Calls the Main Service.
   */
  private void callMainService()
  {
    mainUI.setMainLabel(mainWindowTaskStatus);
    mainUI.setSecondaryLabel(mainWindowProgressOverall);
    mainUI.setProgressBar(mainWindowPBar1);
    mainUI.setMenuBar(mainWindowMenuBar);
    mainUI.setMainButton(mainWindowPlayButton);
    MainService main = new MainService();
    main.setUI(mainUI);
    new Thread(main).start();
  }

  /**
   * Calls the Background Randomizer Service.
   */
  private void callBackgroundImageService()
  {
    Validate.bgImageRandomizerCaller = "mainWindow";
    mainUI.setStage(switchStage);
    mainUI.setMainImageView(mainWindowBGImage);
    BgImageRandomService bg = new BgImageRandomService();
    new Thread(bg).start();
  }

  /**
   * Calls the Ping Monitoring Service.
   */
  private void callPingService()
  {
    Validate.isPingServiceOnLoginRunning = false;
    mainUI.setPingIcon(mainWindowIconPing);
    mainUI.setPingLabel(mainWindowLabelMS);
    mainUI.setPingLabelTooltip(mainWindowLabelPing);
    PingService pingMainWindow = new PingService();
    pingMainWindow.setPingIcon(mainUI.getPingIcon());
    pingMainWindow.setPingLabel(mainUI.getPingLabel());
    pingMainWindow.setPingLabelTooltip(mainUI.getPingLabelTooltip());
    pingMainWindow.setServiceName("MainWindow");
    Validate.isPingServiceOnMainWindowRunning = true;
    new Thread(pingMainWindow).start();
  }

  /**
   * Define the tooltips.
   */
  private void setTooltips()
  {
    // Ping Label
    mainWindowLabelPing.setTooltip(mainUI.tooltipBuilder("Latência com o Servidor de Sincronização"));

    //Menu Conta
    checkBoxBackupSingleplayer.setTooltip(mainUI.tooltipBuilder
      ("Ativa a sincronização dos mundos do Modo Singleplayer para sua conta."));
    labelDoBackup.setTooltip(mainUI.tooltipBuilder
      ("Força o backup dos seus dados atuais do seu computador para sua conta online."));
    labelRestoreBackup.setTooltip(mainUI.tooltipBuilder
      ("Força o backup dos seus dados atuais da sua conta online para seu computador."));
    labelLogout.setTooltip(mainUI.tooltipBuilder
      ("Desconecta a conta de usuário atual e retorna para a tela de login."));

    //Menu Extras
    labelSoundChocoboV2.setTooltip(mainUI.tooltipBuilder
      ("Instala o pacote de som \"No Batidão do Chocobo (v2)\" no client do servidor escolhido."));
    labelSoundDCDeluxeV2.setTooltip(mainUI.tooltipBuilder
      ("Instala o pacote de som \"DexCraft Deluxe Soundpack (v2)\" no client do servidor escolhido."));
    labelShader1.setTooltip(mainUI.tooltipBuilder
      ("Instala o shader selecionado no client do servidor escolhido."));

    //Menu Avançado
    labelMinCfg.setTooltip(mainUI.tooltipBuilder
      ("Aplica as configurações de gráfico e de jogo no mínimo."));
    labelMaxCfg.setTooltip(mainUI.tooltipBuilder
      ("Aplica as configurações de gráfico e de jogo no máximo."));
    labelRemoveJVMArgs.setTooltip(mainUI.tooltipBuilder
      ("Remove todos os argumentos JVM para o servidor escolhido."));
    labelInstallDefaultCfg.setTooltip(mainUI.tooltipBuilder
      ("Aplica as configurações de gráfico e de jogo padrão."));
    checkBoxOfflineMode.setTooltip(mainUI.tooltipBuilder
      ("Ativa ou desativa o Modo Offline.\n"
        + "No Modo Offline, seu jogo não será sincronizado \n"
        + "e você poderá jogar apenas no Singleplayer."));

    //Menu Ajuda
    labelReinstallClient.setTooltip(mainUI.tooltipBuilder
      ("Reinstala o client selecionado.\n"
        + "Essa opção afeta apenas o client do servidor selecionado."));
    labelCleanLogs.setTooltip(mainUI.tooltipBuilder
      ("Limpa a pasta de logs."));
    labelCleanLogs.setText("Limpar logs... (" + logger.getLogSize() + ")");
    labelAbout.setTooltip(mainUI.tooltipBuilder
      ("Sobre DexCraft Launcher..."));
  }

  /**
   * Disable some menus if needed.
   * @param serverInd the serverIndex based on the chosen game client.
   */
  private void disableMenus(int serverInd)
  {
    if (SessionDTO.isOfflineModeOn())
    {
      menuItemDoBackup.setDisable(true);
      menuItemRestoreBackup.setDisable(true);
    }
    switch (serverInd)
    {
      case 0:
        break;
      case 1:
        // Menu Avançado
        menuAvancado.setVisible(false);

        // Menu Extras
        menuExtras.setVisible(false);

        // Menu Ajuda
        menuItemReinstallClient.setDisable(true);
        break;
      case 2:
        break;
      case 3:
        break;
      default:
        // Menu Avançado
        menuAvancado.setVisible(false);

        // Menu Extras
        menuExtras.setVisible(false);

        // Menu Ajuda
        menuItemReinstallClient.setDisable(true);
        menuItemCleanLogs.setDisable(true);
        break;
    }
  }

  /**
   * Changes the window according to the selected server.
   */
  private void setSelectedServer(int serverInd)
  {

    switch (serverInd)
    {
      case 0:
        mainWindowServerIcon.setImage((new Image(Client.class.getResourceAsStream("dcicon2.png"))));
        mainWindowServerDesc.setText("Tente sobreviver um mundo de mobs hardcore enquanto conquista \n "
                                      + "territórios e vence as facções inimigas!");
        break;
      case 1:
        mainWindowServerIcon.setImage((new Image(Client.class.getResourceAsStream("dcpxicon2.png"))));
        mainWindowServerDesc.setText("Minere, construa, conquiste ginásios e capture pokémon!");
        break;
      case 2:
        mainWindowServerIcon.setImage((new Image(Client.class.getResourceAsStream("dcvnicon2.png"))));
        mainWindowServerDesc.setText("O bom e velho Vanilla, do jeito que você gosta.");
        break;
      case 3:
        mainWindowServerIcon.setImage((new Image(Client.class.getResourceAsStream("dcbicon2.jpg"))));
        mainWindowServerDesc.setText("Servidor em desenvolvimento. Entre por sua própria conta e risco!");
        break;
      default:
        break;
    }
    // Disable some menu options while it's under development
    disableMenus(serverIndex);
    // Default option to disable all menus
//////    disableMenus(99);

  }

  /**
   * Define the menu styles.
   */
  private void setMenuStyles()
  {
    mainWindowMenuBar.setId("default-menu");
    menuConta.setText(SessionDTO.getSessionUser());
    menuConta.setId("username-main-menu");
    checkBoxBackupSingleplayer.setId("menu-item-bold");
    labelRemoveJVMArgs.setId("menu-item-bold");
    checkBoxOfflineMode.setId("menu-item-bold");
    labelReinstallClient.setId("menu-item-bold");
  }

}

