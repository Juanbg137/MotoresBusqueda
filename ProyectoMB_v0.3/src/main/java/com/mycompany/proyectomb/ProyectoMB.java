package com.mycompany.proyectomb;

import java.io.*;
import java.util.*;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author Juan Barrero González
 */
public class ProyectoMB {

    public static void main(String[] args) throws SolrServerException, IOException {

        String fileName = ".\\collection\\CISI.ALL";
        //Scanner scan = new Scanner(new File(fileName));

        //leeCorpus(fileName);
        buscar(fileName);

    }

    public static void indexar(String indice, String autor, String titulo, String texto) throws SolrServerException, IOException {

        final SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/coleccionprueba").build();
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("index", indice);
        doc.addField("autor", autor);
        doc.addField("titulo", titulo);
        doc.addField("texto", texto);
        client.add(doc);
        client.commit();
    }

    public static void leeCorpus(String donde) throws SolrServerException, IOException {

        Scanner scan = new Scanner(new File(donde));

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

    public static String leerconsulta(String donde) throws SolrServerException, IOException {

        Scanner scan = new Scanner(new File(donde));
        String palabra = null;

        while (scan.hasNextLine()) {

            String ahora = scan.nextLine();

            if (ahora.startsWith(".W")) {
                palabra = palabra + " +" + scan.next();
            } else {
            }
        }

        return palabra;
    }

    public static void buscar(String fileName) throws SolrServerException, IOException {

        /*
        El .QRY tiene consultas por .W (1-57) y de la 58 en adelante pueden ser además por autor(.A) o por título (.T)
        En principio, ignorar .T y punto .A. Posible mejora hacerla completa.
        Que devuelva el nombre y score obligatoriamente, ¿más?
        
        IGNORAR LA CONSULTA 112
         */
        HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/coleccionprueba").build();

        SolrQuery query = new SolrQuery();

        Scanner scan = new Scanner(new File(".\\collection\\CISI.QRY"));
        String palabra = null;
        int contador = 0;
        int qId = 0;
        
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            
            if (line.startsWith(".W")) {
                for (int i = 0; i < 5; i++) {
                    if (palabra == null) {
                        palabra = scan.next();
                    } else {
                        palabra = palabra + " AND " + scan.next();
                        contador++;
                    }
                }
                break;
            } else {
            }

//            if (contador == 4) {
//                break;
//            }
        }

        //query.setQuery(palabra);
        query.setQuery("*:*");
        query.setFields("titulo", "autor", "score", "index");

        QueryResponse rsp = solr.query(query);
        SolrDocumentList docs = rsp.getResults();

        FileWriter fichero = new FileWriter(".\\collection\\trec_solr_file");
        String index = null;
        
        try {
            for (int i = 0; i < docs.size()-1; ++i) {
                System.out.println(docs.get(i));
                index = docs.get(i).getFieldValue("index").toString();
                index = index.replace("[", "");
                index = index.replace("]", "");
                fichero.write( qId + " Q0 " + index + " " + i
                              + " " + docs.get(i).getFieldValue("score") + " JBG\n");
            }
            fichero.close();

        } catch (IOException e) {
            System.out.println("Mensaje de la excepción: " + e.getMessage());
        }

    }
}
