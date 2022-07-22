package me.WildAbbee.EscapeEvent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpdatePlayersCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		EscapeEvent.inst.hidePlayers();
		sender.sendMessage("Hidden players.");
		return true;
	}
}
