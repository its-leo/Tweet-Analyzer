/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.analyze;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static tweetanalyzer.Main.DEBUG;
import tweetanalyzer.store.Tweet;

/**
 *
 * @author HENSEL
 */
public class PreProcess {

    public static void process(Tweet tweet) {
        String text = tweet.getText().replace("\n", " ").replace("\r", " ");

//        //Twitterer ausklammern
//        Matcher twitterer = Pattern.compile("@([A-Za-z0-9]+[A-Za-z0-9_]+)(?![A-Za-z0-9_]*\\\\.)").matcher(tweet);
//        while (twitterer.find()) {
//            tweet = tweet.replace("@" + twitterer.group(1), "\"" + twitterer.group(1) + "\"");
//        }
        if (DEBUG) {
            System.out.println("RawTweet1: " + text);
        }
        //URLS entfernen
        text = text.replaceAll("(htt)\\S+", "");
        if (DEBUG) {
            System.out.println("RawTweet2: " + text);
        }

        if (text.matches(".*(#|,|\\.|!|\\?|:|\\–|\\-|\\&|\\;|\\@|\\\").*")) {
            //(?!.*\\. |\\!|\\?)(.*?)(?=\\…) --> Alternative1
            //(?<=\\. |\\!|\\?)(.*?)(?=\\…|\\#) --> Alternative2
            text = text.replaceAll("(?=\\ [^#@,.!?:“\\–\\-\\;\\\"]+$)(.*?)(?=\\…)", " ");
        } else if (DEBUG) {
            System.out.println("Keine Anpassung des unvollständigen Satzes möglich.");
        }
        text = text.replace("…", "");
        text = text.trim();

        if (DEBUG) {
            System.out.println("RawTweet3: " + text);
        }
        //Doppelte Zeichen entfernen
        Matcher duplicateChars = Pattern.compile("[!|?|.]{2,}").matcher(text);
        while (duplicateChars.find()) {
            text = text.replace(duplicateChars.group(), duplicateChars.group().charAt(duplicateChars.group().length() - 1) + "");
        }

        text = text.replaceAll("RT\\s*@[^:]*..", "");
        text = text.replaceAll("(\\\"|\\:|\\,|\\–|\\;|\\-|\\#|\\b[\\w+]\\b)$", "");
        text = text.trim();
        text = text.replaceAll("([\\^])\\w+", "");
        text = text.replaceAll("[ ]{2,}", " ");
        text = text.replaceAll("[-]{2,}", "-");

        //Abkürzungen finden
        Matcher abbreviation = Pattern.compile("(?<![A-Z])\\#[A-Z0-9]{2,5}(?![A-Za-z0-9])").matcher(text);
        while (abbreviation.find()) {
            tweet.addTag(abbreviation.group().replaceFirst("#", "").toLowerCase());

            if (DEBUG) {
                System.out.println("Abkürzung \"" + abbreviation.group().replaceFirst("#", "") + "\" erkannt.");
            }
        }

        //      Alternativ     [#]{2,}\\S+
        text = text.replaceAll("[#]{1,}", "");
        text = text.replaceAll("(\\# )", "#");

        if (DEBUG) {
            System.out.println("RawTweet4: " + text);
        }
        text = text.trim();
        //Leerzeichen vor Interpunktionszeichen entfernen
        text = text.replace(" ?", "?");
        text = text.replace(" !", "!");
        text = text.replace(" .", ".");
        text = text.replace(" ,", ",");

        text = text.trim();

        if (DEBUG) {
            System.out.println("Processed: " + text);
        }
        tweet.setText(text);
    }

}
