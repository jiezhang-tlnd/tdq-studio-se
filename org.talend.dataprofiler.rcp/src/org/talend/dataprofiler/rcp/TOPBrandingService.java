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
package org.talend.dataprofiler.rcp;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.core.ui.branding.IBrandingService;

/**
 * 
 * DOC zshen class global comment. Detailled comment
 */
public class TOPBrandingService implements IBrandingService {

    private IBrandingConfiguration brandingConfigure;

    public TOPBrandingService() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.branding.IBrandingService#getBrandingConfiguration()
     */
    public IBrandingConfiguration getBrandingConfiguration() {
        if (brandingConfigure == null) {
            brandingConfigure = new TOPConfiguration();
        }
        return brandingConfigure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.branding.IBrandingService#getLoginHImage()
     */
    public ImageDescriptor getLoginHImage() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.branding.IBrandingService#getLoginVImage()
     */
    public ImageDescriptor getLoginVImage() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.branding.IBrandingService#isPoweredbyTalend()
     */
    public boolean isPoweredbyTalend() {
        return true;
    }

    public String getFullProductName() {
        return "Talend Open Profiler"; //$NON-NLS-1$
    }

    public URL getLicenseFile() throws IOException {
        final Bundle b = Platform.getBundle(Activator.PLUGIN_ID);
        final URL url = FileLocator.toFileURL(FileLocator.find(b, new Path("resources/license.txt"), null)); //$NON-NLS-1$
        return url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.service.IBrandingService#getCorporationName()
     */
    public String getCorporationName() {
        return "Talend"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.service.IBrandingService#getShortProductName()
     */
    public String getShortProductName() {
        return "T.O.P"; //$NON-NLS-1$
    }

    public String getAcronym() {
        return "top";
    }
}
