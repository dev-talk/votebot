package net.dev_talk.votebot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String[] args) throws Exception {
        final VoteBot bot = new VoteBot();
        try {
            bot.initialize();
        } catch (final Exception exc) {
            logger.error("Encountered Exception while initializing bot!", exc);
            bot.close();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                bot.close();
            } catch (final Exception exc) {
                logger.error("Encountered exception while shutting down bot!", exc);
            }
        }));
    }
}
