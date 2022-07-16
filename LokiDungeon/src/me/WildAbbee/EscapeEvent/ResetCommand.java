package me.WildAbbee.EscapeEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ResetCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("/lokireset <new goal amount>");
			return false;
		}

		int amount = -1;
		try {
			amount = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage(args[0] + " isn't an integer.");
		}

		if (amount < 1) {
			sender.sendMessage(args[0] + " must be greater than 0.");
			return false;
		}

		EscapeEvent.inst.amount = amount;
		EscapeEvent.inst.checkpoints.clear();
		EscapeEvent.inst.eggs.clear();
		
		Bukkit.broadcastMessage("Escape event reset by " + sender.getName() + " (goal: " + amount + ")");
		
		return true;
	}
}
