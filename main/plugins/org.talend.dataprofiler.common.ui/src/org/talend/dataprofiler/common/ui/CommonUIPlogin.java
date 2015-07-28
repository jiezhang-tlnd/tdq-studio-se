// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.common.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * created by yyin on 2014-12-24 Detailled comment
 * 
 */
public class CommonUIPlogin extends AbstractUIPlugin {

    /**
     * DOC yyin CommonUIPlogin constructor comment.
     */
    public CommonUIPlogin() {
    }

    private static final Log _logger = LogFactory.getLog(CommonUIPlogin.class);

    private static final String PLUGIN_ID = "org.talend.dataprofiler.common.ui"; //$NON-NLS-1$

    private static CommonUIPlogin plugin;

    private BundleContext bundleContext;

    /**
     * Initialises the Plugin
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        this.bundleContext = context;
        plugin = this;
    }

    public void error(String message) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, String.valueOf(message), null));
        _logger.error(message);
    }

    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    public static CommonUIPlogin getDefault() {
        return plugin;
    }

}
