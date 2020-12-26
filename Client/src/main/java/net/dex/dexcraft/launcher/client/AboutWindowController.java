package net.dex.dexcraft.launcher.client;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import net.dex.dexcraft.commons.dto.VersionsDTO;


/**
 * FXML Controller class
 *
 */
public class AboutWindowController implements Initializable
{

  @FXML
  private Label aboutWindowVersionLabel;

  @FXML
  private ComboBox<String> aboutWindowComboBox;

  @FXML
  private Label aboutWindowLicenseLabel;

  /**
   * Opens browser to show URL
   * @param event the mouse event.
   */
  @FXML
  void openLicenseURL(MouseEvent event)
  {
    try
    {
      new ProcessBuilder("cmd", "/c", "start " + "https://www.gnu.org/licenses/gpl-3.0-standalone.html").start();
    }
    catch (IOException ex)
    {
      //IGNORED
//      alerts.exceptionHandler(ex, "EXCEÇÃO em AboutWindowController.openLicenseURL(MouseEvent)");
    }
  }


  /**
   * Initializes the controller class.
   * @param url ignored
   * @param rb  ignored
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    //Load versions
    VersionsDTO.parseVersions();

    // Load server options to connect and observables
    List<String> packageList =  new ArrayList<>();
    packageList.add("DexCraft Launcher Init");
    packageList.add("DexCraft Launcher");
    packageList.add("DexCraft Background Services");
    packageList.add("DexCraft Factions Patch");
    packageList.add("DexCraft Pixelmon Patch");
    packageList.add("DexCraft Vanilla Patch");
    packageList.add("DexCraft Beta Patch");
    aboutWindowComboBox.setId("about-combo-box");
    aboutWindowComboBox.getItems().addAll(packageList);
    aboutWindowComboBox.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) ->
    {
      switch (new_value.intValue())
      {
        case 0:
          aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftLauncherInitVersion());
          break;
        case 1:
          aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftLauncherClientVersion());
          break;
        case 2:
          aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftBackgroundServicesVersion());
          break;
        case 3:
          aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftFactionsPatchVersion());
          break;
        case 4:
          aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftPixelmonPatchVersion());
          break;
        case 5:
          aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftVanillaPatchVersion());
          break;
        case 6:
          aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftBetaPatchVersion());
          break;
        default:
          aboutWindowVersionLabel.setText("");
          break;
      }
    });
    aboutWindowComboBox.getSelectionModel().select(0);
    aboutWindowVersionLabel.setText(VersionsDTO.getDexCraftLauncherInitVersion());
  }

}
