package autothieving.loop;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.powerbot.core.event.events.MessageEvent;
import org.powerbot.core.event.listeners.MessageListener;
import org.powerbot.core.event.listeners.PaintListener;
import org.powerbot.core.script.ActiveScript;
import org.powerbot.core.script.job.LoopTask;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.core.script.job.state.Tree;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Tile;

import autothieving.handlers.guard.*;

@Manifest(name = "Auto Thieving",
		  description = "Automates the Thieving skill",
		  version = 0.01,
		  authors = "Noox, Jdog653")
public class AutoThieving extends ActiveScript implements MessageListener, PaintListener {
	
	private int profit, xpGained, startXp;
	
	private boolean fast, start;
	
	private long startTime;
	
	private final Node[] nodes = {new GuardHandler(385, true, "Varrock")};
	private final Tree scriptTree = new Tree(nodes);
	
	private final Image mouse = getImage("http://www.rw-designer.com/cursor-view/17047.png");
	
	@Override
	public void onStart() {
		profit = 0;
		fast = true;
		startTime = System.currentTimeMillis();
		startXp = Skills.getExperience(Skills.THIEVING);
		getContainer().submit(new LoopTask() {
			@Override
			public int loop() {
				xpGained = Skills.getExperience(Skills.THIEVING) - startXp;
				return 50;
			}
		});
		start = true;
	}
	
	@Override
	public int loop() {
		if (start) {
			final Node stateNode = scriptTree.state();
	        if (stateNode != null) {
	            scriptTree.set(stateNode);
	            final Node setNode = scriptTree.get();
	            if (setNode != null) {
	                getContainer().submit(setNode);
	                setNode.join();
	            }
	        }
		}
		return Random.nextInt(10, 20);
	}

	@Override
	public void messageReceived(MessageEvent e) {
		String s = e.getMessage().toString();
		if (s.contains("coins have been added to your money pouch.")) {
			int gained = Integer.parseInt(s.substring(0, s.indexOf(" ")));
			profit += gained;
		}
	}
	
	public static long getPerHour(final int value, final long startTime) {
		return (long)(value * 3600000D / (System.currentTimeMillis() - startTime));
    }

	@Override
	public void onRepaint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		//375,300
		g.setColor(Color.BLACK);
		g.drawRect(375, 300, 140, 88); //516
		g.setColor(new Color(0, 0, 0, 160));
		g.fillRect(375, 300, 140, 88);
		g.setColor(new Color(238, 238, 224, 175));
		g.drawString("Auto Thieving", 407, 315);
		//380,328
		g.drawString("Profit: " + profit + " (" + getPerHour(profit, startTime) + ")", 378, 328);
		g.drawString("Exp: " + xpGained + " (" + getPerHour(xpGained, startTime) + ")", 378, 340);
		g.drawString("Method: Guards [Varrock]", 378, 352);
		if (GuardHandler.currentTile != null) {
			drawTile(GuardHandler.currentTile, Color.BLACK, new Color(0,0,0,150), g);
		}
		drawMouse(g);
	}
	
	private void drawTile(Tile n, Color c, Color c2, Graphics g) {
		g.setColor(c);
		if (n != null) {
			for (Polygon p : n.getBounds()) {
		    	g.drawPolygon(p);
		    	g.setColor(c2);
		    	g.fillPolygon(p);
			}
		}
	}
	
	public void drawMouse(Graphics g) {
		int mouseY = (int) Mouse.getLocation().getY();
		int mouseX = (int) Mouse.getLocation().getX();
		g.drawImage(mouse, mouseX - 8, mouseY - 8, null);
	}
	
	public Image getImage(String url) {
	    Image im = null;
	    int i = 0;
	    
	    while(im == null && i < 50) {
			try {
				im = ImageIO.read(new URL(url));
			} 
			catch (IOException e) {
				System.out.println("Try #" + (i + 1));
			}
			i++;
	    }
	    return im;
	}
}
