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
package org.talend.dq.analysis;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.Property;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.helper.ColumnSetHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.ExecutionInformations;
import org.talend.dq.helper.PropertyHelper;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.ColumnSet;
import orgomg.cwm.resource.relational.RelationalPackage;
import orgomg.cwm.resource.relational.Table;
import orgomg.cwm.resource.relational.View;

/**
 * DOC rli class global comment. Detailled comment
 */
public class AnalysisHandler {

    protected Analysis analysis;

    private ExecutionInformations resultMetadata;

    public AnalysisHandler() {
        super();
    }

    /**
     * Method "setAnalysis".
     * 
     * @param columnAnalysis the analysis to set
     */
    public void setAnalysis(Analysis columnAnalysis) {
        this.analysis = columnAnalysis;
        this.resultMetadata = columnAnalysis.getResults().getResultMetadata();
    }

    /**
     * Method "getAnalysis".
     * 
     * @return the analysis
     */
    public Analysis getAnalysis() {
        return this.analysis;
    }

    public String getName() {
        assert analysis != null;
        return this.analysis.getName();
    }

    public void setName(String name) {
        assert analysis != null;
        this.analysis.setName(name);
    }

    public void clearAnalysis() {
        assert analysis != null;
        assert analysis.getContext() != null;
        analysis.getContext().getAnalysedElements().clear();
        analysis.getResults().getIndicators().clear();
    }

    public String getExecuteData() {
        if (resultMetadata.getExecutionDate() != null) {
            DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);

            return format.format(resultMetadata.getExecutionDate());
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public String getExecuteDuration() {
        return resultMetadata.getExecutionDuration() / 1000.0d + " s"; //$NON-NLS-1$
    }

    public String getExecuteNumber() {
        return String.valueOf(resultMetadata.getExecutionNumber());
    }

    public String getLastExecutionNumberOk() {
        if (resultMetadata != null) {
            return String.valueOf(resultMetadata.getLastExecutionNumberOk());
        } else {
            return "0"; //$NON-NLS-1$
        }
    }

    public ExecutionInformations getResultMetadata() {
        return resultMetadata;
    }

    public String getConnectionName() {
        DataManager connection = analysis.getContext().getConnection();
        Property property = PropertyHelper.getProperty(connection);

        return property == null ? "" : property.getLabel(); //$NON-NLS-1$
    }

    public String getTableNames() {
        String str = ""; //$NON-NLS-1$
        for (String aStr : getColumnSetOwnerNames()) {
            str = str + aStr + " "; //$NON-NLS-1$
        }

        return str;
    }

    // bug 10541 fix by zshen,Change some character set to be proper to add view in the table anasys
    public String getViewNames() {
        String str = ""; //$NON-NLS-1$
        for (String aStr : getColumnSetOwnerViewNames()) {
            str = str + aStr + " "; //$NON-NLS-1$
        }

        return str;
    }

    /**
     * Method "getSchemaNames".
     * 
     * @return the schema names concatenated or the empty string (never null)
     */
    public String getSchemaNames() {
        String str = ""; //$NON-NLS-1$
        for (ColumnSet columnSet : getColumnSets()) {
            Package schema = ColumnSetHelper.getParentCatalogOrSchema(columnSet);
            if (schema != null && RelationalPackage.eINSTANCE.getSchema().equals(schema.eClass())) {
                str = str + schema.getName() + " "; //$NON-NLS-1$
            }
        }
        return str;
    }

    /**
     * Method "getCatalogNames".
     * 
     * @return the catalog names concatenated or the empty string (never null)
     */
    public String getCatalogNames() {
        String str = ""; //$NON-NLS-1$
        for (ColumnSet columnSet : getColumnSets()) {
            Package schema = ColumnSetHelper.getParentCatalogOrSchema(columnSet);
            if (schema == null) {
                continue;
            }
            if (RelationalPackage.eINSTANCE.getCatalog().equals(schema.eClass())) {
                str = str + schema.getName() + " "; //$NON-NLS-1$
            } else {
                Package catalog = ColumnSetHelper.getParentCatalogOrSchema(schema);
                if (catalog != null) {
                    str = str + catalog.getName() + " "; //$NON-NLS-1$
                }
            }
        }
        return str;
    }

    public boolean isCatalogExisting() {
        return getCatalogNames().trim().length() != 0 ? true : false;
    }

    public boolean isSchemaExisting() {
        return getSchemaNames().trim().length() != 0 ? true : false;
    }

    private ColumnSet[] getColumnSets() {
        List<ColumnSet> existingTables = new ArrayList<ColumnSet>();

        for (ModelElement element : getAnalyzedColumns()) {
            if (element instanceof TdColumn) {
                ColumnSet columnSet = ColumnHelper.getColumnOwnerAsColumnSet((TdColumn) element);
                if (!existingTables.contains(columnSet)) {
                    existingTables.add(columnSet);
                }
            } else if (element instanceof Table) {
                ColumnSet columnSet = (ColumnSet) element;
                if (!existingTables.contains(columnSet)) {
                    existingTables.add(columnSet);
                }
            }
        }

        return existingTables.toArray(new ColumnSet[existingTables.size()]);
    }

    public EList<ModelElement> getAnalyzedColumns() {
        return analysis.getContext().getAnalysedElements();
    }

    private String[] getColumnSetOwnerNames() {
        List<String> existingTables = new ArrayList<String>();

        for (ModelElement element : getAnalyzedColumns()) {
            if (element instanceof TdColumn && element.eContainer() instanceof Table) {
                String tableName = ColumnHelper.getTableFullName((TdColumn) element);
                if (!existingTables.contains(tableName)) {
                    existingTables.add(tableName);
                }
            } else if (element instanceof Table) {
                String tableName = ((Table) element).getName();
                if (!existingTables.contains(tableName)) {
                    existingTables.add(tableName);
                }

            } else if (element instanceof MetadataColumn) {
                // MOD qiongli 2011-1-28,for delimited file
                MetadataTable table = ColumnHelper.getColumnOwnerAsMetadataTable((MetadataColumn) element);
                String tableName = table.getLabel();
                if (!existingTables.contains(tableName)) {
                    existingTables.add(tableName);
                }
            }
        }

        return existingTables.toArray(new String[existingTables.size()]);
    }

    // bug 10541 fix by zshen,Change some character set to be proper to add view in the table anasys
    private String[] getColumnSetOwnerViewNames() {
        List<String> existingViews = new ArrayList<String>();

        for (ModelElement element : getAnalyzedColumns()) {
            if (element instanceof TdColumn && element.eContainer() instanceof View) {
                String viewName = ColumnHelper.getTableFullName((TdColumn) element);
                if (!existingViews.contains(viewName)) {
                    existingViews.add(viewName);
                }
            } else if (element instanceof View) {
                String viewName = ((View) element).getName();
                if (!existingViews.contains(viewName)) {
                    existingViews.add(viewName);
                }
            }
        }

        return existingViews.toArray(new String[existingViews.size()]);
    }

    /**
     * DOC xqliu Comment method "isMdmConnection".
     * 
     * @return
     */
    public boolean isMdmConnection() {
        if (this.analysis != null) {
            Connection dataProvider = (Connection) this.analysis.getContext().getConnection();
            if (dataProvider != null) {
                return dataProvider instanceof MDMConnection;
            }
        }
        return false;
    }
}
