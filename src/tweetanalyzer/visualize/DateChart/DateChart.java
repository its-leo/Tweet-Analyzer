/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.visualize.DateChart;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import tweetanalyzer.store.Database;
import tweetanalyzer.store.Tweet;

/**
 *
 * @author HENSEL
 */
public class DateChart {

    private static final DecimalFormat nf = new DecimalFormat();

    private Database db;

    private Set<String> usersTotal;
    //Days
    private Map<LocalDateTime, Long> daysWeighted;
    private Map<LocalDateTime, Long> uniqueDaysWeighted;
    private double average;
    private double uniqueAverage;

    //Hours
    Map<LocalDateTime, Long> hoursWeighted;

    public DateChart(Database db) {
        this.db = db;
        countUserDates(db);
    }

    public File create() throws IOException, URISyntaxException {

        URL url = Resources.getResource("resources/scripts/canvas.js");
        String text = Resources.toString(url, Charsets.UTF_8);

        String html = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <meta charset=\"ISO-8859-1\">\n"
                + "    <title>Diagramm - " + db.getKeyword() + "</title>\n"
                + "<h1><p style=\"font-family: Calibri, Optima, Candara, Verdana, Geneva, sans-serif;text-align:center\">" + nf.format(db.getTweets().size()) + " Tweets von " + nf.format(usersTotal.size()) + " Autoren zu " + db.getTitle(true) + "</p></h1>"
                + "<script>" + text + "</script>"
                + "\n"
                + "<script type=\"text/javascript\">\n"
                + "\n"
                + "window.onload = function () {\n"
                + "    CanvasJS.addCultureInfo(\"de\", \n"
                + "    {      \n"
                + "      decimalSeparator: \",\",\n"
                + "      digitGroupSeparator: \".\", \n"
                + "      shortDays: [\"So\", \"Mo\", \"Di\", \"Mi\", \"Do\", \"Fr\", \"Sa\"]\n"
                + "      \n"
                + "    });"
                + "	var chart = new CanvasJS.Chart(\"chartContainer\", {\n"
                + "             culture:  \"de\","
                + "		animationEnabled: true,\n";
        if (daysWeighted.size() == 1) {
            html += "       axisX:{\n"
                    + "				gridColor: \"Silver\",\n"
                    + "				tickColor: \"silver\",\n"
                    + "        valueFormatString: \"HH:mm \", \n"
                    + "        title: \"Uhrzeit\", \n"
                    + "        interval: 1, \n"
                    + "        gridThickness: 1,\n"
                    + "        intervalType: \"hour\",        \n"
                    + "      },\n"
                    + "      axisY:{\n"
                    + "        title: \"Anzahl Tweets\"\n"
                    + "      },\n"
                    + "      data: [\n"
                    + "      {        \n"
                    + "        type: \"column\",\n"
                    + "        dataPoints: [//array\n";

            for (Map.Entry<LocalDateTime, Long> date : hoursWeighted.entrySet()) {
                LocalDateTime d = date.getKey();
                html += "{ x: new Date(" + d.getYear() + "," + (d.getMonthValue() - 1) + "," + d.getDayOfMonth() + "," + d.getHour() + "," + d.getMinute() + "), y: " + date.getValue() + " },\n";
                System.out.println("{ x: new Date(" + d.getYear() + "," + (d.getMonthValue() - 1) + "," + d.getDayOfMonth() + "," + d.getHour() + "," + d.getMinute() + "), y: " + date.getValue() + " },\n");
            }

            html += "        ]\n"
                    + "      }\n"
                    + "      ],";
        } else {

            html += "			axisX:{\n"
                    + "\n"
                    + "				gridColor: \"Silver\",\n"
                    + "				tickColor: \"silver\",\n"
                    + "				valueFormatString: \"DDD. DD.MM.YYYY\",\n"
                    + "        interval: 1,\n"
                    + "        intervalType: \"day\",\n"
                    + "        labelAngle: 90,\n"
                    + " title: \"Tag\"\n"
                    + "			},                        \n"
                    + "                        toolTip:{\n"
                    + "                          shared:true\n"
                    + "                        },\n"
                    + "			axisY: {\n"
                    + "title: \"Anzahl Tweets\", \n"
                    + "stripLines: [\n"
                    + "        {\n"
                    + "            value : " + average + ",\n"
                    + "				color: \"#F08080\",\n"
                    + "              labelFontSize:16,\n"
                    + "            label: \"Durchschnitt: " + String.format("%.2f", average) + "\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "            value : " + uniqueAverage + ",\n"
                    + "				color: \"#20B2AA\",\n"
                    + "              labelFontSize:16,\n"
                    + "            label: \"Durchschnitt: " + String.format("%.2f", uniqueAverage) + "\"\n"
                    + "        }\n"
                    + "        ],"
                    + "				gridColor: \"Silver\",\n"
                    + "				tickColor: \"silver\"\n"
                    + "			},\n"
                    + "			legend:{\n"
                    + "				verticalAlign: \"center\",\n"
                    + "				horizontalAlign: \"right\"\n"
                    + "			},\n"
                    + "			data: [\n"
                    + "			{        \n"
                    + "				type: \"line\",\n"
                    + "				showInLegend: true,\n"
                    + "				lineThickness: 2,\n"
                    + "				name: \"Mehrere Tweets pro Autor und Tag\",\n"
                    + "				markerType: \"square\",\n"
                    + "				color: \"#F08080\",\n"
                    + "				dataPoints: [\n";

            for (Map.Entry<LocalDateTime, Long> date : daysWeighted.entrySet()) {
                LocalDateTime d = date.getKey();
                html += "{ x: new Date(" + d.getYear() + "," + (d.getMonthValue() - 1) + "," + d.getDayOfMonth() + "), y: " + date.getValue() + " },\n";
                System.out.println("{ x: new Date(" + d.getYear() + "," + (d.getMonthValue() - 1) + "," + d.getDayOfMonth() + "), y: " + date.getValue() + " },\n");
            }

            html += "				]\n"
                    + "			},\n"
                    + "			{        \n"
                    + "				type: \"line\",\n"
                    + "				showInLegend: true,\n"
                    + "				name: \"Ein Tweet pro Autor und Tag\",\n"
                    + "				color: \"#20B2AA\",\n"
                    + "				lineThickness: 2,\n"
                    + "\n"
                    + "				dataPoints: [\n";
            for (Map.Entry<LocalDateTime, Long> date : uniqueDaysWeighted.entrySet()) {
                LocalDateTime d = date.getKey();
                html += "{ x: new Date(" + d.getYear() + "," + (d.getMonthValue() - 1) + "," + d.getDayOfMonth() + "), y: " + date.getValue() + " },\n";
                System.out.println("{ x: new Date(" + d.getYear() + "," + (d.getMonthValue() - 1) + "," + d.getDayOfMonth() + "), y: " + date.getValue() + " },\n");
            }
            html += "				]\n"
                    + "			}\n"
                    + "\n"
                    + "			\n"
                    + "			],\n";
        }
        html += "          legend:{\n"
                + "            cursor:\"pointer\",\n"
                + "            itemclick:function(e){\n"
                + "              if (typeof(e.dataSeries.visible) === \"undefined\" || e.dataSeries.visible) {\n"
                + "              	e.dataSeries.visible = false;\n"
                + "              }\n"
                + "              else{\n"
                + "                e.dataSeries.visible = true;\n"
                + "              }\n"
                + "              chart.render();\n"
                + "            }\n"
                + "          }\n"
                + "		});\n"
                + "\n"
                + "chart.render();\n"
                + "}\n"
                + "</script>"
                + "</head>\n"
                + "<body>\n"
                + "<div id=\"chartContainer\" style=\"height: 600px; width: 100%;\"></div>\n"
                + "</body>\n"
                + "</html>";

        try {

            //create a temp file
            File temp = new File("DateChart-" + db.getKeyword().replaceAll(" ", "_") + ".html");

            //write it
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(html);
            bw.close();
            System.out.println("Done");

            return temp;

        } catch (IOException e) {

            e.printStackTrace();

        }
        return null;
    }

