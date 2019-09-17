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
        List<? extends ModelElement> allElement = RepResourceFileHelper.getInstance().getAllElement();
        for (ModelElement me : allElement) {
            // for report datamart part, consider the password
            if (me instanceof TdReport) {
                TdReport report = (TdReport) me;
                TaggedValue oldPassword = TaggedValueHelper
                        .getTaggedValue(TaggedValueHelper.REP_DBINFO_PASSWORD, report.getTaggedValue());
                String newPassword = PasswordMigrationUtil.getEncryptPasswordIfNeed(oldPassword.getValue());
                TaggedValueHelper.setTaggedValue(report, TaggedValueHelper.REP_DBINFO_PASSWORD, newPassword); // after
                ElementWriterFactory.getInstance().createReportWriter().save(me);
            } else if (me instanceof DatabaseConnection) {
                // for database connection, consider the password
                DatabaseConnection dbConnection = (DatabaseConnection) me;
                String pass = dbConnection.getPassword();
                if (pass != null) {
                    dbConnection.setPassword(PasswordMigrationUtil.getEncryptPasswordIfNeed(pass));
                }
                ElementWriterFactory.getInstance().createReportWriter().save(me);
            }
        }
        return true;
    }
}
