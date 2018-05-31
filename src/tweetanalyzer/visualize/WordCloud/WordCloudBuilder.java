/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.visualize.WordCloud;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.BufferedWriter;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author HENSEL
 */
public class WordCloudBuilder {

    private String keyword;
    private Map<String, Long> weightedKeywords;

    public WordCloudBuilder(String keyword, Map<String, Long> weightedKeywords) {
        this.keyword = keyword;

        this.weightedKeywords = weightedKeywords;
        this.weightedKeywords.remove(keyword);
    }

    public File create() throws IOException, URISyntaxException {

        URL url = Resources.getResource("resources/scripts/wordcloud.js");
        String text = Resources.toString(url, Charsets.UTF_8);

        String html = "<html>\n"
                + "  <head>\n"
                + "    <meta charset=\"ISO-8859-1\">\n"
                + "    <title>WordCloud - " + keyword + "</title>\n"
                + text
                + "		\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <style>\n"
                + "            html, body, #main {\n"
                + "                width: 100%;\n"
                + "                height: 100%;\n"
                + "                margin: 0;\n"
                + "            }\n"
                + "        </style>\n"
                + "        <div id='main'></div>\n"
                + "        <script>\n"
                + "            var chart = echarts.init(document.getElementById('main'));\n"
                + "            var option = {\n"
                + "                tooltip: {},\n"
                + "                series: [ {\n"
                + "                    type: 'wordCloud',\n"
                + "                    gridSize: 8,\n"
                + "                    sizeRange: [24, 90],\n"
                + "                    rotationRange: [-90, 90],\n"
                + "                    shape: 'circle',\n"
                + "                    width: 1600,\n"
                + "                    height: 1000,\n"
                + "                    drawOutOfBound: true,\n"
                + "                    textStyle: {\n"
                + "                        normal: {\n"
                + "                            color: function () {\n"
                + "                                return 'rgb(' + [\n"
                + "                                    Math.round(Math.random() * 160),\n"
                + "                                    Math.round(Math.random() * 160),\n"
                + "                                    Math.round(Math.random() * 160)\n"
                + "                                ].join(',') + ')';\n"
                + "                            }\n"
                + "                        },\n"
                + "                        emphasis: {\n"
                + "                            shadowBlur: 10,\n"
                + "                            shadowColor: '#333'\n"
                + "                        }\n"
                + "                    },\n"
                + "                    data: [\n";

        for (Map.Entry<String, Long> word : weightedKeywords.entrySet()) {

            System.out.println("{text: '" + word.getKey() + "', size: " + word.getValue() + "},");

            html += "                        {\n"
                    + "                            name: '" + word.getKey() + "',\n"
                    + "                            value: " + word.getValue() + ",\n"
                    + "                        },\n";

        }

        html += "                    ]\n"
                + "                } ]\n"
                + "            };\n"
                + "            chart.setOption(option);\n"
                + "            window.onresize = chart.resize;\n"
                + "        </script>\n"
                + "    </body>\n"
                + "</html>";

        try {

            //create a temp file
            File temp = new File("WordCloud-" + keyword.replaceAll(" ", "_") + ".html");

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

}