    public void countUserDates(Database db) {
        List<Tweet> tweetList = db.getTweets();
        List<LocalDateTime> hours = new ArrayList<LocalDateTime>();

        List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
        List<LocalDateTime> uniqueDates = new ArrayList<LocalDateTime>();

        Set<String> usersPerDate = new HashSet<String>();
        usersTotal = new HashSet<String>();

        LocalDateTime lastTweetDate = LocalDateTime.ofInstant(tweetList.get(0).getDate().toInstant(), ZoneId.systemDefault());

        for (int i = 0; i < tweetList.size(); i++) {
            Tweet h = tweetList.get(i);
            Date t = tweetList.get(i).getDate();
            LocalDateTime nowTweetDate = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
            hours.add(nowTweetDate.truncatedTo(ChronoUnit.HOURS));
            nowTweetDate = nowTweetDate.truncatedTo(ChronoUnit.DAYS);

            usersTotal.add(h.getUser());

            if (!lastTweetDate.isEqual(nowTweetDate)) {
                usersPerDate = new HashSet<String>();
            }
            if (!usersPerDate.contains(h.getUser())) {
                usersPerDate.add(h.getUser());
                uniqueDates.add(nowTweetDate);
            }
            dates.add(nowTweetDate);
            lastTweetDate = nowTweetDate;

        }

        daysWeighted = new TreeMap<LocalDateTime, Long>(dates.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting())));
        uniqueDaysWeighted = new TreeMap<LocalDateTime, Long>(uniqueDates.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting())));
        hoursWeighted = new TreeMap<LocalDateTime, Long>(hours.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting())));

        average = 0;
        for (Map.Entry<LocalDateTime, Long> date : daysWeighted.entrySet()) {
            average += date.getValue();
        }
        average /= daysWeighted.size();

        uniqueAverage = 0;
        for (Map.Entry<LocalDateTime, Long> date : uniqueDaysWeighted.entrySet()) {
            uniqueAverage += date.getValue();
        }
        uniqueAverage /= uniqueDaysWeighted.size();
    }
}
