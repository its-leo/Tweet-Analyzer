/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import static tweetanalyzer.Main.DEBUG;

/**
 *
 * @author HENSEL
 */
@XmlRootElement
public class Database {

    //Threads
    ArrayList<Thread> threads = new ArrayList<Thread>();

    //--------------Basis
    List<Tweet> tweets = new ArrayList<Tweet>();

    String keyword;
    String lang;

    public Database() {

    }

    ArrayList<String> filters = new ArrayList<String>();

    public ArrayList<String> getFilters() {
        return filters;
    }

    @XmlElement(name = "filter")
    public void setFilters(ArrayList<String> filters) {
        this.filters = filters;
    }

    public void addFilter(String filter) {
        this.filters.add(filter);
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    @XmlElement(name = "tweet")
    public void setTweets(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    public String getKeyword() {
        return keyword;
    }

    @XmlElement(name = "keyword")
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLang() {
        return lang;
    }

    @XmlElement(name = "lang")
    public void setLang(String lang) {
        this.lang = lang;
    }

    //--------------After Analyzed
    int sentenceCount;
    int wordCount;
    int analyzedCount = 0;

    public int getAnalyzedCount() {
        return analyzedCount;
    }

    public void updateAnalyzedCount() {
        analyzedCount = 0;
        for (int i = 0; i < tweets.size(); i++) {
            if (tweets.get(i).isAnalyzed()) {
                analyzedCount++;
            }
        }
    }

    public void setAnalyzedCount(int analyzedCount) {
        this.analyzedCount = analyzedCount;
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    @XmlElement(name = "sentenceCount")
    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    @XmlElement(name = "wordCount")
    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    //--------------After Map Visualized
    HashMap<Location, Integer> locations = new HashMap<Location, Integer>();

    public HashMap<Location, Integer> getLocations() {
        return locations;
    }

    public HashMap<Location, Integer> getLocationsSorted() {
        return sortByValue(locations);
    }

    @XmlElement(name = "location")
    public void setLocations(HashMap<Location, Integer> locations) {
        this.locations = locations;
    }

    public int getAbsoluteLocationsCount() {
        int count = 0;
        for (Map.Entry<Location, Integer> location : locations.entrySet()) {
            count += location.getValue();
        }
        return count;
    }

    public void setLocations(Map<String, Integer> rawLocations) {
        HashMap<Location, Integer> locations = new HashMap<Location, Integer>();
        for (Map.Entry<String, Integer> rawLocation : rawLocations.entrySet()) {
            Location location = new Location(rawLocation.getKey());
            locations.put(location, rawLocation.getValue());
        }
        this.locations = locations;
    }

    public void completeLocations() {
        for (Map.Entry<Location, Integer> location : locations.entrySet()) {
            if (location.getValue() > 1 || !location.getKey().getName().matches("([0-9])")) {
                if (!location.getKey().isComplete()) {
                    Runnable runner = new Runnable() {
                        public void run() {
                            if (DEBUG) {
                                System.out.println("LOCATION: " + location.getKey().getName() + ", COUNT: " + location.getValue());
                            }
                            location.getKey().searchLocation();
                        }
                    };
                    Thread mainThread = new Thread(runner, "GeoCode \"" + location.getKey().getName() + "\"");
                    mainThread.start();
                    threads.add(mainThread);

                }
            }
        }
    }

    public boolean threadsAlive() {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public boolean isLocationsComplete() {
        boolean complete = true;

        for (Map.Entry<Location, Integer> location : locations.entrySet()) {
            if (complete = true) {
                if (!location.getKey().isComplete()) {
                    complete = false;
                }
            }
        }
        return complete;
    }

    public int getCompletedLocationsCount() {
        int complete = 0;

        for (Map.Entry<Location, Integer> location : locations.entrySet()) {
            if (location.getKey().isComplete()) {
                complete++;
            }
        }

        return complete;
    }

    public String getTitle(boolean html) {
        String text = keyword;
        for (int i = 0; i < filters.size(); i++) {
            if (html) {
                text += " &#x2192; ";
            } else {
                text += " → ";
            }
            text += filters.get(i);
        }
        return text;
    }

//---------------------------------------------------------------
    public void sortTweets() {
        Collections.sort(tweets, new Comparator<Tweet>() {
            public int compare(Tweet t1, Tweet t2) {
                return t2.getDate().compareTo(t1.getDate());
            }
        });
    }

    public void makeTweetsDistinct() {
        for (int i = 0; i < tweets.size(); i++) {
            for (int j = i + 1; j < tweets.size(); j++) {
                if (tweets.get(i).getId() == tweets.get(j).getId()) {
                    tweets.remove(j);
                }
            }
        }
    }

    public double getSentiment() {
        double sentiment = 0;
        for (int i = 0; i < tweets.size(); i++) {
            sentiment += tweets.get(i).getSentiment();
        }
        return sentiment /= tweets.size();
    }

    public ArrayList<String> getTags() {
        ArrayList<String> keywords = new ArrayList<String>();
        for (int i = 0; i < tweets.size(); i++) {
            Tweet tweet = (Tweet) tweets.get(i);
            for (int j = 0; j < tweet.getTags().size(); j++) {
                keywords.add(tweet.getTags().get(j));
            }
        }
        return keywords;
    }

    public List<String> getTagsDistinct() {
// add elements to al, including duplicates
        Set<String> hs = new HashSet<>();
        hs.addAll(this.getTags());
        List<String> al = new ArrayList<>();
        al.addAll(hs);
        return al;
    }

    public Map<String, Long> getTagsWeighted() {
        return this.getTags().stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
    }

    public LinkedHashMap<String, Long> getTopAnalyzedTagsSizeWeighted(int top) {
        Map<String, Long> hmap = this.getTags().stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        if (top > hmap.size()) {
            top = hmap.size();
        }

        List<Map.Entry<String, Long>> greatest = findGreatest(hmap, top);

        LinkedHashMap<String, Long> h = new LinkedHashMap<String, Long>();

        for (Map.Entry<String, Long> entry : greatest) {
            h.put(entry.getKey(), entry.getValue());

        }
        h = sortByValue(h);
        return h;
    }

    public LinkedHashMap<String, Long> getTopTweetsToFilteredTagsWeighted(LinkedHashMap<String, Long> topTagsWeighted) {
        LinkedHashMap<String, Long> h = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> tagWeighted : topTagsWeighted.entrySet()) {
            long c = 0;
            String kw1 = tagWeighted.getKey().toLowerCase();
            String[] kw2 = kw1.split(" ");
            String g = "(?s)^";
            for (int j = 0; j < kw2.length; j++) {
                g += "(?=.*?" + kw2[j] + ")";
            }
            for (int i = 0; i < tweets.size(); i++) {
                String text = tweets.get(i).getText().toLowerCase();
                if (kw1.length() > 2) {
                    boolean a = text.indexOf(kw1) != -1;
                    boolean b = text.matches(g);
                    if (a || b) {
                        c++;
                    }
                }
            }
            if (!h.containsKey(kw1)) {
                h.put(kw1, c);
            } else {
                h.put(kw1, h.get(kw1) + c);
            }
        }
        return h;
    }

    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>>
            findGreatest(Map<K, V> map, int n) {
        Comparator<? super Map.Entry<K, V>> comparator
                = new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1) {
                V v0 = e0.getValue();
                V v1 = e1.getValue();
                return v0.compareTo(v1);
            }
        };
        PriorityQueue<Map.Entry<K, V>> highest
                = new PriorityQueue<Map.Entry<K, V>>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n) {
                highest.poll();
            }
        }
        List<Map.Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }

    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
