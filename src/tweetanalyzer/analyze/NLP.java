/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.analyze;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static tweetanalyzer.Main.DEBUG;
import tweetanalyzer.store.Tweet;

/**
 *
 * @author HENSEL
 */
public class NLP {

    static StanfordCoreNLP pipeline;
    static LexicalizedParser lp;

    static double sentimentSum;

    static int analyzedSentences;
    static int analyzedWords;

    static HashMap<String, Integer> rawLocationsMap;

    private static String lang;

    public static void init(String lang) {
        Properties props = new Properties();
        NLP.lang = lang;

        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        switch (lang) {
            case "de":
                props.put("parse.model", "edu/stanford/nlp/models/lexparser/germanFactored.ser.gz");
                props.put("ner.model", "edu/stanford/nlp/models/ner/german.conll.hgc_175m_600.crf.ser.gz");
                props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/german/german-hgc.tagger");
                props.put("parse.flags", "");
                break;
        }

        pipeline = new StanfordCoreNLP(props);
        lp = LexicalizedParser.loadModel();

        rawLocationsMap = new HashMap<String, Integer>();

    }

    public static void reset() {
        rawLocationsMap = new HashMap<String, Integer>();
        sentimentSum = 0;
        analyzedSentences = 0;
        analyzedWords = 0;

    }

    public static void analyze(Tweet tweet) {

        //Vorverarbeitung
        String text = tweet.getText();

        //Auswertung
        //NLP durchführen
        if (text != null && text.length() > 0) {

            Annotation annotation = pipeline.process(text);
            List<String> rawLocations = new ArrayList<String>();

            //für einen Satz
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

                //Damit z.B. die aufeinanderfolgenden Wörter South und Carolina als eine zusammenhängende Location verstanden wird
                boolean lastLocation = false;

                //NUR FÜR ENGLISCH WICHTIG! Damit z.B. power plant zusammenstehen und nicht power und plant zwei getrennte Keyword sind.
                boolean lastKeyword = false;

                // token ist ein Wort
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                    // text of the token
                    String word = token.word();

                    if (word.matches("[A-Za-z0-9&äöüÄÖUß-]{1,}")) {
                        // POS tag of the token
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                        // NE label of the token
                        String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                        if (DEBUG) {
                            System.out.println(word + " | POS: " + pos + " | NER: " + ner + " | Lemma: " + lemma);
                        }
                        if (lang.equals("de")) {
                            if (pos.equals("NN") && !ner.equals("I-PER") && !ner.equals("I-ORG") && !ner.equals("I-LOC")) {
                                tweet.addTag(lemma);
                                text = text.replaceAll(word, "<font color=green>" + word + "</font>");
                            }
                            if (ner.equals("I-ORG") && !pos.equals("KOUS") && !pos.equals("ART")) {
                                text = text.replaceAll(word, "<font color=purple>" + word + "</font>");
                            }
//                            if (ner.equals("I-PER") || word.startsWith("@")) {
////                                tweetText = tweetText.replaceAll(word, "<font color=778899>" + word + "</font>");
//                            }
                            if (ner.equals("I-LOC") && pos.equals("NE")) {
                                addLocation(rawLocations, word, lastLocation);
                                text = text.replaceAll(word, "<font color=orange>" + word + "</font>");
                                lastLocation = true;
                            } else {
                                lastLocation = false;
                            }

                        }
                        if (lang.equals("en")) {
                            if (pos.equals("NN") || pos.equals("NNS") || lemma.startsWith("#") && !ner.equals("PERSON") && !ner.equals("ORGANIZATION") && !ner.equals("LOCATION") && !ner.equals("MISC")) {
                                ArrayList<String> keywords = tweet.getTags();
                                if (lastKeyword) {
                                    String lastKeywordInList = keywords.get(keywords.size() - 1);
                                    if (lp.getLexicon().isKnown(lastKeywordInList) && lp.getLexicon().isKnown(word)) {
                                        tweet.getTags().set(keywords.size() - 1, lastKeywordInList + " " + lemma);
                                    } else {
                                        tweet.addTag(lemma);
                                    }
                                } else {
                                    tweet.addTag(lemma);
                                }

                                text = text.replaceAll(word, "<font color=green>" + word + "</font>");
                                lastKeyword = true;
                            } else {
                                lastKeyword = false;
                            }

                            if (ner.equals("ORGANIZATION")) {
                                text = text.replaceAll(word, "<font color=purple>" + word + "</font>");
                            }
//                            if (ner.equals("PERSON") || word.startsWith("@")) {
////                                tweetText = tweetText.replaceAll(word, "<font color=778899>" + word + "</font>");
//                            }
                            if (ner.equals("LOCATION")) {
                                addLocation(rawLocations, word, lastLocation);
                                text = text.replaceAll(word, "<font color=orange>" + word + "</font>");
                                lastLocation = true;
                            } else {
                                lastLocation = false;
                            }
                        }
                        tweet.setText("<html>" + text + "</html>");
                        analyzedWords++;
                    } else {
                        lastLocation = false;
                        lastKeyword = false;
                    }
                }

//                Tree tree1 = sentence.get(TreeAnnotation.class);
//                tree1.pennPrint();
                // Sentiment Detection
                Tree tree = sentence.get(SentimentAnnotatedTree.class);
                tweet.setSentiment(RNNCoreAnnotations.getPredictedClass(tree));

//                System.out.println("Sentiment: " + sentiment);
                analyzedSentences++;
                if (!rawLocations.isEmpty()) {
                    for (int i = 0; i < rawLocations.size(); i++) {
                        String rawLocation = rawLocations.get(i);
                        if (rawLocationsMap.get(rawLocation) == null) {
                            rawLocationsMap.put(rawLocation, 1);
                        } else {
                            rawLocationsMap.put(rawLocation, rawLocationsMap.get(rawLocation) + 1);
                        }

                    }
                }
                if (DEBUG) {
                    System.out.println("###");
                }
            }
        }
    }

    private static void addLocation(List<String> rawLocations, String word, boolean lastLocation) {
        if (lastLocation) {
            rawLocations.set(rawLocations.size() - 1, rawLocations.get(rawLocations.size() - 1) + " " + word);
        } else {
            rawLocations.add(word);
        }
    }

    public static HashMap<String, Integer> getRawLocationsMap() {
        return rawLocationsMap;
    }

    public static void setRawLocationsMap(HashMap<String, Integer> rawLocationsMap) {
        NLP.rawLocationsMap = rawLocationsMap;
    }

    public static int getAnalyzedSentenceCount() {
        return analyzedSentences;
    }

    public static int getAnalyzedWordCount() {
        return analyzedWords;
    }

    public static void setAnalyzedSentences(int analyzedSentences) {
        NLP.analyzedSentences = analyzedSentences;
    }

    public static void setAnalyzedWords(int analyzedWords) {
        NLP.analyzedWords = analyzedWords;
    }

    public static double getSentiment() {
        System.out.println("0 - negative, 1 - neutrale, 2 - positive Stimmung");
        return sentimentSum / analyzedSentences;
    }
}
