package net.dex.dexcraft.launcher.client;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;


/**
 * FXML Controller class
 *
 */
public class AboutWindowController implements Initializable
{

  @FXML
  private Label aboutWindowInitVersion;

  @FXML
  private Label aboutWindowLauncherVersion;

  @FXML
  private Label aboutWindowDCBSVersion;

  @FXML
  private Label aboutWindowFactionsVersion;

  @FXML
  private Label aboutWindowPixelmonVersion;

  @FXML
  private Label aboutWindowVanillaVersion;

  @FXML
  private Label aboutWindowBetaVersion;


  private void loadVersions()
  {
    Validate.versions();
    aboutWindowInitVersion.setText(Validate.dexCraftLauncherInitVersion);
    aboutWindowLauncherVersion.setText(Validate.dexCraftLauncherClientVersion);
    aboutWindowDCBSVersion.setText(Validate.dexCraftBackgroundServicesVersion);
    aboutWindowFactionsVersion.setText(Validate.dexCraftFactionsPatchVersion);
    aboutWindowPixelmonVersion.setText(Validate.dexCraftPixelmonPatchVersion);
    aboutWindowVanillaVersion.setText(Validate.dexCraftVanillaPatchVersion);
    aboutWindowBetaVersion.setText(Validate.dexCraftBetaPatchVersion);
  }


  /**
   * Initializes the controller class.
   * @param url ignored
   * @param rb  ignored
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    loadVersions();
  }

}
