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
package org.talend.top.repository;

/***/
public class ProxyRepositoryManager {

    private static ProxyRepositoryManager instance = new ProxyRepositoryManager();

    public static ProxyRepositoryManager getInstance() {
        return instance;
    }

    public void save() {
        save(Boolean.TRUE);
    }

    public void save(boolean avoidUnload) {
        ImplementationHelper.getRepositoryManager().save(avoidUnload);
    }

    public void refresh() {
        ImplementationHelper.getRepositoryManager().refresh();
    }
}
