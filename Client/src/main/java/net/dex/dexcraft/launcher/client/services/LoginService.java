package net.dex.dexcraft.launcher.client.services;


import javafx.application.Platform;
import javafx.concurrent.Task;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.dao.SqlDAO;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.dto.SqlDTO;
import net.dex.dexcraft.commons.tools.Crypto;
import net.dex.dexcraft.launcher.client.Client;
import static net.dex.dexcraft.launcher.client.Client.alerts;
import static net.dex.dexcraft.launcher.client.Client.switchStage;
import net.dex.dexcraft.launcher.client.LoginScreenController;
import static net.dex.dexcraft.launcher.client.LoginScreenController.loginUI;


/**
 * Performs login and verify credentials, fields and create new
 * account if needed.
 */
public class LoginService extends Task<Void>
{
  // Variable retrieving from Controller Caller
  public String serviceName = LoginScreenController.serviceName;

  /**
   * GET the Service Name.
   * @return the Service Name.
   */
  public String getServiceName()
  {
    return this.serviceName;
  }

  /**
   * Caller method.
   * @return null (ignored)
   */
  @Override
  protected Void call()
  {
    logger.log("INFO", "SERVIÇO: Iniciando o Serviço " + serviceName + "...");
    mainRoutine(getServiceName());
    return null;
  }

  /**
   * Service's main method.
   * @param service the service to perform:<br>
   * "LoginVerification" to validate login fields.
   * "LoginValidation" to validate password and account status.
   */
  public void mainRoutine(String service)
  {
    switch (service)
    {
      case "LoginVerification":
        Thread thread = new Thread(new LoginVerification());
        thread.setDaemon(true);
        thread.start();
        break;
      case "LoginValidation":
        Thread thread2 = new Thread(new LoginValidation());
        thread2.setDaemon(true);
        thread2.start();
        break;
      default:
        alerts.tryAgain();
        break;
    }
  }

  /**
  * Securely verify login fields, login attempts and<br>
  * perform new account creation.
  */
  public class LoginVerification extends Thread
  {
    // Variable retrieving from Controller Caller
    public String user = LoginScreenController.username;
    public String pass = LoginScreenController.password;

    boolean hasTheUserConfirmed;

    /**
     * Main method.
     */
    @Override
    public void run()
    {
      logger.log("INFO", "Iniciando Thread LoginVerification...");
      LoginScreenController.loginCode = validateFields(this.user, this.pass);
      if (LoginScreenController.loginCode == 0)
      {
        if (!SessionDTO.isOfflineModeOn())
        {
          LoginScreenController.serviceName = "LoginValidation";
          LoginService loginValidationService = new LoginService();
          new Thread(loginValidationService).start();
          while (LoginScreenController.serviceName.equals("LoginValidation"))
          {
            try
            {
              Thread.sleep(500);
            }
            catch (InterruptedException ex)
            {
              alerts.exceptionHandler(ex, "EXCEÇÃO EM LoginServices.LoginVerification()");
            }
          }
        }
      }
      returnToUI();
      logger.log("INFO", "Fechando Thread LoginVerification...");
    }

    /**
     * VersionsDTO login according to informed user and password.
     * @param user the username
     * @param password the account password
     * @return the code according to the data informed, as follows:<br>
     * 0 - the fields are ok<br>
     * 1 - username is empty<br>
     * 2 - password is empty<br>
     * 3 - password doesn't match the length required or contain spaces<br>
     * 4 - password contain letters or symbols<br>
     * 5 - user doesn't match the length required or contain spaces<br>
     * 6 - user contain special characters
     */
    public int validateFields(String user, String password)
    {
      if (user.isEmpty())
      {
        return 1;
      }
      else if (password.isEmpty())
      {
        return 2;
      }
      else if ( (password.length() < 4) || (password.length() > 8) || (password.contains(" ")) )
      {
        return 3;
      }
      else if (!password.matches("[0-9]+"))
      {
        return 4;
      }
      else if ( (user.length() < 3) || (user.length() > 12) || (user.contains(" ")) )
      {
        return 5;
      }
      else if (!user.matches("[a-zA-Z0-9]+"))
      {
        return 6;
      }
      else
      {
        return 0;
      }
    }

