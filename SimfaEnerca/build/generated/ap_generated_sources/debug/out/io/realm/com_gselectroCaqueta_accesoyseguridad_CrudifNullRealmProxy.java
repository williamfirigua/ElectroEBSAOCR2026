package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.ImportFlag;
import io.realm.ProxyUtils;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsList;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.Property;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.android.JsonUtils;
import io.realm.internal.objectstore.OsObjectBuilder;
import io.realm.log.RealmLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("all")
public class com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy extends com.gselectroCaqueta.accesoyseguridad.CrudifNull
    implements RealmObjectProxy, com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface {

    static final class CrudifNullColumnInfo extends ColumnInfo {
        long idColKey;
        long imeiColKey;
        long filaColKey;
        long operarioColKey;
        long impresoraColKey;
        long pathColKey;
        long nivelOperadorColKey;

        CrudifNullColumnInfo(OsSchemaInfo schemaInfo) {
            super(7);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("CrudifNull");
            this.idColKey = addColumnDetails("id", "id", objectSchemaInfo);
            this.imeiColKey = addColumnDetails("imei", "imei", objectSchemaInfo);
            this.filaColKey = addColumnDetails("fila", "fila", objectSchemaInfo);
            this.operarioColKey = addColumnDetails("operario", "operario", objectSchemaInfo);
            this.impresoraColKey = addColumnDetails("impresora", "impresora", objectSchemaInfo);
            this.pathColKey = addColumnDetails("path", "path", objectSchemaInfo);
            this.nivelOperadorColKey = addColumnDetails("nivelOperador", "nivelOperador", objectSchemaInfo);
        }

        CrudifNullColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new CrudifNullColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final CrudifNullColumnInfo src = (CrudifNullColumnInfo) rawSrc;
            final CrudifNullColumnInfo dst = (CrudifNullColumnInfo) rawDst;
            dst.idColKey = src.idColKey;
            dst.imeiColKey = src.imeiColKey;
            dst.filaColKey = src.filaColKey;
            dst.operarioColKey = src.operarioColKey;
            dst.impresoraColKey = src.impresoraColKey;
            dst.pathColKey = src.pathColKey;
            dst.nivelOperadorColKey = src.nivelOperadorColKey;
        }
    }

    private static final String NO_ALIAS = "";
    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private CrudifNullColumnInfo columnInfo;
    private ProxyState<com.gselectroCaqueta.accesoyseguridad.CrudifNull> proxyState;

    com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (CrudifNullColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.gselectroCaqueta.accesoyseguridad.CrudifNull>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public int realmGet$id() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.idColKey);
    }

    @Override
    public void realmSet$id(int value) {
        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'id' cannot be changed after object was created.");
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$imei() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.imeiColKey);
    }

    @Override
    public void realmSet$imei(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.imeiColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.imeiColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.imeiColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.imeiColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$fila() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.filaColKey);
    }

    @Override
    public void realmSet$fila(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.filaColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.filaColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.filaColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.filaColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$operario() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.operarioColKey);
    }

    @Override
    public void realmSet$operario(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.operarioColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.operarioColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.operarioColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.operarioColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$impresora() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.impresoraColKey);
    }

    @Override
    public void realmSet$impresora(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.impresoraColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.impresoraColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.impresoraColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.impresoraColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$path() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.pathColKey);
    }

    @Override
    public void realmSet$path(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.pathColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.pathColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.pathColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.pathColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$nivelOperador() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.nivelOperadorColKey);
    }

    @Override
    public void realmSet$nivelOperador(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.nivelOperadorColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.nivelOperadorColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.nivelOperadorColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.nivelOperadorColKey, value);
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder(NO_ALIAS, "CrudifNull", false, 7, 0);
        builder.addPersistedProperty(NO_ALIAS, "id", RealmFieldType.INTEGER, Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "imei", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fila", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "operario", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "impresora", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "path", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "nivelOperador", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static CrudifNullColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new CrudifNullColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "CrudifNull";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "CrudifNull";
    }

    @SuppressWarnings("cast")
    public static com.gselectroCaqueta.accesoyseguridad.CrudifNull createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.gselectroCaqueta.accesoyseguridad.CrudifNull obj = null;
        if (update) {
            Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
            CrudifNullColumnInfo columnInfo = (CrudifNullColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
            long pkColumnKey = columnInfo.idColKey;
            long objKey = Table.NO_MATCH;
            if (!json.isNull("id")) {
                objKey = table.findFirstLong(pkColumnKey, json.getLong("id"));
            }
            if (objKey != Table.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(objKey), realm.getSchema().getColumnInfo(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class), false, Collections.<String> emptyList());
                    obj = new io.realm.com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy();
                } finally {
                    objectContext.clear();
                }
            }
        }
        if (obj == null) {
            if (json.has("id")) {
                if (json.isNull("id")) {
                    obj = (io.realm.com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy) realm.createObjectInternal(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class, null, true, excludeFields);
                } else {
                    obj = (io.realm.com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy) realm.createObjectInternal(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class, json.getInt("id"), true, excludeFields);
                }
            } else {
                throw new IllegalArgumentException("JSON object doesn't have the primary key field 'id'.");
            }
        }

        final com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface objProxy = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) obj;
        if (json.has("imei")) {
            if (json.isNull("imei")) {
                objProxy.realmSet$imei(null);
            } else {
                objProxy.realmSet$imei((String) json.getString("imei"));
            }
        }
        if (json.has("fila")) {
            if (json.isNull("fila")) {
                objProxy.realmSet$fila(null);
            } else {
                objProxy.realmSet$fila((String) json.getString("fila"));
            }
        }
        if (json.has("operario")) {
            if (json.isNull("operario")) {
                objProxy.realmSet$operario(null);
            } else {
                objProxy.realmSet$operario((String) json.getString("operario"));
            }
        }
        if (json.has("impresora")) {
            if (json.isNull("impresora")) {
                objProxy.realmSet$impresora(null);
            } else {
                objProxy.realmSet$impresora((String) json.getString("impresora"));
            }
        }
        if (json.has("path")) {
            if (json.isNull("path")) {
                objProxy.realmSet$path(null);
            } else {
                objProxy.realmSet$path((String) json.getString("path"));
            }
        }
        if (json.has("nivelOperador")) {
            if (json.isNull("nivelOperador")) {
                objProxy.realmSet$nivelOperador(null);
            } else {
                objProxy.realmSet$nivelOperador((String) json.getString("nivelOperador"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.gselectroCaqueta.accesoyseguridad.CrudifNull createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        boolean jsonHasPrimaryKey = false;
        final com.gselectroCaqueta.accesoyseguridad.CrudifNull obj = new com.gselectroCaqueta.accesoyseguridad.CrudifNull();
        final com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface objProxy = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("id")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$id((int) reader.nextInt());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("imei")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$imei((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$imei(null);
                }
            } else if (name.equals("fila")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fila((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fila(null);
                }
            } else if (name.equals("operario")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$operario((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$operario(null);
                }
            } else if (name.equals("impresora")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$impresora((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$impresora(null);
                }
            } else if (name.equals("path")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$path((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$path(null);
                }
            } else if (name.equals("nivelOperador")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$nivelOperador((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$nivelOperador(null);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        if (!jsonHasPrimaryKey) {
            throw new IllegalArgumentException("JSON object doesn't have the primary key field 'id'.");
        }
        return realm.copyToRealmOrUpdate(obj);
    }

    static com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class), false, Collections.<String>emptyList());
        io.realm.com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy obj = new io.realm.com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static com.gselectroCaqueta.accesoyseguridad.CrudifNull copyOrUpdate(Realm realm, CrudifNullColumnInfo columnInfo, com.gselectroCaqueta.accesoyseguridad.CrudifNull object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null) {
            final BaseRealm otherRealm = ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm();
            if (otherRealm.threadId != realm.threadId) {
                throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
            }
            if (otherRealm.getPath().equals(realm.getPath())) {
                return object;
            }
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (com.gselectroCaqueta.accesoyseguridad.CrudifNull) cachedRealmObject;
        }

        com.gselectroCaqueta.accesoyseguridad.CrudifNull realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
            long pkColumnKey = columnInfo.idColKey;
            long objKey = table.findFirstLong(pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
            if (objKey == Table.NO_MATCH) {
                canUpdate = false;
            } else {
                try {
                    objectContext.set(realm, table.getUncheckedRow(objKey), columnInfo, false, Collections.<String> emptyList());
                    realmObject = new io.realm.com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy();
                    cache.put(object, (RealmObjectProxy) realmObject);
                } finally {
                    objectContext.clear();
                }
            }
        }

        return (canUpdate) ? update(realm, columnInfo, realmObject, object, cache, flags) : copy(realm, columnInfo, object, update, cache, flags);
    }

    public static com.gselectroCaqueta.accesoyseguridad.CrudifNull copy(Realm realm, CrudifNullColumnInfo columnInfo, com.gselectroCaqueta.accesoyseguridad.CrudifNull newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.gselectroCaqueta.accesoyseguridad.CrudifNull) cachedRealmObject;
        }

        com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface unmanagedSource = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) newObject;

        Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addInteger(columnInfo.idColKey, unmanagedSource.realmGet$id());
        builder.addString(columnInfo.imeiColKey, unmanagedSource.realmGet$imei());
        builder.addString(columnInfo.filaColKey, unmanagedSource.realmGet$fila());
        builder.addString(columnInfo.operarioColKey, unmanagedSource.realmGet$operario());
        builder.addString(columnInfo.impresoraColKey, unmanagedSource.realmGet$impresora());
        builder.addString(columnInfo.pathColKey, unmanagedSource.realmGet$path());
        builder.addString(columnInfo.nivelOperadorColKey, unmanagedSource.realmGet$nivelOperador());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        return managedCopy;
    }

    public static long insert(Realm realm, com.gselectroCaqueta.accesoyseguridad.CrudifNull object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long tableNativePtr = table.getNativePtr();
        CrudifNullColumnInfo columnInfo = (CrudifNullColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long pkColumnKey = columnInfo.idColKey;
        long objKey = Table.NO_MATCH;
        Object primaryKeyValue = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id();
        if (primaryKeyValue != null) {
            objKey = Table.nativeFindFirstInt(tableNativePtr, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
        }
        if (objKey == Table.NO_MATCH) {
            objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, objKey);
        String realmGet$imei = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$imei();
        if (realmGet$imei != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.imeiColKey, objKey, realmGet$imei, false);
        }
        String realmGet$fila = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$fila();
        if (realmGet$fila != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.filaColKey, objKey, realmGet$fila, false);
        }
        String realmGet$operario = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$operario();
        if (realmGet$operario != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.operarioColKey, objKey, realmGet$operario, false);
        }
        String realmGet$impresora = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$impresora();
        if (realmGet$impresora != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.impresoraColKey, objKey, realmGet$impresora, false);
        }
        String realmGet$path = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$path();
        if (realmGet$path != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.pathColKey, objKey, realmGet$path, false);
        }
        String realmGet$nivelOperador = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$nivelOperador();
        if (realmGet$nivelOperador != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nivelOperadorColKey, objKey, realmGet$nivelOperador, false);
        }
        return objKey;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long tableNativePtr = table.getNativePtr();
        CrudifNullColumnInfo columnInfo = (CrudifNullColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long pkColumnKey = columnInfo.idColKey;
        com.gselectroCaqueta.accesoyseguridad.CrudifNull object = null;
        while (objects.hasNext()) {
            object = (com.gselectroCaqueta.accesoyseguridad.CrudifNull) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = Table.NO_MATCH;
            Object primaryKeyValue = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id();
            if (primaryKeyValue != null) {
                objKey = Table.nativeFindFirstInt(tableNativePtr, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
            }
            if (objKey == Table.NO_MATCH) {
                objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
            } else {
                Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
            }
            cache.put(object, objKey);
            String realmGet$imei = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$imei();
            if (realmGet$imei != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.imeiColKey, objKey, realmGet$imei, false);
            }
            String realmGet$fila = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$fila();
            if (realmGet$fila != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.filaColKey, objKey, realmGet$fila, false);
            }
            String realmGet$operario = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$operario();
            if (realmGet$operario != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.operarioColKey, objKey, realmGet$operario, false);
            }
            String realmGet$impresora = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$impresora();
            if (realmGet$impresora != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.impresoraColKey, objKey, realmGet$impresora, false);
            }
            String realmGet$path = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$path();
            if (realmGet$path != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.pathColKey, objKey, realmGet$path, false);
            }
            String realmGet$nivelOperador = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$nivelOperador();
            if (realmGet$nivelOperador != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nivelOperadorColKey, objKey, realmGet$nivelOperador, false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, com.gselectroCaqueta.accesoyseguridad.CrudifNull object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long tableNativePtr = table.getNativePtr();
        CrudifNullColumnInfo columnInfo = (CrudifNullColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long pkColumnKey = columnInfo.idColKey;
        long objKey = Table.NO_MATCH;
        Object primaryKeyValue = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id();
        if (primaryKeyValue != null) {
            objKey = Table.nativeFindFirstInt(tableNativePtr, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
        }
        if (objKey == Table.NO_MATCH) {
            objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
        }
        cache.put(object, objKey);
        String realmGet$imei = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$imei();
        if (realmGet$imei != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.imeiColKey, objKey, realmGet$imei, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.imeiColKey, objKey, false);
        }
        String realmGet$fila = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$fila();
        if (realmGet$fila != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.filaColKey, objKey, realmGet$fila, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.filaColKey, objKey, false);
        }
        String realmGet$operario = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$operario();
        if (realmGet$operario != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.operarioColKey, objKey, realmGet$operario, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.operarioColKey, objKey, false);
        }
        String realmGet$impresora = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$impresora();
        if (realmGet$impresora != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.impresoraColKey, objKey, realmGet$impresora, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.impresoraColKey, objKey, false);
        }
        String realmGet$path = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$path();
        if (realmGet$path != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.pathColKey, objKey, realmGet$path, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.pathColKey, objKey, false);
        }
        String realmGet$nivelOperador = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$nivelOperador();
        if (realmGet$nivelOperador != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nivelOperadorColKey, objKey, realmGet$nivelOperador, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.nivelOperadorColKey, objKey, false);
        }
        return objKey;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long tableNativePtr = table.getNativePtr();
        CrudifNullColumnInfo columnInfo = (CrudifNullColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        long pkColumnKey = columnInfo.idColKey;
        com.gselectroCaqueta.accesoyseguridad.CrudifNull object = null;
        while (objects.hasNext()) {
            object = (com.gselectroCaqueta.accesoyseguridad.CrudifNull) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = Table.NO_MATCH;
            Object primaryKeyValue = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id();
            if (primaryKeyValue != null) {
                objKey = Table.nativeFindFirstInt(tableNativePtr, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
            }
            if (objKey == Table.NO_MATCH) {
                objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$id());
            }
            cache.put(object, objKey);
            String realmGet$imei = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$imei();
            if (realmGet$imei != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.imeiColKey, objKey, realmGet$imei, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.imeiColKey, objKey, false);
            }
            String realmGet$fila = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$fila();
            if (realmGet$fila != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.filaColKey, objKey, realmGet$fila, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.filaColKey, objKey, false);
            }
            String realmGet$operario = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$operario();
            if (realmGet$operario != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.operarioColKey, objKey, realmGet$operario, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.operarioColKey, objKey, false);
            }
            String realmGet$impresora = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$impresora();
            if (realmGet$impresora != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.impresoraColKey, objKey, realmGet$impresora, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.impresoraColKey, objKey, false);
            }
            String realmGet$path = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$path();
            if (realmGet$path != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.pathColKey, objKey, realmGet$path, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.pathColKey, objKey, false);
            }
            String realmGet$nivelOperador = ((com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) object).realmGet$nivelOperador();
            if (realmGet$nivelOperador != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nivelOperadorColKey, objKey, realmGet$nivelOperador, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.nivelOperadorColKey, objKey, false);
            }
        }
    }

    public static com.gselectroCaqueta.accesoyseguridad.CrudifNull createDetachedCopy(com.gselectroCaqueta.accesoyseguridad.CrudifNull realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.gselectroCaqueta.accesoyseguridad.CrudifNull unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new com.gselectroCaqueta.accesoyseguridad.CrudifNull();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.gselectroCaqueta.accesoyseguridad.CrudifNull) cachedObject.object;
            }
            unmanagedObject = (com.gselectroCaqueta.accesoyseguridad.CrudifNull) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface unmanagedCopy = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) unmanagedObject;
        com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface realmSource = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$id(realmSource.realmGet$id());
        unmanagedCopy.realmSet$imei(realmSource.realmGet$imei());
        unmanagedCopy.realmSet$fila(realmSource.realmGet$fila());
        unmanagedCopy.realmSet$operario(realmSource.realmGet$operario());
        unmanagedCopy.realmSet$impresora(realmSource.realmGet$impresora());
        unmanagedCopy.realmSet$path(realmSource.realmGet$path());
        unmanagedCopy.realmSet$nivelOperador(realmSource.realmGet$nivelOperador());

        return unmanagedObject;
    }

    static com.gselectroCaqueta.accesoyseguridad.CrudifNull update(Realm realm, CrudifNullColumnInfo columnInfo, com.gselectroCaqueta.accesoyseguridad.CrudifNull realmObject, com.gselectroCaqueta.accesoyseguridad.CrudifNull newObject, Map<RealmModel, RealmObjectProxy> cache, Set<ImportFlag> flags) {
        com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface realmObjectTarget = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) realmObject;
        com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface realmObjectSource = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxyInterface) newObject;
        Table table = realm.getTable(com.gselectroCaqueta.accesoyseguridad.CrudifNull.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);
        builder.addInteger(columnInfo.idColKey, realmObjectSource.realmGet$id());
        builder.addString(columnInfo.imeiColKey, realmObjectSource.realmGet$imei());
        builder.addString(columnInfo.filaColKey, realmObjectSource.realmGet$fila());
        builder.addString(columnInfo.operarioColKey, realmObjectSource.realmGet$operario());
        builder.addString(columnInfo.impresoraColKey, realmObjectSource.realmGet$impresora());
        builder.addString(columnInfo.pathColKey, realmObjectSource.realmGet$path());
        builder.addString(columnInfo.nivelOperadorColKey, realmObjectSource.realmGet$nivelOperador());

        builder.updateExistingTopLevelObject();
        return realmObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("CrudifNull = proxy[");
        stringBuilder.append("{id:");
        stringBuilder.append(realmGet$id());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{imei:");
        stringBuilder.append(realmGet$imei() != null ? realmGet$imei() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fila:");
        stringBuilder.append(realmGet$fila() != null ? realmGet$fila() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{operario:");
        stringBuilder.append(realmGet$operario() != null ? realmGet$operario() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{impresora:");
        stringBuilder.append(realmGet$impresora() != null ? realmGet$impresora() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{path:");
        stringBuilder.append(realmGet$path() != null ? realmGet$path() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{nivelOperador:");
        stringBuilder.append(realmGet$nivelOperador() != null ? realmGet$nivelOperador() : "null");
        stringBuilder.append("}");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public ProxyState<?> realmGet$proxyState() {
        return proxyState;
    }

    @Override
    public int hashCode() {
        String realmName = proxyState.getRealm$realm().getPath();
        String tableName = proxyState.getRow$realm().getTable().getName();
        long objKey = proxyState.getRow$realm().getObjectKey();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (objKey ^ (objKey >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy aCrudifNull = (com_gselectroCaqueta_accesoyseguridad_CrudifNullRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aCrudifNull.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aCrudifNull.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aCrudifNull.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}
