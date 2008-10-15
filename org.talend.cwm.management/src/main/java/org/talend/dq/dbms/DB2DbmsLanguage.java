// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dq.dbms;

import java.util.Map;

import org.talend.dataquality.indicators.DateGrain;

/**
 * DOC scorreia class global comment. Detailled comment
 */
public class DB2DbmsLanguage extends DbmsLanguage {

    /**
     * DOC scorreia DB2DbmsLanguage constructor comment.
     */
    public DB2DbmsLanguage() {
        super(DbmsLanguage.DB2);
    }

    /**
     * DOC scorreia DB2DbmsLanguage constructor comment.
     * 
     * @param dbmsType
     * @param majorVersion
     * @param minorVersion
     */
    public DB2DbmsLanguage(String dbmsType, int majorVersion, int minorVersion) {
        super(dbmsType, majorVersion, minorVersion);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.cwm.management.api.DbmsLanguage#initDbmsFunctions(java.lang.String)
     */
    @Override
    protected Map<String, Integer> initDbmsFunctions(String dbms) {
        Map<String, Integer> functions = super.initDbmsFunctions(dbms);
        functions.put("TRIM", 1);
        functions.put("CHAR", 1);
        functions.put("VARCHAR", 1);
        functions.put("LENGTH", 1);
        functions.put("REPEAT", 2);
        functions.put("TRANSLATE", 3);
        return functions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.cwm.management.api.DbmsLanguage#getPatternFinderDefaultFunction(java.lang.String)
     */
    @Override
    public String getPatternFinderDefaultFunction(String expression) {
        return "TRANSLATE(CHAR(" + expression + ") ,VARCHAR(REPEAT('9',10) || REPEAT('A',25)||REPEAT('a',25)), "
                + " '1234567890BCDEFGHIJKLMNOPQRSTUVWXYZbcdefghijklmnopqrstuvwxyz')"; // cannot put accents
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.cwm.management.api.DbmsLanguage#getTopNQuery(java.lang.String, int)
     */
    @Override
    public String getTopNQuery(String query, int n) {
        return query + " FETCH FIRST " + n + " ROWS ONLY ";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.cwm.management.api.DbmsLanguage#extract(org.talend.dataquality.indicators.DateGrain,
     * java.lang.String)
     */
    @Override
    protected String extract(DateGrain dateGrain, String colName) {
        return dateGrain.getName() + surroundWith('(', colName, ')');
    }

}
