package net.dex.dexcraft.launcher.client.services;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static net.dex.dexcraft.commons.Commons.alerts;
import static net.dex.dexcraft.commons.Commons.logger;
import net.dex.dexcraft.commons.dto.SessionDTO;
import net.dex.dexcraft.commons.dto.SystemDTO;
import net.dex.dexcraft.commons.tools.Connections;
import net.dex.dexcraft.launcher.client.Client;
import static net.dex.dexcraft.launcher.client.LoginScreenController.loginUI;


/**
 * Monitors server latency.
 */
public class PingService extends Task<Void>
{
  // Variable retrieving from Controller Caller
  public String serviceName;

  public boolean isServiceNeeded;

  public Label pingLabel;

  public Label tooltipLabel;

  public ImageView pingIcon;


  /**
   * SET the name of Service.
   * @param name the name of the Service.
   */
  public void setServiceName(String name)
  {
    this.serviceName= name;
  }

  /**
   * GET the label which informs the ping.
   * @return the label.
   */
  public Label getPingLabel()
  {
    return this.pingLabel;
  }

  /**
   * GET the label which will have a tooltip.
   * @return the label.
   */
  public Label getPingLabelTooltip()
  {
    return this.tooltipLabel;
  }

  /**
   * GET the ImageView which have the ping icon.
   * @return the ImageView.
   */
  public ImageView getPingIcon()
  {
    return this.pingIcon;
  }

  /**
   * SET the label which informs the ping.
   * @param lb the label.
   */
  public void setPingLabel(Label lb)
  {
    this.pingLabel = lb;
  }

  /**
   * SET the label which will have a tooltip.
   * @param lb the label
   */
  public void setPingLabelTooltip(Label lb)
  {
    this.tooltipLabel = lb;
  }

  /**
   * SET the ImageView which have the ping icon.
   * @param img the ImageView
   */
  public void setPingIcon(ImageView img)
  {
    this.pingIcon = img;
  }


  /**
   * Caller method.
   * @return null (ignored)
   */
  @Override
  protected Void call()
  {
    logger.log("INFO", "SERVIÇO: Iniciando o Serviço PingService...");
    if (serviceName.equals("LoginScreen"))
    {
      isServiceNeeded = Validate.isPingServiceOnLoginRunning;
    }
    else if (serviceName.equals("MainWindow"))
    {
      isServiceNeeded = Validate.isPingServiceOnMainWindowRunning;
    }
    Thread thread = new Thread(new PingMonitor());
    thread.setDaemon(true);
    thread.start();
    return null;
  }

  /**
   * Ping Monitoring Class.
   */
  public class PingMonitor extends Thread
  {
    /**
     * Main method.
     */
    @Override
    public void run()
    {
      Connections con = new Connections();
      String pingURL = SessionDTO.getPingURL();
      SystemDTO.parseSystemAssets();
      Image green = new Image(Client.class.getResourceAsStream("green1.png"));
      Image yellow = new Image(Client.class.getResourceAsStream("yellow1.png"));
      Image red = new Image(Client.class.getResourceAsStream("red1.png"));
      Image gray = new Image (Client.class.getResourceAsStream("gray1.png"));
      long getSpeed = Long.parseLong(SessionDTO.getNominalUploadSpeed());
      long getMinimumSpeed = 1;
      if (!SessionDTO.isOfflineModeOn())
      {
        getMinimumSpeed = Long.parseLong(SystemDTO.getMinimumMbpsUploadSpeed());
      }
      while (isServiceNeeded)
      {
        if (serviceName.equals("LoginScreen"))
        {
          isServiceNeeded = Validate.isPingServiceOnLoginRunning;
        }
        else if (serviceName.equals("MainWindow"))
        {
          isServiceNeeded = Validate.isPingServiceOnMainWindowRunning;
        }
        if (SessionDTO.isConnectionTestDisabled())
        {
          Platform.runLater(()->
          {
            getPingIcon().setImage(gray);
            getPingLabelTooltip().setTooltip(loginUI.tooltipBuilder("Testes de rede desativados"));
            getPingLabel().setText("Ping Off");
          });
        }
        else if (!SessionDTO.isOfflineModeOn())
        {
          long ping = (Long.parseLong(con.getPing(pingURL)) + (700 /(getSpeed - getMinimumSpeed)));

          // Use line below to test the service
//          System.out.println("ping: " + ping + "ms");

          Platform.runLater(()-> { getPingLabel().setText(ping + "ms"); });
          if (ping < 121)
          {
            Platform.runLater(()-> { getPingIcon().setImage(green); });
          }
          else if (ping < 181)
          {
            Platform.runLater(()-> { getPingIcon().setImage(yellow); });
          }
          else
          {
            Platform.runLater(()-> { getPingIcon().setImage(red); });
          }
        }
        else
        {
          Platform.runLater(()->
          {
            getPingIcon().setImage(gray);
            getPingLabelTooltip().setTooltip(loginUI.tooltipBuilder("Modo Offline ativado!"));
            getPingLabel().setText("Modo Offline");
          });
        }
        try
        {
          Thread.sleep(1500);
        }
        catch (InterruptedException ex)
        {
          alerts.exceptionHandler(ex, "EXCEÇÃO EM LoginScreenController.initialize().PingMonitor().run()");
        }
      }
      logger.log("INFO", "SERVIÇO: Serviço PingService terminado.");
    }
  }

}
