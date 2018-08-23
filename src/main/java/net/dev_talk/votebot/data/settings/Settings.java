package net.dev_talk.votebot.data.settings;

public class Settings {
    public Settings() {
    }

    public Settings(final Settings defaultValues) {
        discord = new Discord();

        final String discordToken = System.getenv("DISCORD_TOKEN");
        discord.token = discordToken != null ? discordToken : defaultValues.discord.token;

        final String discordActivity = System.getenv("DISCORD_ACTIVITY");
        discord.activity = discordActivity != null ? discordActivity : defaultValues.discord.activity;

        final String discordActivityType = System.getenv("DISCORD_ACTIVITY_TYPE");
        discord.activityType = discordActivityType != null ? discordActivityType : defaultValues.discord.activityType;

        final String discordEmbedColor = System.getenv("DISCORD_EMBED_COLOR");
        discord.embedColor = discordEmbedColor != null ? discordEmbedColor : defaultValues.discord.embedColor;

        vote = new Vote();

        final String voteChannelName = System.getenv("VOTE_CHANNEL_NAME");
        vote.channelName = voteChannelName != null ? voteChannelName : defaultValues.vote.channelName;

        final String voteSilentIdentifier = System.getenv("VOTE_SILENT_IDENTIFIER");
        vote.silentIdentifier =
                voteSilentIdentifier != null ? voteSilentIdentifier : defaultValues.vote.silentIdentifier;

        final String voteYesEmoji = System.getenv("VOTE_YES_EMOJI");
        vote.yesEmoji = voteYesEmoji != null ? voteYesEmoji : defaultValues.vote.yesEmoji;

        final String voteNoEmoji = System.getenv("VOTE_NO_EMOJI");
        vote.noEmoji = voteNoEmoji != null ? voteNoEmoji : defaultValues.vote.noEmoji;

        final String voteEvaluateEmoji = System.getenv("VOTE_EVALUATE_EMOJI");
        vote.evaluateEmoji = voteEvaluateEmoji != null ? voteEvaluateEmoji : defaultValues.vote.evaluateEmoji;

        final String voteDeleteEmoji = System.getenv("VOTE_DELETE_EMOJI");
        vote.deleteEmoji = voteDeleteEmoji != null ? voteDeleteEmoji : defaultValues.vote.deleteEmoji;
    }

    private Discord discord;
    private Vote vote;

    public Discord getDiscordSettings() {
        return discord;
    }

    public Vote getVoteSettings() {
        return vote;
    }

    /**
     * configuration for interaction with discord
     */
    public class Discord {
        private String token;
        private String activity;
        private String activityType;
        private String embedColor;

        public String getToken() {
            return token;
        }

        public String getActivity() {
            return activity;
        }

        public String getActivityType() {
            return activityType;
        }

        public String getEmbedColor() {
            return embedColor;
        }
    }

    /**
     * configuration for the vote-system
     */
    public class Vote {
        private String channelName;
        private String silentIdentifier;
        private String yesEmoji;
        private String noEmoji;
        private String evaluateEmoji;
        private String deleteEmoji;

        public String getChannelName() {
            return channelName;
        }

        public String getSilentIdentifier() {
            return silentIdentifier;
        }

        public String getYesEmoji() {
            return yesEmoji;
        }

        public String getNoEmoji() {
            return noEmoji;
        }

        public String getEvaluateEmoji() {
            return evaluateEmoji;
        }

        public String getDeleteEmoji() {
            return deleteEmoji;
        }
    }
}
