package atamayo.offlinereader.RedditAPI.RedditModel;

public class RedditTimeFormatter {
    public static String format(long utc){
        long seconds = (System.currentTimeMillis() - (utc * 1000)) / 1000;
        if(seconds < 60){
            if(seconds == 1)
                return "a second ago";

            return Long.toString(seconds) + " seconds ago";
        }

        long minutes = seconds / 60;
        if(minutes < 60){
            if(minutes == 1)
                return "a minute ago";

            return Long.toString(minutes) + " minutes ago";
        }

        long hours = minutes / 60;
        if(hours < 24){
            if(hours == 1)
                return "an hour ago";

            return Long.toString(hours) + " hours ago";
        }

        long days = hours / 24;
        if(days < 7){
            if(days == 1)
                return "a day ago";

            return Long.toString(days) + " days ago";
        }

        long weeks = days / 7;
        if(weeks < 4){
            if(weeks == 1)
                return "a week ago";

            return Long.toString(weeks) + " weeks ago";
        }

        long months = weeks / 4;
        if(months < 12){
            if(months == 1)
                return "a month ago";

            return Long.toString(months) + " months ago";
        }

        long years = months / 12;
        if(years == 1)
            return "a year ago";

        return Long.toString(years) + " years ago";
    }
}
