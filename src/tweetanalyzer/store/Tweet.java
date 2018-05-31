/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.store;

import java.util.ArrayList;
import java.util.Date;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import tweetanalyzer.analyze.NLP;
import tweetanalyzer.analyze.PreProcess;

/**
 *
 * @author HENSEL
 */
public class Tweet {

    
    //BASISDATEN
    String text;
    String user;
    Date date;
    long id;    

    double latitude, longitude;
    
    boolean analyzed = false;



    
    // ANALYSEDATEN (ERWEITERT)
    ArrayList<String> tags = new ArrayList<String>();
    int sentiment = 2;


    //ID 
    public long getId() {
        return id;
    }

    @XmlAttribute
    public void setId(long id) {
        this.id = id;
    }

    //TEXT
    public String getText() {
        return text;
    }

    @XmlElement
    public void setText(String text) {
        this.text = text;
    }

    public int getSentiment() {
        return sentiment;
    }

    @XmlElement
    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }

    public Date getDate() {
        return date;
    }

    @XmlElement
    public void setDate(Date date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    @XmlElement
    public void setUser(String user) {
        this.user = user;
    }

    //LOCATION DES TWEETS
    public double getLatitude() {
        return latitude;
    }

    @XmlElement
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @XmlElement
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    @XmlElement(name = "tag")
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }
    
        public boolean isAnalyzed() {
        return analyzed;
    }
    @XmlElement(name = "analyzed")
    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    //METHODE
    public void analyze() {
        //Vorverarbeitung
        PreProcess.process(this);   
        //Analyse
        NLP.analyze(this);
        
        analyzed = true;
    }  
    

}
