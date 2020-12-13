package net.dex.dexcraft.launcher.client;


import javafx.application.Platform;
import javafx.concurrent.Task;
import net.dex.dexcraft.commons.tools.Database;
import net.dex.dexcraft.commons.tools.JSONUtility;


/**
 *
 */
public class LoginServices extends Task<Void>
{
  // Variable retrieving from Controller Caller
  public String serviceName = LoginScreenController.serviceName;

  JSONUtility ju = new JSONUtility();

  public String getServiceName()
  {
    return this.serviceName;
  }

  @Override
  protected Void call() throws Exception
  {
    Client.logger.log("INFO", "SERVIÇO: Iniciando o Serviço " + serviceName + "...");
    mainRoutine();
    Client.logger.log("INFO", "SERVIÇO: Serviço " + serviceName + " terminado.");
    return null;
  }

  public void mainRoutine()
  {
    switch (getServiceName())
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
        LoginScreenController.loginAlerts.tryAgain();
        break;
    }
  }


  public class LoginValidation extends Thread
  {
    // Variable retrieving from Controller Caller
    public String user = LoginScreenController.username;
    public String pass = LoginScreenController.password;

    @Override
    public void run()
    {
      Client.logger.log("INFO", "Iniciando Thread LoginValidation...");
      if (LoginScreenController.loginCode == 9)
      {
        LoginScreenController.loginCode = insertPlayer(this.user, this.pass);
      }
      else
      {
        LoginScreenController.loginCode = checkDatabase(this.user, this.pass);
      }
      LoginScreenController.serviceName = "LoginVerification";
      Client.logger.log("INFO", "Fechando Thread LoginValidation...");
    }


    public int insertPlayer(String user, String pass)
    {
      Database db = new Database(Validate.dbClass);
      db.createTable();
      db.insertPlayer(user, pass, generateTimestamp());
      return 0;
    }


    public String generateTimestamp()
    {
      return Long.toString(System.currentTimeMillis());
    }


    public int checkDatabase(String user, String pass)
    {
      if (LoginScreenController.errorAttempts == 3)
      {
        LoginScreenController.loginAlerts.accountBlocked();
        return 8;
      }
      else
      {
        Database db = new Database(Validate.dbClass);
        db.connect();
        db.createTable();
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


  public class LoginVerification extends Thread
  {
    // Variable retrieving from Controller Caller
    public String user = LoginScreenController.username;
    public String pass = LoginScreenController.password;

    boolean hasTheUserConfirmed;

    @Override
    public void run()
    {
      Client.logger.log("INFO", "Iniciando Thread LoginVerification...");
      LoginScreenController.loginCode = validateFields(this.user, this.pass);
      if (LoginScreenController.loginCode == 0)
      {
        LoginScreenController.serviceName = "LoginValidation";
        LoginServices loginValidationService = new LoginServices();
        new Thread(loginValidationService).start();
        while (LoginScreenController.serviceName.equals("LoginValidation"))
        {
          try
          {
            Thread.sleep(500);
//            Client.logger.log("INFO", "Aguardando finalização da Thread Login Validation...");
          }
          catch (InterruptedException ex)
          {
            Client.alerts.exceptionHandler(ex, "EXCEÇÃO EM LoginServices.LoginVerification()");
          }
        }
      }
      returnToUI();
      Client.logger.log("INFO", "Fechando Thread LoginVerification...");
    }

    /**
     * Validate login according to informed user and password.
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


    public void returnToUI()
    {
      switch (LoginScreenController.loginCode)
      {
        case 1:
          Client.logger.log("INFO", "ERRO DE LOGIN: Insira um nome de usuário.");
          returnToUILabelModifications("user", "Insira um nome de usuário.");
          break;
        case 2:
          Client.logger.log("INFO", "ERRO DE LOGIN: Insira um PIN de 4 a 8 dígitos.");
          returnToUILabelModifications("PIN", "Insira um PIN de 4 a 8 dígitos.");
          break;
        case 3:
          Client.logger.log("INFO", "ERRO DE LOGIN: O PIN deve conter 4 a 8 dígitos, sem espaços.");
          returnToUILabelModifications("PIN", "O PIN deve conter 4 a 8 dígitos, sem espaços.");
          break;
        case 4:
          Client.logger.log("INFO", "ERRO DE LOGIN: O PIN deve conter APENAS NÚMEROS.");
          returnToUILabelModifications("PIN", "O PIN deve conter APENAS NÚMEROS.");
          break;
        case 5:
          Client.logger.log("INFO", "ERRO DE LOGIN: O nome de usuário deve conter 3 a 12 caracteres, sem espaços ou símbolos.");
          returnToUILabelModifications("PIN", "O nome de usuário deve conter 3 a 12 caracteres, sem espaços ou símbolos.");
          break;
        case 6:
          Client.logger.log("INFO", "ERRO DE LOGIN: O nome de usuário NÃO pode conter caracteres especiais, números ou espaços.");
          returnToUILabelModifications("PIN", "O nome de usuário NÃO pode conter caracteres especiais, números ou espaços.");
          break;
        case 7:
          Client.logger.log("INFO", "LOGIN: Usuário não foi encontrado. Uma nova conta será criada.");
          Platform.runLater(() ->
          {
            LoginScreenController.loginUI.setStyle("MainLabel", "-fx-text-fill: white");
            LoginScreenController.loginUI.changeMainLabel("Aguarde...");
            LoginScreenController.loginUI.changeMainLabelVisibility(true);
            LoginScreenController.loginUI.setStyle("MainTextField", "-fx-background-color: white;");
            LoginScreenController.loginUI.setStyle("MainPasswordField", "-fx-background-color: white;");
          });
          hasTheUserConfirmed = LoginScreenController.loginAlerts.newAccount(user, pass);
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
                LoginScreenController.loginAlerts.exceptionHandler(ex, "EXCEÇÃO EM LoginServices.LoginVerification.returnToUI()");
              }
            }
            returnToUI();
          }
          else
          {
            Platform.runLater(() ->
            {
              LoginScreenController.loginUI.setStyle("MainLabel", "-fx-text-fill: white");
              LoginScreenController.loginUI.changeMainLabel("Aguarde...");
              LoginScreenController.loginUI.changeMainLabelVisibility(false);
              LoginScreenController.loginUI.setStyle("MainTextField", "-fx-background-color: white;");
              LoginScreenController.loginUI.setStyle("MainPasswordField", "-fx-background-color: white;");
              LoginScreenController.loginUI.changeMainTextField("");
              LoginScreenController.loginUI.changeMainPasswordField("");
              LoginScreenController.loginUI.setMainButtonDisable(false);
            });
          }
          break;
        case 8:
          Client.logger.log("INFO", "ERRO DE LOGIN: Usuário ou senha incorretos.");
          returnToUILabelModifications("user", "Usuário ou senha incorretos (Tentativa " + LoginScreenController.errorAttempts + " de 3)." );
          break;
        default:
          Client.logger.log("INFO", "LOGIN: Campos validados e login realizado.");
          Validate.setLastUser(user);
          LoginScreenController.errorAttempts = 0;
          Client.setParent("MainWindow");
          break;
      }
    }


    public void returnToUILabelModifications(String fieldName, String text)
    {
      Platform.runLater(() ->
      {
        LoginScreenController.loginUI.setStyle("MainLabel", "-fx-text-fill: red;");
        LoginScreenController.loginUI.changeMainLabel(text);
        LoginScreenController.loginUI.setMainButtonDisable(false);
      });
      if (fieldName.equals("user"))
      {
        Platform.runLater(() ->
        {
          LoginScreenController.loginUI.setStyle("MainTextField", "-fx-background-color: red;");
        });
      }
      else
      {
        Platform.runLater(() ->
        {
          LoginScreenController.loginUI.setStyle("MainPasswordField", "-fx-background-color: red;");
        });
      }
    }
  }

}
