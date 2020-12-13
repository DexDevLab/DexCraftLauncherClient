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
import javafx.scene.input.MouseEvent;
import net.dex.dexcraft.commons.tools.DexUI;
import net.dex.dexcraft.commons.tools.ErrorAlerts;

/**
 * FXML Controller Class.
 */
public class LoginScreenController implements Initializable
{

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


  public static String serviceName = "";
  public static String username = "";

  public static String password = "";

  public static int loginCode = 99;

  public static DexUI loginUI = new DexUI();
  public static ErrorAlerts loginAlerts = new ErrorAlerts();

  public static int errorAttempts = 0;


  @FXML
  public void doLogin(ActionEvent event)
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
    LoginServices login = new LoginServices();
    new Thread(login).start();
  }

  @FXML
  public void openAboutWindow(MouseEvent event)
  {
    Client.setParent("AboutWindow");
  }

  @FXML
  private void doClearField(MouseEvent event)
  {
    clearFields();
  }


  public void clearFields()
  {
    loginScreenUserField.setStyle("-fx-background-color: white;");
    loginScreenPINField.setStyle("-fx-background-color: white;");
    loginScreenWaitLabel.setStyle("-fx-text-fill: white");
    loginScreenWaitLabel.setText("Aguarde...");
    loginScreenWaitLabel.setVisible(false);
    loginScreenLoginButton.setDisable(false);
  }

  private void setTooltips()
  {

    loginScreenHelpName.setTooltip(loginUI.tooltipBuilder("O nome de usuário criado (3 a 12 caracteres, sem caracteres especiais) \n"
                                                        + "será utilizado também dentro do jogo e não poderá ser alterado."));
    loginScreenHelpPIN.setTooltip(loginUI.tooltipBuilder("A senha é um PIN de 4 a 8 dígitos numéricos."));
    loginScreenAbout.setTooltip(loginUI.tooltipBuilder("Sobre..."));
    loginScreenServerComboBox.setTooltip(loginUI.tooltipBuilder("Escolha um dos servidores para jogar."));
  }


  /**
   * Initializes the controller class.
   * @param url ignored
   * @param rb ignored
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    // Load tooltips
    setTooltips();

    // Load server options to connect and observables
    List<String> serverList =  new ArrayList<>();
    serverList.add("DexCraft Factions");
    serverList.add("DexCraft Pixelmon");
    serverList.add("DexCraft Vanilla");
    serverList.add("DexCraft Beta Server");
    loginScreenServerComboBox.getItems().addAll(serverList);
    loginScreenServerComboBox.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) ->
    {
      loginScreenLoginButton.setDisable(false);
      Validate.setLastServer(new_value.intValue());
      if ( (new_value.intValue() == 0) || (new_value.intValue() == 2) || (new_value.intValue() == 3) )
      {
        loginScreenLoginButton.setDisable(true);
        loginScreenServerComboBox.setTooltip(loginUI.tooltipBuilder("Servidor indisponível."));
      }
      else if (new_value.intValue() == 1)
      {
        loginScreenServerComboBox.setTooltip(loginUI.tooltipBuilder("Minere, construa, conquiste ginásios e capture pokémon!"));
      }
    });
    loginScreenServerComboBox.getSelectionModel().select(Integer.parseInt(Validate.lastServer));

    loginScreenUserField.setText(Validate.lastUser);

    Platform.runLater(()->
    {
      loginScreenLoginButton.requestFocus();
    });
  }
}