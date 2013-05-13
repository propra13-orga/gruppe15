package dungeon.models;

import dungeon.models.events.Transform;

public class TeleporterTile extends Tile {
  private final Target target;

  public TeleporterTile (Target target) {
    super(false);

    this.target = target;
  }

  public Target getTarget () {
    return this.target;
  }

  public static class Target {
    private final String roomId;

    private final int x;

    private final int y;

    public Target (String roomId, int x, int y) {
      this.roomId = roomId;
      this.x = x;
      this.y = y;
    }

    public String getRoomId () {
      return this.roomId;
    }

    public int getX () {
      return this.x;
    }

    public int getY () {
      return this.y;
    }
  }

  public TeleporterTile apply (Transform transform) {
    return this;
  }
}