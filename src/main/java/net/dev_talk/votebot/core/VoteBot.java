package net.dev_talk.votebot.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dev_talk.votebot.data.settings.Settings;
import net.dev_talk.votebot.util.ResourceUtil;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * @author LGA1151 (https://github.com/LGA1151)
 */
public class VoteBot implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(VoteBot.class);

    private static final String DEFAULT_RESOURCES_FOLDER = "defaults";
    private static final String SETTINGS_FILE_NAME = "/settings.json";

    private boolean initialized = false;
    private Settings settings = null;
    private JDA discordApi = null;

    /**
     * Initializes the bot.
     * This method must be executed to use this bot and can only be executed once!
     *
     * @throws Exception Exception thrown while initializing
     */
    public void initialize() throws Exception {
        if (initialized) {
            throw new IllegalStateException("Bot already initialized!");
        }
        initialized = true;

        final long startTimeMillis = System.currentTimeMillis();

        final String workingDir = System.getProperty("user.dir");

        final File settingsFile = new File(workingDir.concat(SETTINGS_FILE_NAME));
        if (ResourceUtil.saveDefaultResource(getClass().getClassLoader(),
                DEFAULT_RESOURCES_FOLDER.concat(SETTINGS_FILE_NAME), settingsFile)) {
            logger.info("Default settings.json saved");
            logger.info("Make sure to insert your discord-bot-token in the settings!");
        }

        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        final Gson gson = gsonBuilder.create();

        try (final Reader reader = new BufferedReader(new FileReader(settingsFile))) {
            settings = gson.fromJson(reader, Settings.class);
        }

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setEnableShutdownHook(false);
        final Settings.Discord discordSettings = settings.getDiscordSettings();
        jdaBuilder.setToken(discordSettings == null ? null : discordSettings.getToken());
        final String activity = discordSettings == null ? null : discordSettings.getActivity();
        if (activity != null && !activity.isEmpty()) {
            final String activityType = discordSettings.getActivityType();
            final Game.GameType gameType = activityType == null ? Game.GameType.DEFAULT :
                    Game.GameType.valueOf(activityType.toUpperCase());
            jdaBuilder.setGame(Game.of(gameType, activity));
        }
        discordApi = jdaBuilder.buildBlocking();

        logger.info(String.format("Startup complete! Took %dms.", System.currentTimeMillis() - startTimeMillis));
    }

    @Override
    public void close() throws Exception {
        if (discordApi != null) {
            discordApi.shutdownNow();
        }
    }
}
