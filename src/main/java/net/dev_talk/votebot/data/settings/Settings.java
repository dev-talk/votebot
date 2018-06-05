package net.dev_talk.votebot.data.settings;

public class Settings {
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
