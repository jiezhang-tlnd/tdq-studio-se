// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.cwm.management.connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.cwm.builders.CatalogBuilder;
import org.talend.cwm.builders.ColumnBuilder;
import org.talend.cwm.builders.TableBuilder;
import org.talend.cwm.builders.ViewBuilder;
import org.talend.cwm.constants.SoftwareSystemConstants;
import org.talend.cwm.db.connection.ConnectionUtils;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.helper.ColumnSetHelper;
import org.talend.cwm.helper.DataProviderHelper;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.cwm.relational.RelationalFactory;
import org.talend.cwm.relational.TdCatalog;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdSchema;
import org.talend.cwm.relational.TdSqlDataType;
import org.talend.cwm.relational.TdTable;
import org.talend.cwm.relational.TdView;
import org.talend.cwm.softwaredeployment.SoftwaredeploymentFactory;
import org.talend.cwm.softwaredeployment.TdDataProvider;
import org.talend.cwm.softwaredeployment.TdProviderConnection;
import org.talend.cwm.softwaredeployment.TdSoftwareSystem;
import org.talend.utils.collections.MultiMapHelper;
import org.talend.utils.sql.metadata.constants.GetColumn;
import org.talend.utils.sql.metadata.constants.MetaDataConstants;
import org.talend.utils.sql.metadata.constants.TypeInfoColumns;
import orgomg.cwm.foundation.softwaredeployment.Component;
import orgomg.cwm.foundation.typemapping.TypeSystem;
import orgomg.cwm.foundation.typemapping.TypemappingFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;
import orgomg.cwm.resource.relational.Column;
import orgomg.cwm.resource.relational.QueryColumnSet;

/**
 * @author scorreia
 * 
 * This utility class creates CWM object from a java.sql classes (e.g. Connection, Driver...)
 */
public final class DatabaseContentRetriever {

    private static Logger log = Logger.getLogger(DatabaseContentRetriever.class);

    private DatabaseContentRetriever() {
    }

    /**
     * Method "getCatalogs".
     * 
     * @param connection the connection
     * @return a map [name of catalog, catalog]
     * @throws SQLException
     */
    public static Collection<TdCatalog> getCatalogs(Connection connection) throws SQLException {
        CatalogBuilder builder = new CatalogBuilder(connection);
        return builder.getCatalogs();
    }

    public static QueryColumnSet getQueryColumnSet(ResultSetMetaData metaData) throws SQLException {
        QueryColumnSet columnSet = ColumnSetHelper.createQueryColumnSet();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            // String columnClassName = metaData.getColumnClassName(i);
            // TODO add other informations
            Column column = ColumnHelper.createColumn(columnName);
            ColumnSetHelper.addColumn(column, columnSet);
        }

