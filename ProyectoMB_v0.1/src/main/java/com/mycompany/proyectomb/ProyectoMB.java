package com.mycompany.proyectomb;

import java.io.*;
import java.util.*;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author Usuario
 */
public class ProyectoMB {

    public static void main(String[] args) throws SolrServerException, IOException {

        String fileName = ".\\collection\\CISI.ALL.extract";
        Scanner scan = new Scanner(new File(fileName));

        final SolrInputDocument doc = new SolrInputDocument();
        String indice = null, autor = null, titulo = null, texto = null;
        
        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            if (line.startsWith(".I")) {
                indexar(indice, autor, titulo, texto);
                String[] partes = line.split(" ");
                indice = partes[1];
            }

            if (line.startsWith(".T")) {
                titulo = scan.nextLine();
            }
            
            if (line.startsWith(".A")) {
                autor = scan.nextLine();
            }


            if (line.startsWith(".W")) {
                texto = scan.nextLine();
                String parte = scan.nextLine();
                while (!".X".equals(parte) && scan.hasNextLine()) {
                    texto += parte;
                    parte = scan.nextLine();
                }
            }
            if (line.startsWith(".X")) {
            } else {
            }

            System.out.println(line);

        }
        indexar(indice, autor, titulo, texto);
    }

    public static void indexar(String indice, String autor, String titulo, String texto) throws SolrServerException, IOException {

        final SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/micoleccion").build();
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("index", indice);
        doc.addField("autor", autor);
        doc.addField("titulo", titulo);
        doc.addField("texto", texto);
        client.add(doc);
        client.commit();
    }
    
}
