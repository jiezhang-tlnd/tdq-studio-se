// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import org.talend.dq.analysis.explore.DataExplorer;
import org.talend.dq.analysis.explore.FunctionFrequencyStatExplorer;

/**
 * DOC Zqin class global comment. Detailled comment
 */
public class PatternLowFrequencyStatisticsState extends LowFrequencyStatisticsState {

    public PatternLowFrequencyStatisticsState(List<IndicatorUnit> units) {
        super(units);
    }

    @Override
    protected String getTitle() {
        // MOD hcheng 2009-06-16,0007725: missed indicator name in pattern graphics.
        return DefaultMessagesImpl.getString("FrequencyTypeStates.PatternLowFreqyebctStatistics"); //$NON-NLS-1$
    }

    @Override
    public DataExplorer getDataExplorer() {
        return new FunctionFrequencyStatExplorer();
    }
}
