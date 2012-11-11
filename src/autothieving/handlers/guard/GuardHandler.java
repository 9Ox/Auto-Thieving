package autothieving.handlers.guard;

import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.SceneObject;

public class GuardHandler extends Node {
	public static Tile currentTile;
	
	public static SceneObject currentObject;
	
	private final int[] GUARD_IDS = {5919,5920,5921}; 
	
	private int foodId, gloveId;
	
	private String location;
	
	private boolean fast;
	
	private final Area house = new Area(
						new Tile(3202, 3382, 0), 
						new Tile(3202, 3377, 0), 
						new Tile(3206, 3377, 0), 
						new Tile(3206, 3382, 0),
						new Tile(3202, 3382, 0));
	
	public GuardHandler(int foodId, boolean fast, String location) {
		this.foodId = foodId;
		this.fast = fast;
		this.location = location;
		gloveId = 10075;
	}
	
	@Override
	public boolean activate() {
		return Inventory.contains(new int[]{foodId});
	}
	
	/**
	 * Gets the local players HP in real time
	 * @return The local players HP
	 */
	public static double getHpPercent() {
		int realLevel = Skills.getRealLevel(Skills.CONSTITUTION);
		double lifePoints = realLevel * 10;
		double hp = Integer.parseInt(Widgets.get(748, 8).getText());
		double percent = hp/lifePoints;
		return percent * 100.0;
	}
	
	private boolean checkGlove() {
		for (int i : Players.getLocal().getAppearance()) {
			if (i == gloveId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void execute() {
		NPC guard = NPCs.getNearest(GUARD_IDS);
		SceneObject door = SceneEntities.getNearest(15536);
		if (getHpPercent() < 50 && Inventory.getItem(foodId) != null) {
			Inventory.getItem(foodId).getWidgetChild().click(true);
			currentTile = null;
			currentObject = null;
		}
		switch (location) {
		case "Varrock":
			if (!checkGlove() && Inventory.getItem(gloveId) != null) {
				Inventory.getItem(gloveId).getWidgetChild().click(true);
			}
			break;
		case "Ardougne":
			break;
		case "Falador":
			break;
		default:
			break;
		}
		if (checkGlove()) {
			if (house.contains(guard)) {
				if (door != null && door.getLocation().getX() < 3207) {
					if (door.isOnScreen()) {
						if (!Players.getLocal().isMoving()) {
							currentObject = door;
							door.interact("Open");
							Camera.setPitch(100);
							Task.sleep(1500,2001);
						}
					} else {
						Camera.setPitch(50);
						Camera.turnTo(door);
					}
				}
				if (guard != null) {
					currentTile = guard.getLocation();
					if (Players.getLocal().getAnimation() != 424) {
						if (fast) {
							Mouse.hop(Random.nextInt(guard.getCentralPoint().x - 3, guard.getCentralPoint().x + 3), 
									  Random.nextInt(guard.getCentralPoint().y - 3, guard.getCentralPoint().y + 3));
							Menu.select("Pickpocket", guard.getName());
						} else {
							currentTile = guard.getLocation();
							guard.interact("Pickpocket", guard.getName());
						}
					} else {
						currentTile = null;
						Task.sleep(2000);
					}
					if (Camera.getPitch() < 90) {
						currentTile = null;
						Camera.setPitch(100);
					}
				}
			} else {
				if (guard != null) {
					if (guard.isOnScreen()) {
						currentTile = guard.getLocation();
						if (Players.getLocal().getAnimation() != 424) {
							if (fast) {
								Mouse.hop(Random.nextInt(guard.getCentralPoint().x - 3, guard.getCentralPoint().x + 3), 
										  Random.nextInt(guard.getCentralPoint().y - 3, guard.getCentralPoint().y + 3));
								Menu.select("Pickpocket", guard.getName());
							} else {
								guard.interact("Pickpocket", guard.getName());
							}
						} else {
							currentTile = null;
							Task.sleep(2000);
						}
						if (Camera.getPitch() < 90) {
							currentTile = null;
							Camera.setPitch(100);
						}
					} else {
						currentTile = null;
						Camera.setPitch(50);
						Camera.turnTo(guard);
					}
				}
			}
		} else {
			if (Inventory.getItem(gloveId) != null) {
				Inventory.getItem(gloveId).getWidgetChild().click(true);
			}
		}
	}
}