    /**
     * Brings the result of verification to the UI.
     * 1 - No username input<br>
     * 2 - No PIN input<br>
     * 3 - Incorrect PIN input<br>
     * 4 - PIN containing not-numbers<br>
     * 5 - Username with lenght bigger than allowed<br>
     * 6 - Username with not allowed characters<br>
     * 7 - New account confirmation<br>
     * 8 - Wrong username or password (security implementation) <br>
     */
    public void returnToUI()
    {
      switch (LoginScreenController.loginCode)
      {
        case 1:
          logger.log("INFO", "ERRO DE LOGIN: Insira um nome de usuário.");
          returnToUILabelModifications("user", "Insira um nome de usuário.");
          break;
        case 2:
          logger.log("INFO", "ERRO DE LOGIN: Insira um PIN de 4 a 8 dígitos.");
          returnToUILabelModifications("PIN", "Insira um PIN de 4 a 8 dígitos.");
          break;
        case 3:
          logger.log("INFO", "ERRO DE LOGIN: O PIN deve conter 4 a 8 dígitos, sem espaços.");
          returnToUILabelModifications("PIN", "O PIN deve conter 4 a 8 dígitos, sem espaços.");
          break;
        case 4:
          logger.log("INFO", "ERRO DE LOGIN: O PIN deve conter APENAS NÚMEROS.");
          returnToUILabelModifications("PIN", "O PIN deve conter APENAS NÚMEROS.");
          break;
        case 5:
          logger.log("INFO", "ERRO DE LOGIN: O nome de usuário deve conter 3 a 12 caracteres, sem espaços ou símbolos.");
          returnToUILabelModifications("PIN", "O nome de usuário deve conter 3 a 12 caracteres, sem espaços ou símbolos.");
          break;
        case 6:
          logger.log("INFO", "ERRO DE LOGIN: O nome de usuário NÃO pode conter caracteres especiais, números ou espaços.");
          returnToUILabelModifications("PIN", "O nome de usuário NÃO pode conter caracteres especiais, números ou espaços.");
          break;
        case 7:
          logger.log("INFO", "LOGIN: Usuário não foi encontrado. Uma nova conta será criada.");
          Platform.runLater(() ->
          {
            loginUI.setStyle("MainLabel", "-fx-text-fill: white");
            loginUI.changeMainLabel("Aguarde...");
            loginUI.changeMainLabelVisibility(true);
            loginUI.setStyle("MainTextField", "-fx-background-color: white;");
            loginUI.setStyle("MainPasswordField", "-fx-background-color: white;");
          });
          hasTheUserConfirmed = alerts.newAccount(user, pass);
          if (hasTheUserConfirmed)
          {
            LoginScreenController.loginCode = 9;
            Thread thread = new Thread(new LoginValidation());
            thread.setDaemon(true);
            thread.start();
            while (LoginScreenController.loginCode == 9)
            {
              try
              {
                Thread.sleep(500);
              }
              catch (InterruptedException ex)
              {
                alerts.exceptionHandler(ex, "EXCEÇÃO EM LoginServices.LoginVerification.returnToUI()");
              }
            }
            returnToUI();
          }
          else
          {
            Platform.runLater(() ->
            {
              loginUI.setStyle("MainLabel", "-fx-text-fill: white");
              loginUI.changeMainLabel("Aguarde...");
              loginUI.changeMainLabelVisibility(false);
              loginUI.setStyle("MainTextField", "-fx-background-color: white;");
              loginUI.setStyle("MainPasswordField", "-fx-background-color: white;");
              loginUI.changeMainTextField("");
              loginUI.changeMainPasswordField("");
              loginUI.setMainButtonDisable(false);
            });
          }
          break;
        case 8:
          logger.log("INFO", "ERRO DE LOGIN: Usuário ou senha incorretos.");
          returnToUILabelModifications("user", "Usuário ou senha incorretos (Tentativa " + LoginScreenController.errorAttempts + " de 3)." );
          break;
        default:
          logger.log("INFO", "LOGIN: Campos validados e login realizado.");
          LoginScreenController.errorAttempts = 0;
          Validate.gameCache(loginUI, user);
          SessionDTO.setSessionUser(user);
          pass = Crypto.encrypt(pass);
          SessionDTO.setSessionPassword(pass);
          Platform.runLater(() ->
          {
            switchStage.close();
            Client.setParent("MainWindow");
          });
          logger.log("INFO", "SERVIÇO: Serviço " + serviceName + " terminado.");
          break;
      }
    }

