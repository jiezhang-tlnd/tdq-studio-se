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
package org.talend.dq.helper;

import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC yyin class global comment. Detailled comment
 */
// @RunWith(PowerMockRunner.class)
// @PrepareForTest({ ProxyRepositoryFactory.class })
public class ProxyRepositoryManagerTest {

    private ProxyRepositoryFactory proxFactory;

    /**
     * DOC yyin Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
    // @Before
    public void setUp() throws Exception {
        // proxFactory = mock(ProxyRepositoryFactory.class);
        // stub(method(ProxyRepositoryFactory.class, "getInstance")).toReturn(proxFactory);//$NON-NLS-1$

    }

    /**
     * DOC yyin Comment method "tearDown".
     *
     * @throws java.lang.Exception
     */
    // @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link org.talend.dq.helper.ProxyRepositoryManager#isLocked(org.talend.core.model.properties.Item)}.
     */
    // @Test
    public void testIsLocked() {
        // Item item = mock(Item.class);
        // when(proxFactory.getStatus(item)).thenReturn(ERepositoryStatus.LOCK_BY_USER);
        // Assert.assertTrue(ProxyRepositoryManager.getInstance().isLocked(item));
        //
        // when(proxFactory.getStatus(item)).thenReturn(ERepositoryStatus.LOCK_BY_OTHER);
        // Assert.assertTrue(ProxyRepositoryManager.getInstance().isLocked(item));
        //
        // when(proxFactory.getStatus(item)).thenReturn(ERepositoryStatus.DEFAULT);
        // Assert.assertFalse(ProxyRepositoryManager.getInstance().isLocked(item));
        //
        // when(proxFactory.getStatus(item)).thenReturn(ERepositoryStatus.READ_ONLY);
        // Assert.assertFalse(ProxyRepositoryManager.getInstance().isLocked(item));
    }

}
