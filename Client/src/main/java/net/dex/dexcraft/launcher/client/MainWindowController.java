package net.dex.dexcraft.launcher.client;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;


/**
 * FXML Controller class
 */
public class MainWindowController implements Initializable
{

  @FXML
  private AnchorPane anchorpane;
  @FXML
  private ImageView bgimage;
  @FXML
  private Button buttondc;
  @FXML
  private Button buttondcpx;
  @FXML
  private MenuBar menubar1;
  @FXML
  private Menu menuarquivo;
  @FXML
  private CustomMenuItem custommenuitemdobkp;
  @FXML
  private Label labeldobkp;
  @FXML
  private CustomMenuItem customMenuItemLocalMap;
  @FXML
  private CheckBox checkboxLocalMap;
  @FXML
  private CustomMenuItem custommenuitemrestorebkp;
  @FXML
  private Label labelrestorebkp;
  @FXML
  private CustomMenuItem custommenuitemreinstall;
  @FXML
  private Label labelreinstall;
  @FXML
  private Menu menuextras;
  @FXML
  private Menu dexcraftFactionsSubmenu;
  @FXML
  private Menu submenusoundpacksdc;
  @FXML
  private CustomMenuItem custommenuitemsoundDCChocobov2;
  @FXML
  private Label labelsoundDCChocobov2;
  @FXML
  private CustomMenuItem custommenuitemsoundDCDCDeluxev2;
  @FXML
  private Label labelsoundDCDCDeluxev2;
  @FXML
  private Menu submenushadersdc;
  @FXML
  private CustomMenuItem custommenuitemshaderdc1;
  @FXML
  private Label labelshaderdc1;
  @FXML
  private Menu dexcraftPixelmonSubmenu;
  @FXML
  private Menu submenusoundpacksdcpx;
  @FXML
  private CustomMenuItem custommenuitemsoundDCPXChocobov2;
  @FXML
  private Label labelsoundDCPXChocobov2;
  @FXML
  private CustomMenuItem custommenuitemsoundDCPXDCDeluxev2;
  @FXML
  private Label labelsoundDCPXDCDeluxev2;
  @FXML
  private Menu submenushadersdcpx;
  @FXML
  private CustomMenuItem custommenuitemshaderdcpx1;
  @FXML
  private Label labelshaderdcpx1;
  @FXML
  private CustomMenuItem custommenuitemshaderdcpx2;
  @FXML
  private Label labelshaderdcpx2;
  @FXML
  private Menu menuavancado;
  @FXML
  private CustomMenuItem custommenuitemmincfg;
  @FXML
  private Label labelmincfg;
  @FXML
  private CustomMenuItem custommenuitemmaxcfg;
  @FXML
  private Label labelmaxcfg;
  @FXML
  private Menu submenuremovejvmargs;
  @FXML
  private CustomMenuItem custommenuitemresetdcargs;
  @FXML
  private Label labelresetdcargs;
  @FXML
  private CustomMenuItem custommenuitemresetdcpxargs;
  @FXML
  private Label labelresetdcpxargs;
  @FXML
  private CustomMenuItem custommenuitemdefaultcfg;
  @FXML
  private Label labeldefaultcfg;
  @FXML
  private CustomMenuItem customMenuItemOfflineMode;
  @FXML
  private CheckBox checkboxOfflineMode;
  @FXML
  private CustomMenuItem custommenuitemlanmode;
  @FXML
  private CheckBox checkboxlanmode;
  @FXML
  private Menu menuajuda;
  @FXML
  private CustomMenuItem custommenuitemdeletelog;
  @FXML
  private Label labeldeletelog;
  @FXML
  private CustomMenuItem custommenuitemsobre;
  @FXML
  private Label labelsobre;
  @FXML
  private Label labelinfo;
  @FXML
  private Label labelmaintitle;
  @FXML
  private Label labelbackup;
  @FXML
  private TextField textfielduser;
  @FXML
  private PasswordField passwordfieldpin;
  @FXML
  private ProgressBar progress1;
  @FXML
  private Label labelstatus;
  @FXML
  private ProgressBar progress2;
  @FXML
  private Label labelstatus2;
  @FXML
  private Label labelLANMode;

  public String easterURL = Validate.easterEggURL;

  /**
   * Initializes the controller class.
   * @param url ignored
   * @param rb ignored
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {
    // TODO
  }

  @FXML
  private void acaoButtonPlayGame(ActionEvent event)
  {
  }

  @FXML
  private void acaoCustomMenuItem(ActionEvent event)
  {
  }

  @FXML
  private void acaoLabelMainTitle(MouseEvent event)
  {
    try
    {
      new ProcessBuilder("cmd", "/c", "start " + easterURL).start();
    }
    catch (IOException ex)
    {
      //ERRO
    }
  }

}

