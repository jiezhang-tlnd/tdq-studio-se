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
package org.talend.dataprofiler.core.ui.editor.preview.model.states.freq;

import java.util.List;

import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.preview.IndicatorUnit;
import org.talend.dataprofiler.core.ui.utils.ComparatorsFactory;
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.analysis.explore.FrequencyStatisticsExplorer;
import org.talend.dq.indicators.ext.FrequencyExt;

/**
 * DOC Zqin class global comment. Detailled comment
 */
public class LowFrequencyStatisticsState extends FrequencyTypeStates {

    public LowFrequencyStatisticsState(List<IndicatorUnit> units) {
        super(units);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void sortIndicator(FrequencyExt[] frequencyExt) {
        // TODO Auto-generated method stub
        ComparatorsFactory.sort(frequencyExt, ComparatorsFactory.LOW_FREQUENCY_COMPARATOR_ID);
    }

    @Override
    protected String getTitle() {
        return DefaultMessagesImpl.getString("FrequencyTypeStates.LowFreqyebctStatistics"); //$NON-NLS-1$
    }
    
    public DataExplorer getDataExplorer() {
        // TODO Auto-generated method stub
        return new FrequencyStatisticsExplorer();
    }
}
