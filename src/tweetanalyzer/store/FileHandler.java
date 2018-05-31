/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.store;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author HENSEL
 */
public class FileHandler {

    public void marshall(Database db, File location) throws IOException, JAXBException {
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Database.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(db, location);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    public Database unmarshall(File location) {
        Database db = new Database();
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Database.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            db = (Database) jaxbUnmarshaller.unmarshal(location);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return db;
    }

}
