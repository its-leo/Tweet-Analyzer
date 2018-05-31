package tweetanalyzer.store;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import org.json.simple.parser.ParseException;
import static tweetanalyzer.Main.DEBUG;
import tweetanalyzer.search.LocationManager;

public class Location {

    private String name;

    private double latitude, longitude;

    // country, continent, locality, street
    private String type;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String name) {
        this.name = name;
    }

    @XmlElement(name = "latitude")
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    @XmlElement(name = "longitude")
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @XmlElement(name = "type")
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @XmlElement(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isComplete() {
        if (this.longitude != 0 && this.latitude != 0 && this.name != "") {
            return true;
        }
        return false;
    }

    public void searchLocation() {
        try {
            LocationManager.search(this);
        } catch (ParseException ex) {
            Logger.getLogger(Location.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