        return columnSet;
    }

    /**
     * DOC mzhao bug 8502 2009-10-29.
     * 
     * @param connection
     * @return
     * @throws SQLException
     */
    public static Map<String, List<TdSchema>> getMSSQLSchemas(Connection connection) throws SQLException {
        Map<String, List<TdSchema>> catalogName2schemas = new HashMap<String, List<TdSchema>>();
        DatabaseMetaData connectionMetadata = getConnectionMetadata(connection);
        ResultSet rsc = connectionMetadata.getCatalogs();
        while (rsc.next()) {
            String cl = rsc.getString(MetaDataConstants.TABLE_CAT.name());
            // SQLServerDatabaseMetaData msDBMetaData = (SQLServerDatabaseMetaData) getConnectionMetadata(connection);
            ResultSet schemaRs = null;
            try {
                // Case of JDK 1.6
                Method getSchemaMethod = connectionMetadata.getClass().getMethod("getSchemas", String.class, String.class);
                schemaRs = (ResultSet) getSchemaMethod.invoke(connectionMetadata, cl, null);
            } catch (SecurityException e) {
                // Case of JDK1.5
                schemaRs = getSchemaForJDK15(connectionMetadata);
            } catch (NoSuchMethodException e) {
                schemaRs = getSchemaForJDK15(connectionMetadata);
            } catch (IllegalArgumentException e) {
                schemaRs = getSchemaForJDK15(connectionMetadata);
            } catch (IllegalAccessException e) {
                schemaRs = getSchemaForJDK15(connectionMetadata);
            } catch (InvocationTargetException e) {
                schemaRs = getSchemaForJDK15(connectionMetadata);
            }
            if (schemaRs == null) {
                schemaRs = getSchemaForJDK15(connectionMetadata);
            }

            while (schemaRs.next()) {
                String schemaName = schemaRs.getString(MetaDataConstants.TABLE_CATALOG.name());
                if (schemaName == null) {
                    continue;
                }
                TdSchema schema = createSchema(schemaName);
                MultiMapHelper.addUniqueObjectToListMap(cl, schema, catalogName2schemas);
            }
            schemaRs.close();
        }
        rsc.close();
        return catalogName2schemas;
    }

    /**
     * DOC mzhao Comment method "getSchemaForJDK1_5".
     * 
     * @throws SQLException
     */
    private static ResultSet getSchemaForJDK15(DatabaseMetaData databaseMetaData) throws SQLException {
        return databaseMetaData.getSchemas();
    }

    /**
     * Method "getSchemas" returns a map of catalogs to schemas. Warning: if no catalog is found, catalog name (i.e. key
     * of the map) can be null.
     * 
     * @param connection the connection
     * @return a map [catalog's name -> list of Schemas ].
     * @throws SQLException
     */
    public static Map<String, List<TdSchema>> getSchemas(Connection connection) throws SQLException {
        Map<String, List<TdSchema>> catalogName2schemas = new HashMap<String, List<TdSchema>>();
        ResultSet schemas = null;
        try {
            schemas = getConnectionMetadata(connection).getSchemas();
        } catch (Exception e1) {
            log.warn("Cannot get Schemas from the JDBC driver's metadata", e1);
        }
        try {
            if (schemas != null) {

                // --- check whether the result set has two columns (Oracle and Sybase only return 1 column)
                // MOD xqliu 2009-07-13 bug 7888
                final int columnCount = schemas.getMetaData() == null ? 0 : schemas.getMetaData().getColumnCount();
                // ~
                boolean hasSchema = false;
                while (schemas.next()) {

                    // set link Catalog -> Schema if exists
                    String catName = null;
                    if (columnCount > 1) {
                        try {
                            catName = schemas.getString(MetaDataConstants.TABLE_CATALOG.name());
                        } catch (Exception e) { // catch exception required for DB2/ZOS
                            log
                                    .warn(
                                            "Exception when trying to get the catalog name linked to the schema. Catalogs won't be used.",
                                            e);
                        }
                        if (catName != null) { // standard case (Postgresql)
                            createSchema(schemas, catName, catalogName2schemas);
                        }
                    }

                    if (catName == null) { // MSSQL, Sybase, Oracle, Postgresql
                        // loop on all existing catalogs and create a new Schema for each existing catalog
                        final List<String> catalogNames = new ArrayList<String>();
                        fillListOfCatalogs(connection, catalogNames);
                        if (catalogNames.isEmpty()) {
                            // store schemata with a null key (meaning no catalog -> e.g. Oracle)
                            createSchema(schemas, null, catalogName2schemas);
                        } else { // MSSQL, Sybase case
                            for (String catalogName : catalogNames) {
                                createSchema(schemas, catalogName, catalogName2schemas);
                            }
                        }
                    }
                    hasSchema = true; // found at least one schema
                }
                // handle case of SQLite (no schema no catalog)
                if (!hasSchema && catalogName2schemas.isEmpty()) {
                    // create a fake schema with an empty name (otherwise queries will use the name and will fail)
                    TdSchema schema = createSchema(" "); //$NON-NLS-1$
                    MultiMapHelper.addUniqueObjectToListMap(null, schema, catalogName2schemas);
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {

            // --- release JDBC resources
            if (schemas != null) {
                schemas.close();
            }
        }

        // if no schema exist in catalog, do not create a default one.
        // The tables will be added directly to the catalog.

        return catalogName2schemas;
    }

    /**
     * Method "fillListOfCatalogs" gets the list of catalogs only when the list is empty. When the list is not empty, no
     * call is executed to the connection.
     * 
     * @param connection
     * @param catalogNames the list of catalogs of the given connection
     * @throws SQLException
     */
    private static void fillListOfCatalogs(Connection connection, List<String> catalogNames) throws SQLException {
        if (catalogNames.isEmpty()) {
            // MOD xqliu 2009-10-29 bug 9838
            ResultSet catalogSet = null;
            DatabaseMetaData connectionMetadata = getConnectionMetadata(connection);
            if (connectionMetadata.getDriverName() != null && connectionMetadata.getDriverName().toLowerCase().indexOf(DatabaseConstant.ODBC_ORACLE_DRIVER_NAME) < 0) {
                catalogSet = connectionMetadata.getCatalogs();
            }
            // ~
            try {
                if (catalogSet != null) {
                    // DB support getCatalogs() method
                    while (catalogSet.next()) {
                        String catalogName = catalogSet.getString(MetaDataConstants.TABLE_CAT.name());
                        // MOD xqliu 2009-10-29 bug 9838
                        if (catalogName != null || !"".equals(catalogName)) {
                            catalogNames.add(catalogName);
                        }
                        // ~
                    }
                }
            } catch (SQLException e) {
                log.warn("Cannot retrieve catalogs", e);
            } finally {
                // --- release the result set.
                if (catalogSet != null) {
                    catalogSet.close();
                }
            }
        }
    }

    private static void createSchema(ResultSet schemas, String catalogName, Map<String, List<TdSchema>> catalogName2schemas)
            throws SQLException {
        // MOD xqliu 2009-10-29 bug 9838
        String schemaName = null;
        try {
            schemaName = schemas.getString(MetaDataConstants.TABLE_SCHEM.name());
        } catch (Exception e) {
            log.warn(e, e);
        }
        if (schemaName == null) {
            try {
                schemaName = schemas.getString(DatabaseConstant.ODBC_ORACLE_SCHEMA_NAME);
            } catch (Exception e) {
                log.warn(e, e);
            }
        }
        if (schemaName == null) {
            try { // try to get first column
                schemaName = schemas.getString(1);
            } catch (Exception e) {
                log.warn(e, e);
            }
        }
        // ~
        TdSchema schema = createSchema(schemaName);
        if (schema != null) {
            MultiMapHelper.addUniqueObjectToListMap(catalogName, schema, catalogName2schemas);
        }
    }

    /**
     * Method "getTablesWithColumns".
     * 
     * @param catalogName the name of the Catalog
     * @param schemaPattern the schema pattern
     * @param tablePattern the tables to be loaded (null means all tables are loaded from DB)
     * @param connection
     * @return the tables
     * @throws SQLException
     */
    public static List<TdTable> getTablesWithColumns(String catalogName, String schemaPattern, String tablePattern,
            Connection connection) throws SQLException {
        TableBuilder tableBuilder = new TableBuilder(connection);
        tableBuilder.setColumnsRequested(true);
        return tableBuilder.getColumnSets(catalogName, schemaPattern, tablePattern);
    }

    /**
     * Method "getTablesWithoutColumns".
     * 
     * @param catalogName the name of the Catalog
     * @param schemaPattern the schema pattern
     * @param tablePattern the tables to be loaded (null means all tables are loaded from DB)
     * @param connection
     * @return the tables
     * @throws SQLException
     */
    public static List<TdTable> getTablesWithoutColumns(String catalogName, String schemaPattern, String tablePattern,
            Connection connection) throws SQLException {
        TableBuilder tableBuilder = new TableBuilder(connection);
        return tableBuilder.getColumnSets(catalogName, schemaPattern, tablePattern);
    }

    /**
     * Method "getViewsWithColumns".
     * 
     * @param catalogName
     * @param schemaPattern
     * @param viewPattern
     * @param connection
     * @return the views with the columns
     * @throws SQLException
     */
    public static List<TdView> getViewsWithColumns(String catalogName, String schemaPattern, String viewPattern,
            Connection connection) throws SQLException {
        ViewBuilder viewBuilder = new ViewBuilder(connection);
        viewBuilder.setColumnsRequested(true);
        return viewBuilder.getColumnSets(catalogName, schemaPattern, viewPattern);
    }

    /**
     * Method "getViewsWithoutColumns".
     * 
     * @param catalogName
     * @param schemaPattern
     * @param viewPattern
     * @param connection
     * @return the view without the columns
     * @throws SQLException
     */
    public static List<TdView> getViewsWithoutColumns(String catalogName, String schemaPattern, String viewPattern,
            Connection connection) throws SQLException {
        ViewBuilder viewBuilder = new ViewBuilder(connection);
        return viewBuilder.getColumnSets(catalogName, schemaPattern, viewPattern);
    }

    /**
     * Method "getDataProvider".
     * 
     * @param driver the driver for the database connection
     * @param databaseUrl the database url
     * @param driverProperties the properties given to the driver
     * @return the data provider with a null name. Its name has to be set elsewhere.
     * @throws SQLException
     */
    public static TdDataProvider getDataProvider(Driver driver, String databaseUrl, Properties driverProperties)
            throws SQLException {
        TdDataProvider provider = DataProviderHelper.createTdDataProvider(null);

        // MOD xqliu 2009-10-23 bug 5327
        // print driver properties
        // TODO scorreia adapt this code in order to store information in CWM ????
        // DriverPropertyInfo[] driverProps = driver.getPropertyInfo(databaseUrl, driverProperties);
        DriverPropertyInfo[] driverProps = null;
        if (!databaseUrl.toLowerCase().startsWith("jdbc:odbc:")) {
            driverProps = driver.getPropertyInfo(databaseUrl, driverProperties);
        }
        // ~

        if (driverProps != null) {
            for (int i = 0; i < driverProps.length; i++) {
                DriverPropertyInfo prop = driverProps[i];

                // MOD scorreia 2009-01-09 skip password properties because it is not used and would result in a
                // security hole
                if (TaggedValueHelper.PASSWORD.equals(prop.name)) {
                    continue;
                }

                if (log.isDebugEnabled()) { // TODO use logger here
                    log.debug("Prop description = " + prop.description);
                    log.debug(prop.name + "=" + prop.value);
                }

                // TaggedValue taggedValue = TaggedValueHelper.createTaggedValue(prop.name, prop.value);
                // provider.getTaggedValue().add(taggedValue);

                if (log.isDebugEnabled()) {
                    if (prop.choices != null) {
                        for (int j = 0; j < prop.choices.length; j++) {
                            log.debug("prop choice " + j + " = " + prop.choices[j]);
                        }
                    }
                }
            }
        }

        return provider;
    }

    public static TdProviderConnection getProviderConnection(String dbUrl, String driverClassName, Properties props,
            Connection connection) throws SQLException {
        TdProviderConnection prov = SoftwaredeploymentFactory.eINSTANCE.createTdProviderConnection();
        prov.setName(driverClassName + EcoreUtil.generateUUID()); // TODO scorreia change default name of provider
        // connection
        prov.setDriverClassName(driverClassName);
        prov.setConnectionString(dbUrl);
        try {
            prov.setIsReadOnly(connection.isReadOnly());
        } catch (Exception e) {
            log.warn(e, e);
        }

        // ---add properties as tagged value of the provider connection.
        Enumeration<?> propertyNames = props.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String key = propertyNames.nextElement().toString();
            // hcheng encode password here
            String property = props.getProperty(key);
            if (TaggedValueHelper.PASSWORD.equals(key)) {
                DataProviderHelper.encryptAndSetPassword(prov, property);
            } else {
                TaggedValue taggedValue = TaggedValueHelper.createTaggedValue(key, property);
                prov.getTaggedValue().add(taggedValue);
            }
        }

        // TODO scorreia set name? or let it be set outside of this class?

        return prov;
    }

    public static TdSoftwareSystem getSoftwareSystem(Connection connection) throws SQLException {
        // MOD xqliu 2009-07-13 bug 7888
        DatabaseMetaData databaseMetadata = ConnectionUtils.getConnectionMetadata(connection);
        // ~
        // --- get informations
        String databaseProductName = null;
        try {
            databaseProductName = databaseMetadata.getDatabaseProductName();
        } catch (Exception e1) {
            log.warn("could not get database product name. " + e1, e1);
        }
        String databaseProductVersion = null;
        try {
            databaseProductVersion = databaseMetadata.getDatabaseProductVersion();
        } catch (Exception e1) {
            log.warn("Could not get database product version. " + e1, e1);
        }
        try {
            int databaseMinorVersion = databaseMetadata.getDatabaseMinorVersion();
            int databaseMajorVersion = databaseMetadata.getDatabaseMajorVersion();
            // simplify the database product version when these informations are accessible
            databaseProductVersion = Integer.toString(databaseMajorVersion) + "." + databaseMinorVersion;

            if (log.isDebugEnabled()) {
                log.debug("Database=" + databaseProductName + " | " + databaseProductVersion + ". DB version: "
                        + databaseMajorVersion + "." + databaseMinorVersion);
            }
        } catch (RuntimeException e) {
            // happens for Sybase ASE for example
            if (log.isDebugEnabled()) {
                log.debug("Database=" + databaseProductName + " | " + databaseProductVersion + " " + e, e);
            }
        }

        // --- create and fill the software system
        TdSoftwareSystem system = SoftwaredeploymentFactory.eINSTANCE.createTdSoftwareSystem();
        if (databaseProductName != null) {
            system.setName(databaseProductName);
            system.setSubtype(databaseProductName);
        }
        system.setType(SoftwareSystemConstants.DBMS.toString());
        if (databaseProductVersion != null) {
            system.setVersion(databaseProductVersion);
        }
        Component component = orgomg.cwm.foundation.softwaredeployment.SoftwaredeploymentFactory.eINSTANCE.createComponent();
        system.getOwnedElement().add(component);

        return system;
    }

    public static TypeSystem getTypeSystem(Connection connection) throws SQLException {
        // MOD xqliu 2009-07-13 bug 7888
        DatabaseMetaData databaseMetadata = ConnectionUtils.getConnectionMetadata(connection);
        // ~
        // --- get the types supported by the system
        ResultSet typeInfo = databaseMetadata.getTypeInfo();
        // int columnCount = typeInfo.getMetaData().getColumnCount();

        TypeSystem typeSystem = TypemappingFactory.eINSTANCE.createTypeSystem();
        typeSystem.setName("System type"); // FIXME scorreia put another name?
        while (typeInfo.next()) {
            // --- store the information in CWM structure
            // TODO scorreia change to SQLSimpleType ?
            TdSqlDataType dataType = RelationalFactory.eINSTANCE.createTdSqlDataType();
            try {
                dataType.setName(typeInfo.getString(TypeInfoColumns.TYPE_NAME.name()));
            } catch (Exception e1) {
                log.warn(e1, e1);
            }
            try {
                dataType.setJavaDataType(typeInfo.getInt(TypeInfoColumns.DATA_TYPE.name()));
            } catch (Exception e1) {
                log.warn(e1, e1);
            }
            try {
                dataType.setNumericPrecision(typeInfo.getInt(TypeInfoColumns.PRECISION.name()));
            } catch (Exception e) {
                // some db do not support this method.
                if (log.isDebugEnabled()) {
                    log.debug("precision type skipped");
                }
            }

            try {
                dataType.setNullable(typeInfo.getShort(TypeInfoColumns.NULLABLE.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            try {
                dataType.setCaseSensitive(typeInfo.getBoolean(TypeInfoColumns.CASE_SENSITIVE.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            try {
                dataType.setSearchable(typeInfo.getShort(TypeInfoColumns.SEARCHABLE.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            try {
                dataType.setUnsignedAttribute(typeInfo.getBoolean(TypeInfoColumns.UNSIGNED_ATTRIBUTE.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            try {
                dataType.setAutoIncrement(typeInfo.getBoolean(TypeInfoColumns.AUTO_INCREMENT.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            try {
                dataType.setLocalTypeName(typeInfo.getString(TypeInfoColumns.LOCAL_TYPE_NAME.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            try {
                dataType.setNumericPrecisionRadix(typeInfo.getInt(TypeInfoColumns.NUM_PREC_RADIX.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            try {
                dataType.setTypeNumber(typeInfo.getLong(TypeInfoColumns.DATA_TYPE.name()));
            } catch (Exception e) {
                log.warn(e, e);
            }
            // --- get the informations form the DB
            // TODO scorreia store these informations
            // String literalPrefix = typeInfo.getString(TypeInfoColumns.LITERAL_PREFIX.name());
            // String literalsuffix = typeInfo.getString(TypeInfoColumns.LITERAL_SUFFIX.name());
            // String createparams = typeInfo.getString(TypeInfoColumns.CREATE_PARAMS.name());
            // boolean fixedprecscale = typeInfo.getBoolean(TypeInfoColumns.FIXED_PREC_SCALE.name());
            // short minimumscale = typeInfo.getShort(TypeInfoColumns.MINIMUM_SCALE.name());
            // short maximumscale = typeInfo.getShort(TypeInfoColumns.MAXIMUM_SCALE.name());
            // int sqldatatype = typeInfo.getInt(TypeInfoColumns.SQL_DATA_TYPE.name());
            // int sqldatetimesub = typeInfo.getInt(TypeInfoColumns.SQL_DATETIME_SUB.name());

            // System.out.println(ResultSetUtils.formatRow(typeInfo, columnCount, 10));

            // --- add the element to the list
            typeSystem.getOwnedElement().add(dataType);

            // --- create the mapping with the java language
            // TODO scorreia TypeMapping typeMapping = TypemappingFactory.eINSTANCE.createTypeMapping();
            // typeMapping.

        } // end of loop on typeinfo rows
        return typeSystem;
    }

    /**
     * Method "getColumns".
     * 
     * @param catalogName a catalog name; must match the catalog name as it is stored in the database; "" retrieves
     * those without a catalog; null means that the catalog name should not be used to narrow the search
     * @param schemaPattern a schema name pattern; must match the schema name as it is stored in the database; ""
     * retrieves those without a schema; null means that the schema name should not be used to narrow the search
     * @param tablePattern a table name pattern; must match the table name as it is stored in the database
     * @param columnPattern a column name pattern; must match the column name as it is stored in the database
     * @return the list of columns
     * @throws SQLException
     * @see DatabaseMetaData#getColumns(String, String, String, String)
     */
    public static List<TdColumn> getColumns(String catalogName, String schemaPattern, String tablePattern, String columnPattern,
            Connection connection) throws SQLException {
        return new ColumnBuilder(connection).getColumns(catalogName, schemaPattern, tablePattern, columnPattern);
    }

    /**
     * Method "getDataType".
     * 
     * @param catalogName the catalog (can be null)
     * @param schemaPattern the schema(s) (can be null)
     * @param tablePattern the table(s)
     * @param columnPattern the column(s)
     * @param connection the connection
     * @return the list of datatypes of the given columns
     * @throws SQLException
     */
    public static List<TdSqlDataType> getDataType(String catalogName, String schemaPattern, String tablePattern,
            String columnPattern, Connection connection) throws SQLException {
        List<TdSqlDataType> sqlDatatypes = new ArrayList<TdSqlDataType>();
        ResultSet columns = getConnectionMetadata(connection).getColumns(catalogName, schemaPattern, tablePattern, columnPattern);
        while (columns.next()) {
            sqlDatatypes.add(createDataType(columns));
        }
        columns.close();
        return sqlDatatypes;
    }

    private static DatabaseMetaData getConnectionMetadata(Connection connection) throws SQLException {
        assert connection != null : "Connection should not be null in DatabaseContentRetriever.getConnectionMetadata() "
                + getConnectionInformations(connection);
        // MOD xqliu 2009-07-13 bug 7888
        return ConnectionUtils.getConnectionMetadata(connection);
    }

    public static TdSqlDataType createDataType(ResultSet columns) throws SQLException {
        TdSqlDataType sqlDataType = RelationalFactory.eINSTANCE.createTdSqlDataType();
        try {
            sqlDataType.setName(columns.getString(GetColumn.TYPE_NAME.name()));
        } catch (Exception e1) {
            log.warn(e1, e1);
        }
        try {
            sqlDataType.setJavaDataType(columns.getInt(GetColumn.DATA_TYPE.name()));
        } catch (Exception e1) {
            log.warn(e1, e1);
        }
        try {
            sqlDataType.setNumericPrecision(columns.getInt(GetColumn.DECIMAL_DIGITS.name()));
            sqlDataType.setNumericPrecisionRadix(columns.getInt(GetColumn.NUM_PREC_RADIX.name()));
        } catch (Exception e) {
            log.warn(e);
        }
        return sqlDataType;
    }

    private static String getConnectionInformations(Connection connection) {
        return connection.toString(); // TODO scorreia give more user friendly informations.
    }

    /**
     * Method "createSchema" creates a schema if the given name is not null.
     * 
     * @param name a schema name (or null)
     * @return the created schema or null
     */
    private static TdSchema createSchema(String name) {
        if (name == null) {
            return null;
        }
        TdSchema schema = RelationalFactory.eINSTANCE.createTdSchema();
        schema.setName(name);
        return schema;
    }
}
