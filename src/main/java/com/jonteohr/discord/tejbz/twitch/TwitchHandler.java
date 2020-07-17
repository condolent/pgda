package com.jonteohr.discord.tejbz.twitch;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.SubscriptionEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.CreateClipList;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import com.jonteohr.discord.tejbz.App;
import com.jonteohr.discord.tejbz.CommandSQL;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class TwitchHandler {
	
	@EventSubscriber
	public void onTwitchChat(IRCMessageEvent e) {
		TwitchChat chat = Twitch.twitchClient.getChat();
		
		if(!e.getChannel().getName().equalsIgnoreCase("tejbz"))
			return;
		
		if(!e.getMessage().isPresent() || e.getUser() == null)
			return;
		
		// Increment to counter for automessaging
		AutoMessage.count++;
		
		String[] args = e.getMessage().get().split("\\s+");
		CommandSQL sql = new CommandSQL();
		
		String user = e.getTags().get("display-name");
		
		if(args[0].equalsIgnoreCase("!clip")) {
			CreateClipList clipData = Twitch.twitchClient.getHelix().createClip(Twitch.OAuth2.getAccessToken(), "25622462", false).execute();
			chat.sendMessage("tejbz", "@" + user + " https://clips.twitch.tv/" + clipData.getData().get(0).getId());
			return;
		}
		
		if(Twitch.commands.containsKey(args[0])) {
			String reply = Twitch.commands.get(args[0])
					.replace("[@user]", "@" + user)
					.replace("[user]", user);
			
			if(reply.contains("[subcount]"))
				reply = reply.replace("[subcount]", Twitch.getSubscribers("tejbz") + "");
			if(reply.contains("[follows]"))
				reply = reply.replace("[follows]", "" + Twitch.getFollowers("tejbz"));
			
			chat.sendMessage("tejbz", reply);
			return;
		}
		
		if(e.getTags().containsKey("badges")) {
			if(e.getTags().get("badges").contains("broadcaster") || e.getTags().get("badges").contains("moderator")) {
				if(args[0].equalsIgnoreCase("!addcmd")) {
					if(args.length < 3) {
						chat.sendMessage("tejbz", "@" + user + " Not enough arguments.. Correct usage: !addcmd <!commandName> <reply>");
						return;
					}
					String cmdName = args[1];
					String msg = "";
					for(int i = 2; i < args.length; i++) {
						msg = msg + " " + args[i];
					}
					
					if(sql.getCommands().contains(cmdName)) {
						chat.sendMessage("tejbz", "@" + user + " The command " + cmdName + " already exists. Did you mean to use !editcmd maybe?");
						return;
					}
					
					if(sql.addCommand(cmdName, msg)) {
						chat.sendMessage("tejbz", "@" + user + " Command " + cmdName + " stored!");
						Twitch.commands.put(cmdName, msg);
					}
					
					return;
				}
				
				if(args[0].equalsIgnoreCase("!editcmd")) {
					if(args.length < 3) {
						chat.sendMessage("tejbz", "@" + user + " Not enough arguments.. Correct usage: !addcmd <!commandName> <reply>");
						return;
					}
					
					String cmdName = args[1];
					String msg = "";
					for(int i = 2; i < args.length; i++) {
						msg = msg + " " + args[i];
					}
					
					if(!sql.getCommands().contains(cmdName)) {
						chat.sendMessage("tejbz", "@" + user + " There is no command named " + cmdName);
						return;
					}
					
					if(sql.editCommand(cmdName, msg)) {
						chat.sendMessage("tejbz", "@" + user + " Command " + cmdName + " stored!");
						Twitch.commands.replace(cmdName, msg);
					} else {
						chat.sendMessage("tejbz", "@" + user + " Failed editing the command " + cmdName);
					}
					
					return;
				}
				
				if(args[0].equalsIgnoreCase("!delcmd")) {
					if(args.length < 2) {
						chat.sendMessage("tejbz", "@" + user + " No command specified. Correct usage: !delcmd <!commandName>");
						return;
					}
					
					if(sql.deleteCommand(args[1])) {
						chat.sendMessage("tejbz", "@" + user + " Command " + args[1] + " successfully deleted!");
						Twitch.commands.remove(args[1]);
					}
					
					return;
				}
				
				if(args[0].equalsIgnoreCase("!format")) {
					chat.sendMessage("tejbz", "@" + user + " Formatting rules are in the discord: https://discordapp.com/channels/124204242683559938/489590000556441603/732630257861132438");
					return;
				}
			}
		}
	}
	
	@EventSubscriber
	public void onLive(ChannelGoLiveEvent e) {
		System.out.println(e.getStream().getUserName() + " went live.");
		
		EmbedBuilder msg = new EmbedBuilder();
		msg.setAuthor("Tejbz", null, Twitch.getUser(e.getChannel().getName()).getProfileImageUrl());
		msg.setColor(App.color);
		msg.setImage(Twitch.getStream("tejbz").getThumbnailUrl(1280, 720));
		msg.setTitle("Tejbz just went live!", "https://www.twitch.tv/tejbz");
		msg.addField("Title", e.getStream().getTitle(), false);
		msg.addField("Playing", Twitch.getGameById(e.getStream().getGameId()), false);
		
		App.general.sendMessage(msg.build()).queue();
	}
	
	@EventSubscriber
	public void onChatSub(SubscriptionEvent e) {
		String user = e.getUser().getName();
		int months = e.getMonths();
		int streak = e.getSubStreak();
		boolean gifted = e.getGifted();
		String message = (e.getMessage().isPresent() ? e.getMessage().get() : "No message..");
		String tier = (e.getSubscriptionPlan().contains("Prime") ? "Twitch Prime" : "A Tier " + e.getSubscriptionPlan().replace("0", "") + " sub");
		
		/*
		 * Twitch Chat
		 */
		if(Twitch.twitchClient.getChat().isChannelJoined("tejbz"))
			Twitch.twitchClient.getChat().sendMessage("tejbz", "/me tejbzWave Welcome to the squad, @" + user + " tejbzLove");
		
		TextChannel channel = App.general;
		
		EmbedBuilder msg = new EmbedBuilder();
		msg.setColor(App.color);
		msg.setAuthor("PGDA", null, App.authorImage);
		
		if(gifted) {
			msg.setDescription("[" + e.getGiftedBy().getName() + "](https://twitch.tv/" + e.getGiftedBy().getName() + ") gifted a sub to [" + user + "](https://twitch.tv/" + user + ")");
		} else {
			msg.setDescription("[" + user + "](https://twitch.tv/" + user + ") subscribed with " + tier + ". They've been subscribed for " + months + " months.");
			if(!message.equalsIgnoreCase("No message.."))
				msg.addField("Message", message, true);
			if(streak > 0)
				msg.addField("Streak", streak + " months", true);
		}
		
		channel.sendMessage(msg.build()).queue();
	}
	
	@EventSubscriber
	public void onPubSub(ChannelSubscribeEvent e) {
		System.out.println("PubSub Subscription:");
		System.out.println(e.getData());
	}
	
}
