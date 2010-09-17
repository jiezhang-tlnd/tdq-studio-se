// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.dataprofiler.core.migration.AbstractWorksapceUpdateTask;
import org.talend.dataprofiler.core.ui.imex.model.FileSystemImportWriter;
import org.talend.dq.writer.AElementPersistance;
import org.talend.dq.writer.impl.ElementWriterFactory;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public class UpdatePropertiesFileTask extends AbstractWorksapceUpdateTask {

    private static Logger log = Logger.getLogger(FileSystemImportWriter.class);

    private FilenameFilter nonPropertyFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            IPath namePath = new Path(name);
            String fileExtension = namePath.getFileExtension();
            return fileExtension != null && !name.startsWith(".") && !name.endsWith(FactoriesUtil.PROPERTIES_EXTENSION)
                    && FactoriesUtil.isEmfFile(fileExtension);
        }
    };

    private List<File> fileList;

    private ResourceSet resourceSet = new ResourceSetImpl();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.migration.AbstractWorksapceUpdateTask#valid()
     */
    @Override
    public boolean valid() {
        fileList = new ArrayList<File>();

        for (File folder : getOldTopFolderList()) {
            FilesUtils.getAllFilesFromFolder(folder, fileList, nonPropertyFileFilter);
        }
        return !fileList.isEmpty() && super.valid();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.migration.AMigrationTask#doExecute()
     */
    @Override
    protected boolean doExecute() throws Exception {

        for (File file : fileList) {

            if (file.isFile()) {
                URI uri = URI.createFileURI(file.getAbsolutePath());

                System.out.println("---------Translate " + uri.toString());

                Resource resource = resourceSet.getResource(uri, true);

                EObject eObject = resource.getContents().get(0);

                if (eObject != null) {
                    if (eObject instanceof ModelElement) {
                        ModelElement modelElement = (ModelElement) eObject;
                        AElementPersistance writer = ElementWriterFactory.getInstance().create(uri.fileExtension());

                        if (writer != null) {
                            writer.savePerperties(modelElement);
                        } else {
                            log.warn("Can't create the writer of " + modelElement.getName());
                        }
                    } else {
                        log.warn("Can't get the modelement : " + eObject.toString());
                    }
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.migration.IMigrationTask#getMigrationTaskType()
     */
    public MigrationTaskType getMigrationTaskType() {
        return MigrationTaskType.FILE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataprofiler.core.migration.IMigrationTask#getOrder()
     */
    public Date getOrder() {
        // MOD xqliu 2010-09-15 bug 13941 this task should be called before ExchangeFileNameToReferenceTask
        // return createDate(2010, 8, 17);
        return createDate(2010, 8, 12);
    }

    // /**
    // * DOC xqliu Comment method "file2IFile".
    // *
    // * @param file
    // * @return
    // */
    // private IFile file2IFile(File file) {
    // String filePath = file.getAbsolutePath();
    // String projectPath = getRootProjectFile().getAbsolutePath();
    // filePath = filePath.substring(projectPath.length());
    // return ReponsitoryContextBridge.getRootProject().getFile(filePath);
    // }
    //
    // /**
    // * DOC xqliu Comment method "getRootProjectFile".
    // *
    // * @return
    // */
    // private File getRootProjectFile() {
    // return ResourceManager.getRootProject().getLocation().toFile();
    // }
}
