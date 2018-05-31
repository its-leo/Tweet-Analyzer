/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import tweetanalyzer.gui.GUI;
import tweetanalyzer.gui.Start;

/**
 * @author HENSEL
 */
public class Main {

    public final static boolean DEBUG = true;
    public final static double VERSION = 2.02;

    private static Locale locale;
    public static DateFormat df;
    public static ResourceBundle rb;
    
    //de or en
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        locale = Locale.getDefault();

        String bundleSource = "resources/languages/English";

        if (locale.getLanguage().equals("de")) {
            bundleSource = "resources/languages/German";
        }

        //TODO
        //df = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);        
        df = new SimpleDateFormat("E. dd.MM.yy HH:mm:ss");

        rb = ResourceBundle.getBundle(bundleSource, locale);

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                Runnable runner = new Runnable() {
                    public void run() {
                        JFrame start = new Start();
                        start.setVisible(true);
                    }
                };
                Thread t = new Thread(runner);
                t.start();
            }
        });
    }

}
