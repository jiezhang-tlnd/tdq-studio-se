// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.standardization.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;

import com.csvreader.CsvReader;

/**
 * DOC scorreia class global comment. Detailled comment
 */
public class IndexBuilder {

    private String directoryPath;

    private Directory index;

    /**
     * Getter for index.
     * 
     * @return the index
     */
    public Directory getIndex() {
        return this.index;
    }

    public IndexBuilder(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public boolean initializeIndex(String csvFileToIndex, int[] columnsToBeIndexed) throws IOException {
        assert csvFileToIndex != null;
        if (!new File(csvFileToIndex).exists() || !new File(directoryPath).isDirectory()) {
            throw new IOException(csvFileToIndex + " does not exist or" + directoryPath + " is not a directory");
        }
        index = new MMapDirectory(new File(directoryPath));
        // The same analyzer should be used for indexing and searching
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        // Analyzer analyzer = new StandardAnalyzer();
        // the boolean arg in the IndexWriter ctor means to
        // create a new index, overwriting any existing index
        IndexWriter w = new IndexWriter(index, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
        // read the data (this will be the input data of a component called
        // tFirstNameStandardize)
        CsvReader csvReader = new CsvReader(new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(
                csvFileToIndex.toString()), "windows-1252")), ',');
        csvReader.setTextQualifier('"');
        csvReader.setUseTextQualifier(true);

        csvReader.readHeaders();
        while (csvReader.readRecord()) {
            String name = csvReader.get(columnsToBeIndexed[0]);
            String country = csvReader.get(columnsToBeIndexed[1]);
            String gender = csvReader.get(columnsToBeIndexed[2]);
            String count = csvReader.get(columnsToBeIndexed[3]);
            addDoc(w, name, country, gender, count);
        }
        csvReader.close();
        w.optimize();
        w.close();

        return true;
    }

    private static void addDoc(IndexWriter w, String name,  String country, String gender,String count)
            throws IOException {
        if(!country.equals("")&&!gender.equals("")){
        Document doc = new Document();
        Field field = new Field("name", name, Field.Store.YES, Field.Index.ANALYZED, TermVector.YES);
        doc.add(field);
        doc.add(new Field("country", country, Field.Store.YES, Field.Index.NOT_ANALYZED, TermVector.YES));
        doc.add(new Field("gender", gender, Field.Store.YES, Field.Index.NOT_ANALYZED, TermVector.YES));
        doc.add(new Field("count", count, Field.Store.NO, Field.Index.NOT_ANALYZED, TermVector.NO));
        w.addDocument(doc);
        }
    }

}
