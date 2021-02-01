package com.github.condolent.tejbz.twitch;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.*;

public class Predictions {

	private static boolean predictionActive = false;
	private static List<String> data = new ArrayList<>();
	private static Map<String, Integer> options = new HashMap<>(); // options[answer, total coins]
	private static Map<String, Integer> entries = new HashMap<>();// entries[user, answer]

	@EventSubscriber
	public void onCommand(IRCMessageEvent e) {
		if(!e.getChannel().getName().equalsIgnoreCase("tejbz"))
			return;

		if(!e.getMessage().isPresent() || e.getUser() == null)
			return;

		// DEBUG
		if(!e.getTags().get("display-name").equalsIgnoreCase("rlhypr"))
			return;

		String[] args = e.getMessage().get().split("\\s+");
		String user = e.getTags().get("display-name");

		// Manage predictions
		if(args[0].equalsIgnoreCase("!prediction") && Twitch.isModerator(e.getTags())) {
			if(args[1].equalsIgnoreCase("start")) {
				if(predictionActive) {
					Twitch.chat(user + ", a prediction is already active. Close it first with !prediction end <result>");
					return;
				}

				StringBuilder settings = new StringBuilder();
				for(int i = 2; i < args.length; i++) {
					if(i == args.length - 1)
						settings.append(args[i]);
					else
						settings.append(args[i] + " ");
				}

				String[] settingsArr = settings.toString().split(",");
				data = Arrays.asList(settingsArr);

				// Check if we have all we need!
				if(args.length < 5 || data.size() < 3) {
					Twitch.chat(user + ", not enough arguments. Usage: !prediction start QUESTION, ANSWER1, ANSWER2");
					return;
				}

				predictionActive = true;
				announcePrediction();
			}
		}

		// Viewer predict
		if(args[0].equalsIgnoreCase("!predict")) {
			// TODO
		}
	}

	private void announcePrediction() {
		Twitch.chatMe("PREDICTION STARTED!");
		Twitch.chatMe(data.get(0) + " : " + data.get(1) + " OR " + data.get(2));
	}
}
