package ie;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class indexer {
    private static List<Document> forBroadcastDocList = new ArrayList<>();

    public static List<Document> loadForBroadcastDocs(String pathToForBroadcast) throws IOException {
        File[] directories = new File(pathToForBroadcast).listFiles(File::isDirectory);
        System.out.println(directories);
        String docno,text,title;
        for (File directory : directories) {
            File[] files = directory.listFiles();
            for (File file : files) {
                org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");
                Elements documents = d.select("DOC");

                for (Element document : documents) {
                    docno = document.select("DOCNO").text();
                    text = document.select("TEXT").text();
                    title = document.select("TI").text();

                    addForBroadcastDoc(docno, text, title);
                }
            }
        }
        return forBroadcastDocList;
    }

    private static void addForBroadcastDoc(String docno, String text, String title) {
        Document doc = new Document();
        doc.add(new TextField("docno", docno, Field.Store.YES));
        doc.add(new TextField("headline", title, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));

        forBroadcastDocList.add(doc);
    }


    public static List<org.apache.lucene.document.Document> loadLaTimesDocs(String pathToLATimesRegister) throws IOException {

        List<org.apache.lucene.document.Document> parsedLADocsList = new ArrayList<>();

        File folder = new File(pathToLATimesRegister);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {

            org.jsoup.nodes.Document laTimesContent = Jsoup.parse(file, null, "");

            Elements docs = laTimesContent.select("DOC");

            for(Element doc: docs) {
                String docNo, headline, text;
                docNo = (doc.select("DOCNO").text());
                headline = (doc.select("HEADLINE").text());
                text = (doc.select("TEXT").text());
                parsedLADocsList.add(createDocument(docNo, headline, text));
            }
        }
        return parsedLADocsList;
    }

    private static org.apache.lucene.document.Document createDocument(
            String docNo, String headline,String text) {
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField("docno", docNo, Field.Store.YES));
        document.add(new TextField("headline", headline, Field.Store.YES) );
        document.add(new TextField("text", text, Field.Store.YES) );
        return document;
    }

    private static List<Document> fedRegisterDocList = new ArrayList<>();

    public static List<Document> loadFedRegisterDocs(String pathToFedRegister) throws IOException {
        File[] directories = new File(pathToFedRegister).listFiles(File::isDirectory);
        String docno,text,title;
        for (File directory : directories) {
            File[] files = directory.listFiles();
            for (File file : files) {
                org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");
                Elements documents = d.select("DOC");

                for (Element document : documents) {
                    docno = document.select("DOCNO").text();
                    text = document.select("TEXT").text();
                    title = document.select("DOCTITLE").text();

                    addFedRegisterDoc(docno, text, title);
                }
            }
        }
        return fedRegisterDocList;
    }

    private static void addFedRegisterDoc(String docno, String text, String title) {
        Document doc = new Document();
        doc.add(new TextField("docno", docno, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));
        doc.add(new TextField("headline", title, Field.Store.YES));

        fedRegisterDocList.add(doc);
    }



    private static List<Document> finTimesDocList = new ArrayList<>();

    public static List<Document> loadFinTimesDocs(String pathToFinTimes) throws IOException {
        File[] directories = new File(pathToFinTimes).listFiles(File::isDirectory);
        System.out.println(directories);
        String docno,text,title;
        for (File directory : directories) {
            File[] files = directory.listFiles();
            for (File file : files) {
                org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");
                Elements documents = d.select("DOC");

                for (Element document : documents) {
                    docno = document.select("DOCNO").text();
                    text = document.select("TEXT").text();
                    title = document.select("HEADLINE").text();

                    addFinTimesDoc(docno, text, title);
                }
            }
        }
        return finTimesDocList;
    }

    private static void addFinTimesDoc(String docno, String text, String title) {
        Document doc = new Document();
        doc.add(new TextField("docno", docno, Field.Store.YES));
        doc.add(new TextField("headline", title, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));

        finTimesDocList.add(doc);
    }



    private final static Path currentRelativePath = Paths.get("").toAbsolutePath();
    
    private final static String absPathToIndex = String.format("%s/src/Index", currentRelativePath);
    private final static String absPathToFedRegister = String.format("%s/src/Collection/fr94",currentRelativePath);
    private final static String absPathToForBroadcast = String.format("%s/src/Collection/fbis",currentRelativePath);
    private final static String absPathToFinTimes = String.format("%s/src/Collection/ft",currentRelativePath);
    private final static String absPathToLATimes = String.format("%s/src/Collection/latimes",currentRelativePath);
    //... add the other paths

    public void buildDocsIndex(Similarity similarity, Analyzer analyzer) throws IOException {
    	System.out.println(currentRelativePath);
        IndexWriter indexWriter;
        IndexWriterConfig indexWriterConfig  = new IndexWriterConfig(analyzer);
        indexWriterConfig
                .setSimilarity(similarity)
                .setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        Directory directory = FSDirectory.open(Paths.get(absPathToIndex));

        List<Document> fedRegisterDocs = loadFedRegisterDocs(absPathToFedRegister);
        List<Document> forBroadcastDocs = loadForBroadcastDocs(absPathToForBroadcast);
        List<Document> finTimesDocs = loadFinTimesDocs(absPathToFinTimes);
        List<Document> latimesDocs = loadLaTimesDocs(absPathToLATimes);

        try {
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            indexWriter.deleteAll();
            
            System.out.println("Indexing LA Times Document Collection");
            indexWriter.addDocuments(latimesDocs);
            System.out.println("Done Indexing LA Times Document Collection");

//            System.out.println("Indexing Federal Register Document Collection");
//            indexWriter.addDocuments(fedRegisterDocs);
//            System.out.println("Done Indexing Federal Register Document Collection");
//
//            System.out.println("Indexing Foreign Broadcast Information Service Document Collection");
//            indexWriter.addDocuments(forBroadcastDocs);
//            System.out.println("Done Indexing Foreign Broadcast Information Service Document Collection");
//
//            System.out.println("Indexing Financial Times Document Collection");
//            indexWriter.addDocuments(finTimesDocs);
//            System.out.println("Done Indexing Financial Times Document Collection");

           


            indexWriter.close();
            directory.close();

        } catch ( IOException e) {
            System.out.println("ERROR: An error occurred when trying to instantiate a new IndexWriter");
            System.out.println(String.format("ERROR MESSAGE: %s", e.getMessage()));
        }

    }
}
