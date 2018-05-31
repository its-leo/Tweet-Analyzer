/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import tweetanalyzer.gui.GUI;
import tweetanalyzer.store.Database;
import tweetanalyzer.store.Tweet;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Lennard Hensel
 * @author Charles McGuinness
 * https://www.socialseer.com/twitter-programming-in-java-with-twitter4j/how-to-retrieve-more-than-100-tweets-with-the-twitter-api-and-twitter4j/
 */
public class TweetManager {

    private static final DateFormat df = new SimpleDateFormat("E. dd.MM.yy HH:mm:ss");

    private static final String CONSUMER_KEY = "WrDYoj9jQ7pstpeevfaFTOGFz";
    private static final String CONSUMER_SECRET = "nGywO56FQRRRsHS9lNIlpq41o57SMdZ8bOrjJR4TO3wCNUo6FO";

    private static final int TWEETS_PER_QUERY = 100;

    public static int MAX_QUERIES = 1;

    public static OAuth2Token getOAuth2Token() {
        OAuth2Token token = null;
        ConfigurationBuilder cb;

        cb = new ConfigurationBuilder();
        cb.setApplicationOnlyAuthEnabled(true);

        cb.setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_SECRET);

        try {
            token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
        } catch (Exception e) {
            System.out.println("Could not get OAuth2 token");
            e.printStackTrace();
        }

        return token;
    }

    public static Twitter getTwitter() {
        OAuth2Token token;
        token = getOAuth2Token();

        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setApplicationOnlyAuthEnabled(true);

        cb.setOAuthConsumerKey(CONSUMER_KEY);
        cb.setOAuthConsumerSecret(CONSUMER_SECRET);

        cb.setOAuth2TokenType(token.getTokenType());
        cb.setOAuth2AccessToken(token.getAccessToken());
        
        return new TwitterFactory(cb.build()).getInstance();

    }

    public static List<Tweet> search(String SEARCH_TERM, String lang, long sinceId, GUI gui) {
        JTable resultTable = gui.getResultTable();
        DefaultTableModel tableModel = (DefaultTableModel) resultTable.getModel();

        List<Tweet> tweets = new ArrayList<Tweet>();

        long maxID = -1;

        Twitter twitter = getTwitter();
 

        try {

            Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("search");
            RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");

            for (int queryNumber = 0; queryNumber < MAX_QUERIES; queryNumber++) {

                gui.setStatusLabel(searchTweetsRateLimit.getRemaining() + " Anfragen übrig");

                if (searchTweetsRateLimit.getRemaining() == 0) {
                    long seconds = searchTweetsRateLimit.getSecondsUntilReset() + 2;

                    for (int i = 0; i < seconds; i++) {
                        gui.setCountLabel("Warte " + (seconds - i) + " Sekunden");
                        Thread.sleep(1000l);
                    }

                    searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");
                    System.out.println(searchTweetsRateLimit);
                }

                Query q = new Query(SEARCH_TERM);
                q.setCount(TWEETS_PER_QUERY);
                q.resultType(Query.ResultType.recent);
                q.setLang(lang);
                if (sinceId != 0) {
                    q.setSinceId(sinceId);
                }

                if (maxID != -1) {
                    q.setMaxId(maxID - 1);
                }

                QueryResult r = twitter.search(q);

                if (r.getTweets().size() == 0) {
                    break;
                }

                for (Status s : r.getTweets()) {
                    if (maxID == -1 || s.getId() < maxID) {
                        maxID = s.getId();
                    }

//                    System.out.println(s.toString());
//                    System.out.printf("At %s, @%-20s said:  %s\n",
//                            s.getCreatedAt().toString(),
//                            s.getUser().getScreenName(),
//                            cleanText(s.getText()));
                    tableModel.insertRow(tweets.size(), new Object[]{df.format(s.getCreatedAt()), s.getUser().getScreenName(), "<html>" + s.getText() + "</html>"});

                    Tweet tweet = new Tweet();
                    tweet.setId(s.getId());
                    tweet.setDate(s.getCreatedAt());
                    tweet.setUser(s.getUser().getScreenName());
                    tweet.setText(s.getText());
                    if (s.getGeoLocation() != null) {
                        tweet.setLatitude(s.getGeoLocation().getLatitude());
                        tweet.setLongitude(s.getGeoLocation().getLongitude());
                    }
                    tweets.add(tweet);
                }
                resultTable.scrollRectToVisible(resultTable.getCellRect(tweets.size() - 1, 0, true));

                gui.setCountLabel(tweets.size() + " Tweets erhalten");
                searchTweetsRateLimit = r.getRateLimitStatus();
            }

        } catch (Exception e) {
            gui.openMessageDialog("<html>Fehlschlag!<br><i>" + e.getMessage() + "</i></html>");
        }
        return tweets;
    }

    public static String cleanText(String text) {
        text = text.replace("\n", "\\n");
        text = text.replace("\t", "\\t");

        return text;
    }
}
