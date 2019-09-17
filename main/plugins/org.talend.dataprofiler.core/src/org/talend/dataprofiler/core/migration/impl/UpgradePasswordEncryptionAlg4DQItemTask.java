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
package org.talend.dataprofiler.core.migration.impl;

import java.util.Date;
import java.util.List;

import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.dataprofiler.core.migration.AbstractWorksapceUpdateTask;
import org.talend.dataquality.reports.TdReport;
import org.talend.dq.helper.resourcehelper.PrvResourceFileHelper;
import org.talend.dq.helper.resourcehelper.RepResourceFileHelper;
import org.talend.dq.writer.impl.ElementWriterFactory;
import org.talend.utils.security.PasswordMigrationUtil;

import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * TDQ-16616 msjian: Migration to new encryption/decryption scheme.
 */
public class UpgradePasswordEncryptionAlg4DQItemTask extends AbstractWorksapceUpdateTask {

    public Date getOrder() {
        return createDate(2019, 9, 16);
    }

    public MigrationTaskType getMigrationTaskType() {
        return MigrationTaskType.FILE;
    }

    @Override
    protected boolean doExecute() throws Exception {
        // for report datamart part, consider the password
        List<? extends ModelElement> allElement = RepResourceFileHelper.getInstance().getAllElement();
        for (ModelElement me : allElement) {
            if (me instanceof TdReport) {
                TdReport report = (TdReport) me;
                TaggedValue oldPassword = TaggedValueHelper
                        .getTaggedValue(TaggedValueHelper.REP_DBINFO_PASSWORD, report.getTaggedValue());
                String oldPass = oldPassword.getValue();
                if (oldPass != null) {
                    String newPassword = PasswordMigrationUtil.getEncryptPasswordIfNeed(oldPass);
                    TaggedValueHelper.setTaggedValue(report, TaggedValueHelper.REP_DBINFO_PASSWORD, newPassword); // after
                    ElementWriterFactory.getInstance().createReportWriter().save(me);
                }
            }
        }

        // for database connection, consider the password
        List<? extends ModelElement> allConnectionElement = PrvResourceFileHelper.getInstance().getAllElement();
        for (ModelElement me : allConnectionElement) {
            if (me instanceof DatabaseConnection) {
                DatabaseConnection dbConnection = (DatabaseConnection) me;
                String oldPass = dbConnection.getPassword();
                if (oldPass != null) {
                    dbConnection.setPassword(PasswordMigrationUtil.getEncryptPasswordIfNeed(oldPass));
                    ElementWriterFactory.getInstance().createDataProviderWriter().save(me);
                }
            }
        }
        return true;
    }
}
