package dungeon.ui;

import dungeon.game.messages.DefeatEvent;
import dungeon.game.messages.WinEvent;
import dungeon.messages.LifecycleEvent;
import dungeon.messages.Message;
import dungeon.messages.MessageHandler;
import dungeon.ui.messages.MenuCommand;
import dungeon.ui.screens.Canvas;
import dungeon.ui.screens.DefeatScreen;
import dungeon.ui.screens.StartMenu;
import dungeon.ui.screens.WinScreen;

import javax.swing.*;
import java.awt.CardLayout;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Follows the flow of the game and activates the appropriate screen.
 *
 * For example a StartGame message is received. Then the UiManager will activate the Canvas. If later in the game a
 * WinEvent is received, the UiManager will switch to the WinScreen.
 */
public class UiManager extends JPanel implements MessageHandler {
  private static final String START_MENU = "START_MENU";

  private static final String CANVAS = "CANVAS";

  private static final String WIN_SCREEN = "WIN_SCREEN";

  private static final String DEFEAT_SCREEN = "DEFEAT_SCREEN";

  private final Map<String, JPanel> screens = new LinkedHashMap<>(4);

  private final CardLayout layout;

  public UiManager (Canvas canvas, StartMenu startMenu, WinScreen winScreen, DefeatScreen defeatScreen) {
    super(new CardLayout());

    this.layout = (CardLayout)this.getLayout();

    this.screens.put(CANVAS, canvas);
    this.screens.put(START_MENU, startMenu);
    this.screens.put(WIN_SCREEN, winScreen);
    this.screens.put(DEFEAT_SCREEN, defeatScreen);
  }

  @Override
  public void handleMessage (Message message) {
    if (message == LifecycleEvent.INITIALIZE) {
      this.addScreens();

      this.showScreen(START_MENU);
    } else if (message == MenuCommand.SHOW_MENU) {
      this.showScreen(START_MENU);
    } else if (message == MenuCommand.START_GAME) {
      this.showScreen(CANVAS);
    } else if (message instanceof WinEvent) {
      this.showScreen(WIN_SCREEN);
    } else if (message instanceof DefeatEvent) {
      this.showScreen(DEFEAT_SCREEN);
    }
  }

  /**
   * Adds all registered screens to the layout.
   */
  private void addScreens () {
    for (Map.Entry<String, JPanel> entry : this.screens.entrySet()) {
      this.add(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Brings up the requested screen and gives them focus.
   */
  private void showScreen (String id) {
    this.layout.show(this, id);

    if (this.screens.containsKey(id)) {
      this.screens.get(id).requestFocus();
    }
  }
}