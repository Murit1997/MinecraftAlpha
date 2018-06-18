package mjs.minecraft.plugin.implementPlugin;
import java.util.logging.Logger; 
import org.bukkit.command.Command; 
import org.bukkit.command.CommandSender; 
import org.bukkit.entity.Player; 
import org.bukkit.plugin.PluginManager; 
import org.bukkit.plugin.java.JavaPlugin; 

public class implementAllPlugins extends JavaPlugin {
    
public static final Logger log = Logger.getLogger("Minecraft&quot");   

@Override 
public void onDisable() { 
PluginManager pluginManager = getServer().getPluginManager(); 
log.info("Hello plugin has been disabled.&quot"); 
} 

@Override 
public void onEnable() { 
log.info("Hello plugin has been enabled!&quot"); 
} 

@Override 
public boolean onCommand(CommandSender sender, Command command, 
String label, String[] args) { 
if(label.equals("hello&quot")) {Player player = (Player) sender; 
player.sendMessage("Hello " + player.getDisplayName()); 
return true; 
} 
return false; 
}
}
