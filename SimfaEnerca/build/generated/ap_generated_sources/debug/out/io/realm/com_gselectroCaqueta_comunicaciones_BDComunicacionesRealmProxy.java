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
public class com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy extends com.gselectroCaqueta.comunicaciones.BDComunicaciones
    implements RealmObjectProxy, com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface {

    static final class BDComunicacionesColumnInfo extends ColumnInfo {
        long idColKey;
        long URLColKey;
        long paginaWsColKey;
        long estadoColKey;
        long rutaAdministradorColKey;
        long puertoApiColKey;

        BDComunicacionesColumnInfo(OsSchemaInfo schemaInfo) {
            super(6);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("BDComunicaciones");
            this.idColKey = addColumnDetails("id", "id", objectSchemaInfo);
            this.URLColKey = addColumnDetails("URL", "URL", objectSchemaInfo);
            this.paginaWsColKey = addColumnDetails("paginaWs", "paginaWs", objectSchemaInfo);
            this.estadoColKey = addColumnDetails("estado", "estado", objectSchemaInfo);
            this.rutaAdministradorColKey = addColumnDetails("rutaAdministrador", "rutaAdministrador", objectSchemaInfo);
            this.puertoApiColKey = addColumnDetails("puertoApi", "puertoApi", objectSchemaInfo);
        }

        BDComunicacionesColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new BDComunicacionesColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final BDComunicacionesColumnInfo src = (BDComunicacionesColumnInfo) rawSrc;
            final BDComunicacionesColumnInfo dst = (BDComunicacionesColumnInfo) rawDst;
            dst.idColKey = src.idColKey;
            dst.URLColKey = src.URLColKey;
            dst.paginaWsColKey = src.paginaWsColKey;
            dst.estadoColKey = src.estadoColKey;
            dst.rutaAdministradorColKey = src.rutaAdministradorColKey;
            dst.puertoApiColKey = src.puertoApiColKey;
        }
    }

    private static final String NO_ALIAS = "";
    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private BDComunicacionesColumnInfo columnInfo;
    private ProxyState<com.gselectroCaqueta.comunicaciones.BDComunicaciones> proxyState;

    com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (BDComunicacionesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<com.gselectroCaqueta.comunicaciones.BDComunicaciones>(this);
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
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.idColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.idColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$URL() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.URLColKey);
    }

    @Override
    public void realmSet$URL(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.URLColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.URLColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.URLColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.URLColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$paginaWs() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.paginaWsColKey);
    }

    @Override
    public void realmSet$paginaWs(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.paginaWsColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.paginaWsColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.paginaWsColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.paginaWsColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$estado() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.estadoColKey);
    }

    @Override
    public void realmSet$estado(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.estadoColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.estadoColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.estadoColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.estadoColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$rutaAdministrador() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.rutaAdministradorColKey);
    }

    @Override
    public void realmSet$rutaAdministrador(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.rutaAdministradorColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.rutaAdministradorColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.rutaAdministradorColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.rutaAdministradorColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public int realmGet$puertoApi() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.puertoApiColKey);
    }

    @Override
    public void realmSet$puertoApi(int value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.puertoApiColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.puertoApiColKey, value);
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder(NO_ALIAS, "BDComunicaciones", false, 6, 0);
        builder.addPersistedProperty(NO_ALIAS, "id", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "URL", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "paginaWs", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "estado", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "rutaAdministrador", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "puertoApi", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static BDComunicacionesColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new BDComunicacionesColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "BDComunicaciones";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "BDComunicaciones";
    }

    @SuppressWarnings("cast")
    public static com.gselectroCaqueta.comunicaciones.BDComunicaciones createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        com.gselectroCaqueta.comunicaciones.BDComunicaciones obj = realm.createObjectInternal(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class, true, excludeFields);

        final com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface objProxy = (com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) obj;
        if (json.has("id")) {
            if (json.isNull("id")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
            } else {
                objProxy.realmSet$id((int) json.getInt("id"));
            }
        }
        if (json.has("URL")) {
            if (json.isNull("URL")) {
                objProxy.realmSet$URL(null);
            } else {
                objProxy.realmSet$URL((String) json.getString("URL"));
            }
        }
        if (json.has("paginaWs")) {
            if (json.isNull("paginaWs")) {
                objProxy.realmSet$paginaWs(null);
            } else {
                objProxy.realmSet$paginaWs((String) json.getString("paginaWs"));
            }
        }
        if (json.has("estado")) {
            if (json.isNull("estado")) {
                objProxy.realmSet$estado(null);
            } else {
                objProxy.realmSet$estado((String) json.getString("estado"));
            }
        }
        if (json.has("rutaAdministrador")) {
            if (json.isNull("rutaAdministrador")) {
                objProxy.realmSet$rutaAdministrador(null);
            } else {
                objProxy.realmSet$rutaAdministrador((String) json.getString("rutaAdministrador"));
            }
        }
        if (json.has("puertoApi")) {
            if (json.isNull("puertoApi")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'puertoApi' to null.");
            } else {
                objProxy.realmSet$puertoApi((int) json.getInt("puertoApi"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static com.gselectroCaqueta.comunicaciones.BDComunicaciones createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        final com.gselectroCaqueta.comunicaciones.BDComunicaciones obj = new com.gselectroCaqueta.comunicaciones.BDComunicaciones();
        final com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface objProxy = (com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) obj;
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
            } else if (name.equals("URL")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$URL((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$URL(null);
                }
            } else if (name.equals("paginaWs")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$paginaWs((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$paginaWs(null);
                }
            } else if (name.equals("estado")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$estado((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$estado(null);
                }
            } else if (name.equals("rutaAdministrador")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$rutaAdministrador((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$rutaAdministrador(null);
                }
            } else if (name.equals("puertoApi")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$puertoApi((int) reader.nextInt());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'puertoApi' to null.");
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return realm.copyToRealm(obj);
    }

    static com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class), false, Collections.<String>emptyList());
        io.realm.com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy obj = new io.realm.com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static com.gselectroCaqueta.comunicaciones.BDComunicaciones copyOrUpdate(Realm realm, BDComunicacionesColumnInfo columnInfo, com.gselectroCaqueta.comunicaciones.BDComunicaciones object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
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
            return (com.gselectroCaqueta.comunicaciones.BDComunicaciones) cachedRealmObject;
        }

        return copy(realm, columnInfo, object, update, cache, flags);
    }

    public static com.gselectroCaqueta.comunicaciones.BDComunicaciones copy(Realm realm, BDComunicacionesColumnInfo columnInfo, com.gselectroCaqueta.comunicaciones.BDComunicaciones newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (com.gselectroCaqueta.comunicaciones.BDComunicaciones) cachedRealmObject;
        }

        com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface unmanagedSource = (com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) newObject;

        Table table = realm.getTable(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addInteger(columnInfo.idColKey, unmanagedSource.realmGet$id());
        builder.addString(columnInfo.URLColKey, unmanagedSource.realmGet$URL());
        builder.addString(columnInfo.paginaWsColKey, unmanagedSource.realmGet$paginaWs());
        builder.addString(columnInfo.estadoColKey, unmanagedSource.realmGet$estado());
        builder.addString(columnInfo.rutaAdministradorColKey, unmanagedSource.realmGet$rutaAdministrador());
        builder.addInteger(columnInfo.puertoApiColKey, unmanagedSource.realmGet$puertoApi());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        return managedCopy;
    }

    public static long insert(Realm realm, com.gselectroCaqueta.comunicaciones.BDComunicaciones object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        long tableNativePtr = table.getNativePtr();
        BDComunicacionesColumnInfo columnInfo = (BDComunicacionesColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        long objKey = OsObject.createRow(table);
        cache.put(object, objKey);
        Table.nativeSetLong(tableNativePtr, columnInfo.idColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$id(), false);
        String realmGet$URL = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$URL();
        if (realmGet$URL != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.URLColKey, objKey, realmGet$URL, false);
        }
        String realmGet$paginaWs = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$paginaWs();
        if (realmGet$paginaWs != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.paginaWsColKey, objKey, realmGet$paginaWs, false);
        }
        String realmGet$estado = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$estado();
        if (realmGet$estado != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.estadoColKey, objKey, realmGet$estado, false);
        }
        String realmGet$rutaAdministrador = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$rutaAdministrador();
        if (realmGet$rutaAdministrador != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.rutaAdministradorColKey, objKey, realmGet$rutaAdministrador, false);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.puertoApiColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$puertoApi(), false);
        return objKey;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        long tableNativePtr = table.getNativePtr();
        BDComunicacionesColumnInfo columnInfo = (BDComunicacionesColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        com.gselectroCaqueta.comunicaciones.BDComunicaciones object = null;
        while (objects.hasNext()) {
            object = (com.gselectroCaqueta.comunicaciones.BDComunicaciones) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createRow(table);
            cache.put(object, objKey);
            Table.nativeSetLong(tableNativePtr, columnInfo.idColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$id(), false);
            String realmGet$URL = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$URL();
            if (realmGet$URL != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.URLColKey, objKey, realmGet$URL, false);
            }
            String realmGet$paginaWs = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$paginaWs();
            if (realmGet$paginaWs != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.paginaWsColKey, objKey, realmGet$paginaWs, false);
            }
            String realmGet$estado = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$estado();
            if (realmGet$estado != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.estadoColKey, objKey, realmGet$estado, false);
            }
            String realmGet$rutaAdministrador = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$rutaAdministrador();
            if (realmGet$rutaAdministrador != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.rutaAdministradorColKey, objKey, realmGet$rutaAdministrador, false);
            }
            Table.nativeSetLong(tableNativePtr, columnInfo.puertoApiColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$puertoApi(), false);
        }
    }

    public static long insertOrUpdate(Realm realm, com.gselectroCaqueta.comunicaciones.BDComunicaciones object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        long tableNativePtr = table.getNativePtr();
        BDComunicacionesColumnInfo columnInfo = (BDComunicacionesColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        long objKey = OsObject.createRow(table);
        cache.put(object, objKey);
        Table.nativeSetLong(tableNativePtr, columnInfo.idColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$id(), false);
        String realmGet$URL = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$URL();
        if (realmGet$URL != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.URLColKey, objKey, realmGet$URL, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.URLColKey, objKey, false);
        }
        String realmGet$paginaWs = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$paginaWs();
        if (realmGet$paginaWs != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.paginaWsColKey, objKey, realmGet$paginaWs, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.paginaWsColKey, objKey, false);
        }
        String realmGet$estado = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$estado();
        if (realmGet$estado != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.estadoColKey, objKey, realmGet$estado, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.estadoColKey, objKey, false);
        }
        String realmGet$rutaAdministrador = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$rutaAdministrador();
        if (realmGet$rutaAdministrador != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.rutaAdministradorColKey, objKey, realmGet$rutaAdministrador, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.rutaAdministradorColKey, objKey, false);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.puertoApiColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$puertoApi(), false);
        return objKey;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        long tableNativePtr = table.getNativePtr();
        BDComunicacionesColumnInfo columnInfo = (BDComunicacionesColumnInfo) realm.getSchema().getColumnInfo(com.gselectroCaqueta.comunicaciones.BDComunicaciones.class);
        com.gselectroCaqueta.comunicaciones.BDComunicaciones object = null;
        while (objects.hasNext()) {
            object = (com.gselectroCaqueta.comunicaciones.BDComunicaciones) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createRow(table);
            cache.put(object, objKey);
            Table.nativeSetLong(tableNativePtr, columnInfo.idColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$id(), false);
            String realmGet$URL = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$URL();
            if (realmGet$URL != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.URLColKey, objKey, realmGet$URL, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.URLColKey, objKey, false);
            }
            String realmGet$paginaWs = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$paginaWs();
            if (realmGet$paginaWs != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.paginaWsColKey, objKey, realmGet$paginaWs, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.paginaWsColKey, objKey, false);
            }
            String realmGet$estado = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$estado();
            if (realmGet$estado != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.estadoColKey, objKey, realmGet$estado, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.estadoColKey, objKey, false);
            }
            String realmGet$rutaAdministrador = ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$rutaAdministrador();
            if (realmGet$rutaAdministrador != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.rutaAdministradorColKey, objKey, realmGet$rutaAdministrador, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.rutaAdministradorColKey, objKey, false);
            }
            Table.nativeSetLong(tableNativePtr, columnInfo.puertoApiColKey, objKey, ((com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) object).realmGet$puertoApi(), false);
        }
    }

    public static com.gselectroCaqueta.comunicaciones.BDComunicaciones createDetachedCopy(com.gselectroCaqueta.comunicaciones.BDComunicaciones realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        com.gselectroCaqueta.comunicaciones.BDComunicaciones unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new com.gselectroCaqueta.comunicaciones.BDComunicaciones();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (com.gselectroCaqueta.comunicaciones.BDComunicaciones) cachedObject.object;
            }
            unmanagedObject = (com.gselectroCaqueta.comunicaciones.BDComunicaciones) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface unmanagedCopy = (com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) unmanagedObject;
        com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface realmSource = (com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$id(realmSource.realmGet$id());
        unmanagedCopy.realmSet$URL(realmSource.realmGet$URL());
        unmanagedCopy.realmSet$paginaWs(realmSource.realmGet$paginaWs());
        unmanagedCopy.realmSet$estado(realmSource.realmGet$estado());
        unmanagedCopy.realmSet$rutaAdministrador(realmSource.realmGet$rutaAdministrador());
        unmanagedCopy.realmSet$puertoApi(realmSource.realmGet$puertoApi());

        return unmanagedObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("BDComunicaciones = proxy[");
        stringBuilder.append("{id:");
        stringBuilder.append(realmGet$id());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{URL:");
        stringBuilder.append(realmGet$URL() != null ? realmGet$URL() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{paginaWs:");
        stringBuilder.append(realmGet$paginaWs() != null ? realmGet$paginaWs() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{estado:");
        stringBuilder.append(realmGet$estado() != null ? realmGet$estado() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{rutaAdministrador:");
        stringBuilder.append(realmGet$rutaAdministrador() != null ? realmGet$rutaAdministrador() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{puertoApi:");
        stringBuilder.append(realmGet$puertoApi());
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
        com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy aBDComunicaciones = (com_gselectroCaqueta_comunicaciones_BDComunicacionesRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aBDComunicaciones.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aBDComunicaciones.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aBDComunicaciones.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}
