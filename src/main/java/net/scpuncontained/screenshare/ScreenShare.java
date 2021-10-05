package net.scpuncontained.screenshare;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public final class ScreenShare extends JavaPlugin{
	
	Robot r;
	
	public int WIDTH = 128;
	public int HEIGHT = 72;
	
	boolean go = false;
	
	HashMap<Material, Color> colors = new HashMap<Material, Color>();
	
	public void onEnable() {
		
		for (File f : new File(getDataFolder().getPath() + "/block").listFiles()) {
			
			System.out.println(f.getName());
			
			if (f.getName().endsWith(".png")) {
				
				BufferedImage img = null;
				try {
					img = ImageIO.read(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				long redBucket = 0;
				long greenBucket = 0;
				long blueBucket = 0;
				long pixelCount = 0;

				for (int y = 0; y < img.getHeight(); y++)
				{
				    for (int x = 0; x < img.getWidth(); x++)
				    {
				        Color c = new Color(img.getRGB(x, y));
				        
				        if (!(c.getGreen() == 0 && c.getRed() == 0 && c.getBlue() == 0)) {
				        	
				        	pixelCount++;
				        	redBucket += c.getRed();
				        	greenBucket += c.getGreen();
				        	blueBucket += c.getBlue();				        	
				        	
				        }

				    }
				}
				
				if (pixelCount != 0) {
					
					Color averageColor = new Color((int)(redBucket / pixelCount),
                            (int)(greenBucket / pixelCount),
                            (int)(blueBucket / pixelCount));

					for (Material m : Material.values()) {

						if (f.getName().toLowerCase().contains(m.toString().toLowerCase()) && m.isSolid() && m.isOccluding() && !m.toString().toLowerCase().contains("shulker") &&
								!m.hasGravity() && !m.equals(Material.SPAWNER) && !m.toString().toLowerCase().contains("legacy") && !m.isInteractable()) {
							
							colors.put(m, averageColor);
	
						}

					}
					
				}
				
			}
			
		}
		
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				if (go) {
					
					Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		            BufferedImage screencap = r.createScreenCapture(rectangle);
		            BufferedImage smallcap = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		            Graphics2D g2d = smallcap.createGraphics();
		            g2d.drawImage(screencap.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH), 0, 0, null);
		            g2d.dispose();
		            
		            for (int x = 0; x < WIDTH; x++) {
		            	
		            	for (int y = 0; y < HEIGHT; y++) {
		            		
		            		Color c = new Color(smallcap.getRGB(x, y));
		            		
		            		Material m = getMaterial(c);
		            		
		            		Bukkit.getWorld("world").getBlockAt(new Location(Bukkit.getWorld("world"), x, 200, y)).setType(m);
		            		
		            	}
		            	
		            }
					
				} else {
					
		            for (int x = 0; x < WIDTH; x++) {
		            	
		            	for (int y = 0; y < HEIGHT; y++) {
		            		
		            		Material m = Material.BLACK_CONCRETE;
		            		
		            		Block b = Bukkit.getWorld("world").getBlockAt(new Location(Bukkit.getWorld("world"), x, 200, y));
		            		
		            		b.setType(m);
		            		b.getState().update(true);
		            		
		            	}
		            	
		            }
					
				}	            
				
			}
			
		}.runTaskTimer(this, 0, 2);
		
	}
	
	public Material getMaterial(Color in) {
		
        Material closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (Material m : colors.keySet()) {
        	Color c = colors.get(m);
            mse = computeMSE(in, c.getRed(), c.getGreen(), c.getBlue());
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = m;
            }
        }
        
        return closestMatch;
		
	}
	
    public int computeMSE(Color c, int pixR, int pixG, int pixB) {
        return (int) (((pixR - c.getRed()) * (pixR - c.getRed()) + (pixG - c.getGreen()) * (pixG - c.getGreen()) + (pixB - c.getBlue())
                * (pixB - c.getBlue())) / 3);
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("toggle")) {
			
			go = !go;
			
			if (go) {
				
				sender.sendMessage(ChatColor.GREEN + "Enabled!");
				
			} else {
				
				sender.sendMessage(ChatColor.RED + "Disabled!");
				
			}
			
			return true;
			
		}
		
		if (cmd.getName().equalsIgnoreCase("resolution") && args.length == 2) {
			
			try {
				
				Integer x = Integer.valueOf(args[0]);
				Integer y = Integer.valueOf(args[1]);
				
				WIDTH = x;
				HEIGHT = y;
				
				sender.sendMessage(ChatColor.GREEN + "Resolution set to " + args[0] + "x" + args[1] + "!");
				
			} catch (NumberFormatException e) {
				
				sender.sendMessage(ChatColor.RED + "Invalid number(s)!");
				
			}
			
			return true;
			
		}
		
		return false;
		
	}

}
