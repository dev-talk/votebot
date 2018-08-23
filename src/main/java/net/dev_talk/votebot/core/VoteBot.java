package net.dev_talk.votebot.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dev_talk.votebot.data.settings.Settings;
import net.dev_talk.votebot.data.vote.VoteDataHandler;
import net.dev_talk.votebot.handler.VoteHandler;
import net.dev_talk.votebot.util.MessageUtil;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoteBot implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(VoteBot.class);

    private static final String DEFAULT_RESOURCES_FOLDER = "defaults";
    private static final String SETTINGS_FILE_NAME = "/settings.json";
    private static final String MESSAGES_FILE_NAME = "/messages.properties";

    private static final String DATA_FOLDER_NAME = "/data";
    private static final String VOTE_DATA_FILE_NAME = "/data.json";

    private boolean initialized = false;
    private JDA discordApi = null;
    private ExecutorService executorService = null;
    private VoteDataHandler voteDataHandler = null;

    /**
     * Initializes the bot. This method must be executed to use this bot and can only be executed once!
     *
     * @throws Exception Exception thrown while initializing
     */
    public void initialize() throws Exception {
        if (initialized) {
            throw new IllegalStateException("Bot already initialized!");
        }
        initialized = true;

        final long startTimeMillis = System.currentTimeMillis();

        final String dataDir = System.getProperty("user.dir").concat(DATA_FOLDER_NAME);
        new File(dataDir).mkdirs();

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        final Settings settings;

        try (final InputStream inputStream =
                     getClass().getClassLoader()
                             .getResourceAsStream(DEFAULT_RESOURCES_FOLDER.concat(SETTINGS_FILE_NAME));
             final Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            final Settings defaultValues = gson.fromJson(reader, Settings.class);
            settings = new Settings(defaultValues); //get settings from env-vars, use default values from resources
        }

        final Settings.Discord discordSettings = settings.getDiscordSettings();

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setEnableShutdownHook(false); //shutting down jda is already done by our shutdown hook
        jdaBuilder.setToken(discordSettings.getToken());
        final String activity = discordSettings.getActivity();
        if (activity != null && !activity.isEmpty()) {
            final String activityType = discordSettings.getActivityType();
            Game.GameType gameType = Game.GameType.DEFAULT;
            if (activityType != null && !activityType.isEmpty()) {
                try {
                    gameType = Game.GameType.valueOf(activityType.toUpperCase());
                } catch (final IllegalArgumentException exc) {
                    logger.error("Can't parse activity type! Using default activity type!", exc);
                }
            }
            jdaBuilder.setGame(Game.of(gameType, activity));
        }
        discordApi = jdaBuilder.build().awaitReady();

        final EmbedBuilder embedBuilder = new EmbedBuilder();
        final String embedColor = discordSettings.getEmbedColor();
        if (embedColor != null && !embedColor.isEmpty()) {
            try {
                embedBuilder.setColor(Color.decode(settings.getDiscordSettings().getEmbedColor()));
            } catch (final NumberFormatException exc) {
                logger.error("Can't parse embed color! Using default color!", exc);
            }
        }

        final MessageUtil messageUtil = new MessageUtil(new File(dataDir.concat(MESSAGES_FILE_NAME)),
                DEFAULT_RESOURCES_FOLDER.concat(MESSAGES_FILE_NAME), embedBuilder);

        executorService = Executors.newCachedThreadPool();

        voteDataHandler = new VoteDataHandler(gson, new File(dataDir.concat(VOTE_DATA_FILE_NAME)), executorService);
        final Settings.Vote voteSettings = settings.getVoteSettings();
        discordApi.addEventListener(new VoteHandler(this, messageUtil, voteDataHandler,
                voteSettings.getChannelName(), voteSettings.getSilentIdentifier(), voteSettings.getYesEmoji(),
                voteSettings.getNoEmoji(), voteSettings.getEvaluateEmoji(), voteSettings.getDeleteEmoji()));

        logger.info(String.format("Startup complete! Took %dms.", System.currentTimeMillis() - startTimeMillis));
    }

    public final JDA getDiscordApi() {
        return discordApi;
    }

    public final ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void close() throws Exception {
        if (discordApi != null) {
            discordApi.shutdownNow();
        }
        if (voteDataHandler != null) {
            voteDataHandler.writeChanges();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
