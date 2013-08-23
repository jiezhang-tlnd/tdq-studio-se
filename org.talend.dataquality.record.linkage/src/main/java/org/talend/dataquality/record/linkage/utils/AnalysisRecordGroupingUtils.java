// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.record.linkage.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.dataquality.record.linkage.grouping.IRecordGrouping;

/**
 * created by zhao on Aug 20, 2013 Detailled comment
 * 
 */
public class AnalysisRecordGroupingUtils {

    /**
     * DOC zshen Comment method "getCompleteColumnSchema".
     * 
     * @return
     */
    public static String[] getCompleteColumnSchema(Map<String, String> columnMap) {
        List<String> columnNameList = new ArrayList<String>();
        if (columnMap == null) {
            return new String[0];
        }
        for (String columnName : columnMap.keySet()) {
            columnNameList.add(columnName);
        }
        columnNameList.add(MatchAnalysisConstant.GID);
        columnNameList.add(MatchAnalysisConstant.GRP_SIZE);
        columnNameList.add(MatchAnalysisConstant.MASTER);
        columnNameList.add(MatchAnalysisConstant.SCORE);
        columnNameList.add(MatchAnalysisConstant.GRP_QUALITY);
        columnNameList.add(MatchAnalysisConstant.ATTRIBUTE_SCORES);
        return columnNameList.toArray(new String[columnNameList.size()]);
    }

    public static Map<String, String> getMatchKeyMap(String column, String algoType, int confidentWeight,
            Map<String, String> columnIndexMap) {
        Map<String, String> matchKeyMap = new HashMap<String, String>();
        matchKeyMap.put(IRecordGrouping.COLUMN_IDX, columnIndexMap.get(column));
        matchKeyMap.put(IRecordGrouping.MATCHING_TYPE, algoType);
        matchKeyMap.put(IRecordGrouping.CONFIDENCE_WEIGHT, String.valueOf(confidentWeight));
        return matchKeyMap;
    }
}
