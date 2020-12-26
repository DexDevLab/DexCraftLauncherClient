package net.dex.dexcraft.launcher.client;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.launcher.client.services.LoginService;
import net.dex.dexcraft.launcher.client.services.PingService;
import net.dex.dexcraft.launcher.client.services.Validate;

/**
 * FXML Controller Class.
 */
public class LoginScreenController implements Initializable
{

  @FXML
  private AnchorPane loginScreenAnchorPane;

  @FXML
  public TextField loginScreenUserField;

  @FXML
  public Button loginScreenLoginButton;

  @FXML
  public Label loginScreenWaitLabel;

  @FXML
  public PasswordField loginScreenPINField;

  @FXML
  public Label loginScreenHelpName;

  @FXML
  public Label loginScreenHelpPIN;

  @FXML
  public Label loginScreenAbout;

  @FXML
  private ComboBox<String> loginScreenServerComboBox;

  @FXML
  public Label loginScreenLabelPing;

  @FXML
  public ImageView loginScreenIconPing;

  @FXML
  public Label loginScreenLabelMS;


  public static String serviceName = "";
  public static String username = "";
  public static String password = "";

  public static int errorAttempts = 0;
  public static int loginCode = 99;

  public static DexUI loginUI = new DexUI();

  /**
   * Performs the login on launcher, retrieving<br>
   * data from database.
   * @param event the action event.
   */
  @FXML
  private void doLogin(ActionEvent event)
  {
    loginScreenLoginButton.setDisable(true);
    Client.logger.log("INFO", "Realizando login...");
    loginScreenWaitLabel.setText("Aguarde...");
    loginScreenWaitLabel.setVisible(true);

    // UI Tool Bindings
    loginUI.setMainLabel(loginScreenWaitLabel);
    loginUI.setMainTextField(loginScreenUserField);
    loginUI.setMainPasswordField(loginScreenPINField);
    loginUI.setMainButton(loginScreenLoginButton);

    username = loginScreenUserField.getText();
    password = loginScreenPINField.getText();
    serviceName = "LoginVerification";
    LoginService login = new LoginService();
    new Thread(login).start();
  }

  /**
   * Calls About Window.
   * @param event the mouse event.
   */
  @FXML
  private void doOpenAboutWindow(MouseEvent event)
  {
    Client.setParent("AboutWindow");
  }

  /**
   * Clear all fields after changing it because of some
   * login error.
   * @param event the mouse event.
   */
  @FXML
  private void doClearField(MouseEvent event)
  {
    loginScreenUserField.setStyle("-fx-background-color: white;");
    loginScreenPINField.setStyle("-fx-background-color: white;");
    loginScreenWaitLabel.setStyle("-fx-text-fill: white");
    loginScreenWaitLabel.setText("Aguarde...");
    loginScreenWaitLabel.setVisible(false);
    loginScreenLoginButton.setDisable(false);
  }

  /**
   * Initializes the controller class.
   * @param url ignored
   * @param rb ignored
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    // Updates ping
    callPingService();

    // Load tooltips
    setTooltips();

    loginScreenUserField.setText(SessionDTO.getSessionUser());
    loginScreenUserField.setId("server-combo-box");
    loginScreenPINField.setId("server-combo-box");

    // Put focus on login button
    Platform.runLater(()->
    {
      loginScreenLoginButton.requestFocus();
    });


    // Load server options to connect and listener
    List<String> serverList =  new ArrayList<>();
    serverList.add("DexCraft Factions");
    serverList.add("DexCraft Pixelmon");
    serverList.add("DexCraft Vanilla");
    serverList.add("DexCraft Beta Server");
    loginScreenServerComboBox.setId("server-combo-box");
    loginScreenServerComboBox.getItems().addAll(serverList);
    loginScreenServerComboBox.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) ->
    {

      // Changes the last server selected on JSON file
      SessionDTO.setLastServer(Integer.toString(new_value.intValue()));

      if ( (new_value.intValue() == 0) || (new_value.intValue() == 2) || (new_value.intValue() == 3) )
      {
        loginScreenLoginButton.setDisable(true);
        loginScreenServerComboBox.setTooltip(loginUI.tooltipBuilder("Servidor indisponível."));
      }
      else if (new_value.intValue() == 1)
      {
        loginScreenLoginButton.setDisable(false);
        loginScreenServerComboBox.setTooltip(loginUI.tooltipBuilder("Minere, construa, conquiste ginásios e capture pokémon!"));
      }
    });
    loginScreenServerComboBox.getSelectionModel().select(Integer.parseInt(SessionDTO.getLastServer()));
    if (Integer.parseInt(SessionDTO.getLastServer()) == 1)
    {
      loginScreenLoginButton.setDisable(false);
    }

  }

  /**
   * Call the Ping Service.
   */
  private void callPingService()
  {
    PingService pingLoginScreen = new PingService();
    loginUI.setPingIcon(loginScreenIconPing);
    loginUI.setPingLabel(loginScreenLabelMS);
    loginUI.setPingLabelTooltip(loginScreenLabelPing);
    pingLoginScreen.setPingIcon(loginUI.getPingIcon());
    pingLoginScreen.setPingLabel(loginUI.getPingLabel());
    pingLoginScreen.setPingLabelTooltip(loginUI.getPingLabelTooltip());
    pingLoginScreen.setServiceName("LoginScreen");
    Validate.isPingServiceOnLoginRunning = true;
    new Thread(pingLoginScreen).start();
  }

  /**
   * Set all label tooltips.
   */
  private void setTooltips()
  {
    loginScreenHelpName.setTooltip(loginUI.tooltipBuilder("O nome de usuário criado (3 a 12 caracteres, sem caracteres especiais) \n"
                                                        + "será utilizado também dentro do jogo e não poderá ser alterado."));
    loginScreenHelpPIN.setTooltip(loginUI.tooltipBuilder("A senha é um PIN de 4 a 8 dígitos numéricos."));
    loginScreenAbout.setTooltip(loginUI.tooltipBuilder("Sobre..."));
    loginScreenServerComboBox.setTooltip(loginUI.tooltipBuilder("Escolha um dos servidores para jogar."));
    loginScreenLabelPing.setTooltip(loginUI.tooltipBuilder("Latência com o Servidor de Sincronização"));
  }

}