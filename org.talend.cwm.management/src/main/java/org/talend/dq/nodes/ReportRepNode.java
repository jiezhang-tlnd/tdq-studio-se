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
package org.talend.dq.nodes;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.dataquality.properties.TDQReportItem;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import orgomg.cwmx.analysis.informationreporting.Report;

/**
 * DOC klliu class global comment. Detailled comment
 */
public class ReportRepNode extends RepositoryNode {

    public static final String ANA_FLODER = "Analyzes";//$NON-NLS-1$

    public static final String GEN_FLODER = "Generated Documents";//$NON-NLS-1$

    private Report report;

    public Report getReport() {
        return this.report;
    }

    /**
     * DOC klliu ReportRepNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public ReportRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);
        if (object != null && object.getProperty() != null) {
            Item item = object.getProperty().getItem();
            if (item != null && item instanceof TDQReportItem) {
                this.report = ((TDQReportItem) item).getReport();
            }
        }
    }

    @Override
    public List<IRepositoryNode> getChildren() {
        List<IRepositoryNode> anaElement = new ArrayList<IRepositoryNode>();
        ReportSubFolderRepNode anaNodeFolder = new ReportSubFolderRepNode(null, this, ENodeType.SIMPLE_FOLDER);
        anaNodeFolder.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.TDQ_REPORTS);
        anaNodeFolder.setProperties(EProperties.LABEL, ANA_FLODER);
        anaElement.add(anaNodeFolder);
        ReportSubFolderRepNode grenNodeFolder = new ReportSubFolderRepNode(null, this, ENodeType.SIMPLE_FOLDER);
        grenNodeFolder.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.TDQ_REPORTS);
        grenNodeFolder.setProperties(EProperties.LABEL, GEN_FLODER);
        anaElement.add(grenNodeFolder);
        return anaElement;
        // return super.getChildren();
    }

    @Override
    public String getLabel() {
        if (this.getReport() != null) {
            return this.getReport().getName();
        }
        return super.getLabel();
    }

}
