/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.visualize.HeatMap;

import tweetanalyzer.store.Location;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static tweetanalyzer.Main.DEBUG;

/**
 *
 * @author HENSEL
 */
public class HeatMapBuilder {

    private final String KEY = "AIzaSyB-DhuKSVz3BiMvvjcdAZx-4dAg31ZRbOA";

    private String keyword;
    private List<Location> locations;

//    private double smoothingFactor = 0.01;
    public HeatMapBuilder(String keyword, HashMap<Location, Integer> locationsMap, double smoothingFactor) {
        this.keyword = keyword;
        this.locations = new ArrayList<Location>();

        double[] raw = new double[locationsMap.size()];
        int i = 0;
        for (Map.Entry<Location, Integer> location : locationsMap.entrySet()) {
            raw[i] = location.getValue();
            i++;
        }
        double[] result = expsmoothing(raw, smoothingFactor);
        int j = 0;
        for (Map.Entry<Location, Integer> location : locationsMap.entrySet()) {
            if (location.getKey().getName() != null) {

                if (DEBUG && result[j] > 10) {
                    System.out.println(location.getKey().getName() + " : " + result[j]);
                }
                for (int k = 0; k < (int) result[j]; k++) {                    
                    locations.add(location.getKey());
                }
            }
            j++;
        }
    }

    private double[] expsmoothing(double[] raw, double smoothingFactor) {
        double[] result = new double[raw.length];

        result[0] = raw[0];
        for (int i = 0; i < raw.length - 1; i++) {
            if (i == 0) {
                result[i + 1] = smoothingFactor * raw[i + 1] + (1 - smoothingFactor) * raw[i];
            } else {
                result[i + 1] = smoothingFactor * raw[i + 1] + (1 - smoothingFactor) * result[i];
            }

        }
        return result;
    }

    public File create() {
        String html = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <meta charset=\"utf-8\">\n"
                + "    <title>HeatMap - " + keyword + "</title>\n"
                + "    <style>\n"
                + "      #map {\n"
                + "        height: 100%;\n"
                + "      }\n"
                + "      html, body {\n"
                + "        height: 100%;\n"
                + "        margin: 0;\n"
                + "        padding: 0;\n"
                + "      }\n"
                + "      #floating-panel {\n"
                + "        position: absolute;\n"
                + "        top: 10px;\n"
                + "        left: 25%;\n"
                + "        z-index: 5;\n"
                + "        background-color: #fff;\n"
                + "        padding: 5px;\n"
                + "        border: 1px solid #999;\n"
                + "        text-align: center;\n"
                + "        font-family: 'Roboto','sans-serif';\n"
                + "        line-height: 30px;\n"
                + "        padding-left: 10px;\n"
                + "      }\n"
                + "      #floating-panel {\n"
                + "        background-color: #fff;\n"
                + "        border: 1px solid #999;\n"
                + "        left: 25%;\n"
                + "        padding: 5px;\n"
                + "        position: absolute;\n"
                + "        top: 10px;\n"
                + "        z-index: 5;\n"
                + "      }\n"
                + "    </style>\n"
                + "  </head>\n"
                + "\n"
                + "  <body>\n"
                + "    <div id=\"floating-panel\">\n"
                + "      <button onclick=\"toggleHeatmap()\">HeatMap umschalten</button>\n"
                + "      <button onclick=\"changeGradient()\">Gradient umschalten</button>\n"
                + "      <button onclick=\"changeRadius()\">Radius umschalten</button>\n"
                + "      <button onclick=\"changeOpacity()\">Deckkraft umschalten</button>\n"
                + "    </div>\n"
                + "    <div id=\"map\"></div>\n"
                + "    <script>\n"
                + "      var map, heatmap;\n"
                + "      function initMap() {\n"
                + "        map = new google.maps.Map(document.getElementById('map'), {\n"
                + "          zoom: 4,\n"
                + "          center: {lat: 51, lng: 10.33},\n"
                + "          mapTypeId: 'satellite'\n"
                + "        });\n"
                + "\n"
                + "        heatmap = new google.maps.visualization.HeatmapLayer({\n"
                + "          data: getPoints(),\n"
                + "          map: map\n"
                + "        });\n"
                + "      }\n"
                + "\n"
                + "      function toggleHeatmap() {\n"
                + "        heatmap.setMap(heatmap.getMap() ? null : map);\n"
                + "      }\n"
                + "\n"
                + "      function changeGradient() {\n"
                + "        var gradient = [\n"
                + "          'rgba(0, 255, 255, 0)',\n"
                + "          'rgba(0, 255, 255, 1)',\n"
                + "          'rgba(0, 191, 255, 1)',\n"
                + "          'rgba(0, 127, 255, 1)',\n"
                + "          'rgba(0, 63, 255, 1)',\n"
                + "          'rgba(0, 0, 255, 1)',\n"
                + "          'rgba(0, 0, 223, 1)',\n"
                + "          'rgba(0, 0, 191, 1)',\n"
                + "          'rgba(0, 0, 159, 1)',\n"
                + "          'rgba(0, 0, 127, 1)',\n"
                + "          'rgba(63, 0, 91, 1)',\n"
                + "          'rgba(127, 0, 63, 1)',\n"
                + "          'rgba(191, 0, 31, 1)',\n"
                + "          'rgba(255, 0, 0, 1)'\n"
                + "        ]\n"
                + "        heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);\n"
                + "      }\n"
                + "\n"
                + "      function changeRadius() {\n"
                + "        heatmap.set('radius', heatmap.get('radius') ? null : 25);\n"
                + "      }\n"
                + "\n"
                + "      function changeOpacity() {\n"
                + "        heatmap.set('opacity', heatmap.get('opacity') ? null : 0.8);\n"
                + "      }\n"
                + "\n"
                + "      function getPoints() {\n"
                + "        return [\n";
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).getLatitude() != 0 && locations.get(i).getLongitude() != 0) {
                html += "          new google.maps.LatLng(" + locations.get(i).getLatitude() + ", " + locations.get(i).getLongitude() + "),\n";
            }
        }
        html += "        ];\n"
                + "      }\n"
                + "    </script>\n"
                + "    <script async defer\n"
                + "        src=\"https://maps.googleapis.com/maps/api/js?key=" + KEY + "&libraries=visualization&callback=initMap\">\n"
                + "    </script>\n"
                + "  </body>\n"
                + "</html>";

        try {

            //create a temp file
            File temp = new File("HeatMap-" + keyword.replaceAll(" ", "_") + ".html");

            //write it
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(html);
            bw.close();

            return temp;

        } catch (IOException e) {

            e.printStackTrace();

        }
        return null;
    }
}
