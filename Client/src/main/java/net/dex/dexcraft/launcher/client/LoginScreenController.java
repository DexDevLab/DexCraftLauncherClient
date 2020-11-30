package net.dex.dexcraft.launcher.client;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import net.dex.dexcraft.launcher.tools.Account;
import net.dex.dexcraft.launcher.tools.DexUI;

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
  public Label loginScreenHelp;

  @FXML
  public Label loginScreenAbout;

  @FXML
  private ComboBox<String> loginScreenServerComboBox;

  public String username;

  public String password;


  @FXML
  private void doLogin(ActionEvent event)
  {
    Client.logger.log("INFO", "Realizando login...");
    loginScreenWaitLabel.setVisible(true);
    username = loginScreenUserField.getText();
    password = loginScreenPINField.getText();
    if(!Validate.offlineMode)
    {
      int loginCode = 0;
      Account login = new Account();
      loginCode = login.login(username, password);
      switch (loginCode)
      {
        case 1:
          Client.logger.log("INFO", "ERRO DE LOGIN: Insira um nome de usuário.");
          loginScreenWaitLabel.setText("Insira um nome de usuário.");
          loginScreenWaitLabel.setStyle("-fx-text-fill: red;");
          loginScreenUserField.setStyle("-fx-background-color: red;");
          break;
        case 2:
          Client.logger.log("INFO", "ERRO DE LOGIN: Insira um PIN de 4 a 8 dígitos.");
          loginScreenWaitLabel.setText("Insira um PIN de 4 a 8 dígitos.");
          loginScreenWaitLabel.setStyle("-fx-text-fill: red;");
          loginScreenPINField.setStyle("-fx-background-color: red;");
          break;
        case 3:
          Client.logger.log("INFO", "ERRO DE LOGIN: O PIN deve conter 4 a 8 dígitos, sem espaços.");
          loginScreenWaitLabel.setText("O PIN deve conter 4 a 8 dígitos, sem espaços.");
          loginScreenWaitLabel.setStyle("-fx-text-fill: red;");
          loginScreenPINField.setStyle("-fx-background-color: red;");
          break;
        case 4:
          Client.logger.log("INFO", "ERRO DE LOGIN: O PIN deve conter APENAS NÚMEROS.");
          loginScreenWaitLabel.setText("O PIN deve conter APENAS NÚMEROS.");
          loginScreenWaitLabel.setStyle("-fx-text-fill: red;");
          loginScreenPINField.setStyle("-fx-background-color: red;");
          break;
        case 5:
          Client.logger.log("INFO", "ERRO DE LOGIN: O nome de usuário deve conter 3 a 12 caracteres, sem espaços ou símbolos.");
          loginScreenWaitLabel.setText("O nome de usuário deve conter 3 a 12 caracteres, sem espaços ou símbolos.");
          loginScreenWaitLabel.setStyle("-fx-text-fill: red;");
          loginScreenUserField.setStyle("-fx-background-color: red;");
          break;
        case 6:
          Client.logger.log("INFO", "ERRO DE LOGIN: O nome de usuário NÃO pode conter caracteres especiais, números ou espaços.");
          loginScreenWaitLabel.setText("O nome de usuário NÃO pode conter caracteres especiais, números ou espaços.");
          loginScreenWaitLabel.setStyle("-fx-text-fill: red;");
          loginScreenUserField.setStyle("-fx-background-color: red;");
          break;
        default:
          Client.logger.log("INFO", "LOGIN: Campos validados.");
          break;
      }
    }
    else
    {
      Client.logger.log("INFO", "O login com o Servidor não será validado devido ao Modo Offline.");
    }

  }

  @FXML
  public void openAboutWindow(MouseEvent event)
  {
    Client.setParent("AboutWindow");
  }

  @FXML
  void doClearField(MouseEvent event)
  {
    loginScreenUserField.setStyle("-fx-background-color: white;");
    loginScreenPINField.setStyle("-fx-background-color: white;");
    loginScreenWaitLabel.setStyle("-fx-text-fill: white");
    loginScreenWaitLabel.setText("Aguarde...");
    loginScreenWaitLabel.setVisible(false);
  }

  private void setTooltips()
  {
    loginScreenHelp.setTooltip(DexUI.tooltipBuilder("O nome de usuário criado (4 a 12 caracteres, sem caracteres especiais) \n"
                                             + "será utilizado também dentro do jogo e não poderá ser alterado.\n"
                                             + "A senha é um PIN de 4 a 8 dígitos numéricos."));
    loginScreenAbout.setTooltip(DexUI.tooltipBuilder("Sobre..."));
    loginScreenServerComboBox.setTooltip(DexUI.tooltipBuilder("Escolha um dos servidores para jogar."));
  }


  /**
   * Initializes the controller class.
   * @param url ignored
   * @param rb ignored
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    setTooltips();
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
        loginScreenServerComboBox.setTooltip(DexUI.tooltipBuilder("Servidor indisponível."));
      }
      else if (new_value.intValue() == 1)
      {
        loginScreenServerComboBox.setTooltip(DexUI.tooltipBuilder("Minere, construa, conquiste ginásios e capture pokémon!"));
      }
    });
    loginScreenServerComboBox.getSelectionModel().select(Integer.parseInt(Validate.getLastServer()));
  }
}