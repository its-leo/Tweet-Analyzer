/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.visualize.SingleLineChart;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.BufferedWriter;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import static tweetanalyzer.Main.DEBUG;
import tweetanalyzer.store.Database;

/**
 *
 * @author HENSEL
 */
public class SingleLineChart {

    private static final DecimalFormat nf = new DecimalFormat();

    private Database db;
    private LinkedHashMap<String, Long> topAnalyzedTagsSizeWeighted, topTweetsToFilteredlTagsWeighted;

    public SingleLineChart(Database db, LinkedHashMap<String, Long> topAnalyzedTagsSizeWeighted, LinkedHashMap<String, Long> topTweetsToFilteredlTagsWeighted) {
        this.db = db;
        this.topAnalyzedTagsSizeWeighted = topAnalyzedTagsSizeWeighted;
        this.topTweetsToFilteredlTagsWeighted = topTweetsToFilteredlTagsWeighted;
    }

    public File create() throws IOException, URISyntaxException {

        URL url = Resources.getResource("resources/scripts/canvas.js");
        String text = Resources.toString(url, Charsets.UTF_8);

        int height1 = (topAnalyzedTagsSizeWeighted.size() * 35) + 120;
        int height2 = 0;
        int width = 98;
        boolean k = false;
        if (topTweetsToFilteredlTagsWeighted != null) {
            height2 = (topTweetsToFilteredlTagsWeighted.size() * 35) + 120;
            width = 49;
            k = true;
        }
        String keyword = db.getKeyword();

        String html = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <meta charset=\"ISO-8859-1\">\n"
                + "    <title>Diagramm - " + keyword + "</title>\n"
                + "<h1><p style=\"font-family: Calibri, Optima, Candara, Verdana, Geneva, sans-serif;text-align:center\">" + nf.format(db.getTweets().size()) + " Tweets zu " + db.getTitle(true) + "</p></h1>"
                + "<script>" + text + "</script>"
                + "\n"
                + "<script type=\"text/javascript\">\n"
                + "\n"
                + "window.onload = function () {\n"
                //CHART1
                + "	var chart1 = new CanvasJS.Chart(\"chartContainer1\", {\n"
                + "	axisX:{\n"
                + "		interval: 1,\n"
                + "labelFontSize: 30\n"
                + "	},\n"
                + "	axisY2:{\n"
                + "		interlacedColor: \"rgba(1,77,101,.2)\",\n"
                + "		gridColor: \"rgba(1,77,101,.1)\",\n"
                + "		titleFontSize: 30,\n"
                + "		labelFontSize: 30,\n"
                + "		title: \"Anzahl identifizierter Schlagwortnennungen\"\n"
                + "	},\n"
                + "		data: [              \n"
                + "		{\n"
                + "			// Change type to \"doughnut\", \"line\", \"splineArea\", etc.\n"
                + "			type: \"bar\",\n"
                + "		axisYType: \"secondary\",\n"
                + "color: \"#014D65\",\n"
                + "			dataPoints: [\n";

        for (Map.Entry<String, Long> word : topAnalyzedTagsSizeWeighted.entrySet()) {

            if (!keyword.toLowerCase().replaceAll("#", "").equals(word.getKey().toLowerCase().replaceAll("#", ""))) {
                html += " { label: \"" + word.getKey() + "\", y: " + word.getValue() + "  },\n";
                if (k) {
                    System.out.println("{text: '" + word.getKey() + "', size: " + word.getValue() + "},");
                }
            }
        }

        html += "			]\n"
                + "		}\n"
                + "		]\n"
                + "	});\n";
        if (k) {
            //CHART2      
            html += "	var chart2 = new CanvasJS.Chart(\"chartContainer2\", {\n"
                    + "	axisX:{\n"
                    + "		interval: 1,\n"
                    + "labelFontSize: 30\n"
                    + "	},\n"
                    + "	axisY2:{\n"
                    + "		interlacedColor: \"rgba(1,77,101,.2)\",\n"
                    + "		gridColor: \"rgba(1,77,101,.1)\",\n"
                    + "		titleFontSize: 30,\n"
                    + "		labelFontSize: 30,\n"
                    + "		title: \"Anzahl Tweets zu identifizierten Schlagworten\"\n"
                    + "	},\n"
                    + "		data: [              \n"
                    + "		{\n"
                    + "			// Change type to \"doughnut\", \"line\", \"splineArea\", etc.\n"
                    + "			type: \"bar\",\n"
                    + "		axisYType: \"secondary\",\n"
                    + "color: \"#014D65\",\n"
                    + "			dataPoints: [\n";

            for (Map.Entry<String, Long> word : topTweetsToFilteredlTagsWeighted.entrySet()) {

                if (!keyword.toLowerCase().replaceAll("#", "").equals(word.getKey().toLowerCase().replaceAll("#", ""))) {
                    html += " { label: \"" + word.getKey() + "\", y: " + word.getValue() + "  },\n";
                    if (DEBUG) {
                        System.out.println("{text: '" + word.getKey() + "', size: " + word.getValue() + "},");
                    }
                }
            }

            html += "			]\n"
                    + "		}\n"
                    + "		]\n"
                    + "	});\n";
        }

        html += "	chart1.render();\n";
        if (k) {
            html += "	chart2.render();\n";
        }
        html += "}\n"
                + "</script>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div id=\"chartContainer1\" style=\"height:" + height1 + "px; width: " + width + "%;display: inline-block;\"></div>\n";
        if (k) {
            html += "<div id=\"chartContainer2\" style=\"height:" + height2 + "px; width: " + width + "%;display: inline-block;\"></div>\n";
        }
        html += "</body>\n";
        if (k) {
            html += "<footer>\n"
                    + "<p style=\"font-family: Calibri, Optima, Candara, Verdana, Geneva, sans-serif;text-align:center\">Eine getrennte Betrachtung von Tweets und Schlagwortnennungen ist sinnvoll, da ein Wort in einem Kontext als Schlagwort identifiziert werden kann und in einem anderen nicht.</p>\n"
                    + "</footer>";
        }
        html += "</html>";

        try {

            //create a temp file
            File temp = new File("SingleLine-" + keyword.replaceAll(" ", "_") + ".html");

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