    /**
     * Method to custom label modifications while trying<br>
     * to input correct info in the login fields
     * @param fieldName the name of the login field
     * @param text the text to show in it.
     */
    public void returnToUILabelModifications(String fieldName, String text)
    {
      Platform.runLater(() ->
      {
        loginUI.setStyle("MainLabel", "-fx-text-fill: red;");
        loginUI.changeMainLabel(text);
        loginUI.setMainButtonDisable(false);
      });
      if (fieldName.equals("user"))
      {
        Platform.runLater(() ->
        {
          loginUI.setStyle("MainTextField", "-fx-background-color: red;");
        });
      }
      else
      {
        Platform.runLater(() ->
        {
          loginUI.setStyle("MainPasswordField", "-fx-background-color: red;");
        });
      }
    }
  }

  /**
   * Perform account verification or creation of a new one if needed.
   */
  public class LoginValidation extends Thread
  {
    // Variable retrieving from Controller Caller
    public String user = LoginScreenController.username;
    public String pass = LoginScreenController.password;

    // Variable retrieving from DTO
    public String dbClass = SqlDTO.getDBClass();
    public String dbName = SqlDTO.getDBName();
    public String dbDriver = SqlDTO.getDBDriver();
    public String dbAddress = SqlDTO.getDBAddress();
    public String dbPort = SqlDTO.getDBPort();
    public String dbUser = SqlDTO.getDBUser();
    public String dbPassword = SqlDTO.getDBPassword();
    SqlDAO db;

    /**
     * Main method.
     */
    @Override
    public void run()
    {
      logger.log("INFO", "Iniciando Thread LoginValidation...");
      if (LoginScreenController.loginCode == 9)
      {
        LoginScreenController.loginCode = insertPlayer(this.user, this.pass);
      }
      else
      {
        LoginScreenController.loginCode = checkDatabase(this.user, this.pass);
      }
      LoginScreenController.serviceName = "LoginVerification";
      logger.log("INFO", "Fechando Thread LoginValidation...");
      logger.log("INFO", "SERVIÇO: Serviço " + serviceName + " terminado.");
    }

    /**
     * Instanciate DAO properly.
     */
    public void callDatabaseConnector()
    {
      db = new SqlDAO(dbClass);
      db.setDBName(dbName);
      db.setDBDriver(dbDriver);
      db.setDBAddress(dbAddress);
      db.setDBPort(dbPort);
      db.setDBUser(dbUser);
      db.setDBPassword(dbPassword);
      db.connect();
      db.createTable();
    }

    /**
     * Create a new account with provided data to the Database.
     * @param user the username
     * @param pass the password
     * @return the status code for a successful operation (0).
     */
    public int insertPlayer(String user, String pass)
    {
      callDatabaseConnector();
      db.createTable();
      db.insertPlayer(user, pass);
      db.disconnect();
      return 0;
    }

    /**
     * Verify the database for the entry, while check if<br>
     * the user hadn't try to login more times than the allowed.
     * @param user the username
     * @param pass the password
     * @return the status code:<br>
     * 0 - account veryfied, present and logged successfully.
     * 7 - username not found, a new account may be created.
     * 8 - user or password incorrect. May trigger an alert<br>
     * about blocking the Launcher and the IP address to prevent<br>
     * hacking, depending of the number of attempts.
     */
    public int checkDatabase(String user, String pass)
    {
      if (LoginScreenController.errorAttempts == 3)
      {
        alerts.accountBlocked();
        return 8;
      }
      else
      {
        callDatabaseConnector();
        if (!db.playerExists(user))
        {
          db.disconnect();
          return 7;
        }
        else if (!pass.equals(db.getInfo("password", user)))
        {
          LoginScreenController.errorAttempts++;
          db.disconnect();
          return 8;
        }
        else
        {
          db.disconnect();
          return 0;
        }
      }
    }
  }

}
