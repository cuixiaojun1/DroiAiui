package com.droi.aiui.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xjp on 2016/1/19.
 */
public class DBManager {

    private static final String TAG = "DBManager";
    private SQLiteDatabase db = null;
    private MySqLiteHelper mHelper;
    private Context mContext;
    private String db_name;

    public DBManager(Context context, String db_name, int db_version, Class<?> clazz) {
        mHelper = new MySqLiteHelper(context, db_name, db_version, clazz);
        db = mHelper.getWritableDatabase();
        this.mContext = context;
        this.db_name = db_name;
    }


    /**
     * �ر����ݿ�
     */
    public void closeDataBase() {
        db.close();
        mHelper = null;
        db = null;
    }

    /**
     * ɾ�����ݿ�
     *
     * @return �ɹ�����true�����򷵻�false
     */
    public boolean deleteDataBase() {
        return mContext.deleteDatabase(db_name);
    }

    /**
     * ����һ������
     *
     * @param obj
     * @return ����-1����������ݿ�ʧ�ܣ�����ɹ�
     * @throws IllegalAccessException
     */
    public long insert(Object obj) {
        Class<?> modeClass = obj.getClass();
        Field[] fields = modeClass.getDeclaredFields();
        ContentValues values = new ContentValues();

        for (Field fd : fields) {
            fd.setAccessible(true);
            String fieldName = fd.getName();
            //�޳�����idֵ�ñ��棬���ڿ��Ĭ������idΪ�����Զ�����
            if (fieldName.equalsIgnoreCase("id") || fieldName.equalsIgnoreCase("_id")) {
                continue;
            }
            putValues(values, fd, obj);
        }
        return db.insert(DBUtils.getTableName(modeClass), null, values);
    }


    /**
     * ��ѯ���ݿ������е�����
     *
     * @param clazz
     * @param <T>   �� List����ʽ�������ݿ�����������
     * @return ����list����
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public <T> List<T> findAll(Class<T> clazz) {
        Cursor cursor = db.query(clazz.getSimpleName(), null, null, null, null, null, null);
        return getEntity(cursor, clazz);
    }

    /**
     * ����ָ�������������������ļ�¼
     *
     * @param clazz      ��
     * @param select     ������� ����"id>��"��
     * @param selectArgs ����(new String[]{"0"}) ��ѯid=0�ļ�¼
     * @param <T>        ����
     * @return ��������������list����
     */
    public <T> List<T> findByArgs(Class<T> clazz, String select, String[] selectArgs) {
        Cursor cursor = db.query(clazz.getSimpleName(), null, select, selectArgs, null, null, null);
        return getEntity(cursor, clazz);
    }

    /**
     * ͨ��id�����ƶ�����
     *
     * @param clazz ָ����
     * @param id    ����id
     * @param <T>   ����
     * @return �������������Ķ���
     */
    public <T> T findById(Class<T> clazz, int id) {
        Cursor cursor = db.query(clazz.getSimpleName(), null, "id=" + id, null, null, null, null);
        List<T> list = getEntity(cursor, clazz);
        return list.get(0);
    }

    /**
     * ɾ����¼һ����¼
     *
     * @param clazz ��Ҫɾ��������
     * @param id    ��Ҫɾ���� id����
     */
    public void deleteById(Class<?> clazz, long id) {
        int delete = db.delete(DBUtils.getTableName(clazz), "id=" + id, null);
    }

    /**
     * ɾ�����ݿ���ָ���ı�
     *
     * @param clazz
     */
    public void deleteTable(Class<?> clazz) {
        db.execSQL("DROP TABLE IF EXISTS " + DBUtils.getTableName(clazz));
    }

    /**
     * ����һ����¼
     *
     * @param clazz  ��
     * @param values ���¶���
     * @param id     ����id����
     */
    public void updateById(Class<?> clazz, ContentValues values, long id) {
        db.update(clazz.getSimpleName(), values, "id=" + id, null);
    }


    /**
     * put value to ContentValues for Database
     *
     * @param values ContentValues object
     * @param fd     the Field
     * @param obj    the value
     */
    private void putValues(ContentValues values, Field fd, Object obj) {
        Class<?> clazz = values.getClass();
        try {
            Object[] parameters = new Object[]{fd.getName(), fd.get(obj)};
            Class<?>[] parameterTypes = getParameterTypes(fd, fd.get(obj), parameters);
            Method method = clazz.getDeclaredMethod("put", parameterTypes);
            method.setAccessible(true);
            method.invoke(values, parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * �õ����䷽���еĲ�������
     *
     * @param field
     * @param fieldValue
     * @param parameters
     * @return
     */
    private Class<?>[] getParameterTypes(Field field, Object fieldValue, Object[] parameters) {
        Class<?>[] parameterTypes;
        if (isCharType(field)) {
            parameters[1] = String.valueOf(fieldValue);
            parameterTypes = new Class[]{String.class, String.class};
        } else {
            if (field.getType().isPrimitive()) {
                parameterTypes = new Class[]{String.class, getObjectType(field.getType())};
            } else if ("java.util.Date".equals(field.getType().getName())) {
                parameterTypes = new Class[]{String.class, Long.class};
            } else {
                parameterTypes = new Class[]{String.class, field.getType()};
            }
        }
        return parameterTypes;
    }

    /**
     * �Ƿ����ַ�����
     *
     * @param field
     * @return
     */
    private boolean isCharType(Field field) {
        String type = field.getType().getName();
        return type.equals("char") || type.endsWith("Character");
    }

    /**
     * �õ����������
     *
     * @param primitiveType
     * @return
     */
    private Class<?> getObjectType(Class<?> primitiveType) {
        if (primitiveType != null) {
            if (primitiveType.isPrimitive()) {
                String basicTypeName = primitiveType.getName();
                if ("int".equals(basicTypeName)) {
                    return Integer.class;
                } else if ("short".equals(basicTypeName)) {
                    return Short.class;
                } else if ("long".equals(basicTypeName)) {
                    return Long.class;
                } else if ("float".equals(basicTypeName)) {
                    return Float.class;
                } else if ("double".equals(basicTypeName)) {
                    return Double.class;
                } else if ("boolean".equals(basicTypeName)) {
                    return Boolean.class;
                } else if ("char".equals(basicTypeName)) {
                    return Character.class;
                }
            }
        }
        return null;
    }


    /**
     * �����ݿ�õ�ʵ����
     *
     * @param cursor
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> List<T> getEntity(Cursor cursor, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Field[] fields = clazz.getDeclaredFields();
                    T modeClass = clazz.newInstance();
                    for (Field field : fields) {
                        Class<?> cursorClass = cursor.getClass();
                        String columnMethodName = getColumnMethodName(field.getType());
                        Method cursorMethod = cursorClass.getMethod(columnMethodName, int.class);

                        Object value = cursorMethod.invoke(cursor, cursor.getColumnIndex(field.getName()));

                        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            if ("0".equals(String.valueOf(value))) {
                                value = false;
                            } else if ("1".equals(String.valueOf(value))) {
                                value = true;
                            }
                        } else if (field.getType() == char.class || field.getType() == Character.class) {
                            value = ((String) value).charAt(0);
                        } else if (field.getType() == Date.class) {
                            long date = (Long) value;
                            if (date <= 0) {
                                value = null;
                            } else {
                                value = new Date(date);
                            }
                        }
                        String methodName = makeSetterMethodName(field);
                        Method method = clazz.getDeclaredMethod(methodName, field.getType());
                        method.invoke(modeClass, value);
                    }
                    list.add(modeClass);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    private String getColumnMethodName(Class<?> fieldType) {
        String typeName;
        if (fieldType.isPrimitive()) {
            typeName = DBUtils.capitalize(fieldType.getName());
        } else {
            typeName = fieldType.getSimpleName();
        }
        String methodName = "get" + typeName;
        if ("getBoolean".equals(methodName)) {
            methodName = "getInt";
        } else if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
            methodName = "getString";
        } else if ("getDate".equals(methodName)) {
            methodName = "getLong";
        } else if ("getInteger".equals(methodName)) {
            methodName = "getInt";
        }
        return methodName;
    }


    private boolean isPrimitiveBooleanType(Field field) {
        Class<?> fieldType = field.getType();
        if ("boolean".equals(fieldType.getName())) {
            return true;
        }
        return false;
    }

    private String makeSetterMethodName(Field field) {
        String setterMethodName;
        String setterMethodPrefix = "set";
        if (isPrimitiveBooleanType(field) && field.getName().matches("^is[A-Z]{1}.*$")) {
            setterMethodName = setterMethodPrefix + field.getName().substring(2);
        } else if (field.getName().matches("^[a-z]{1}[A-Z]{1}.*")) {
            setterMethodName = setterMethodPrefix + field.getName();
        } else {
            setterMethodName = setterMethodPrefix + DBUtils.capitalize(field.getName());
        }
        return setterMethodName;
    }


    /**
     * ���ݿ������
     */
    class MySqLiteHelper extends SQLiteOpenHelper {
        private static final String TAG = "MySqLiteHelper";

        private Class mClazz;

        public MySqLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public MySqLiteHelper(Context context, String db_name, int db_version, Class clazz) {
            this(context, db_name, null, db_version);
            this.mClazz = clazz;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DBUtils.getTableName(mClazz));
            createTable(db);
        }

        /**
         * �����ƶ�����������
         */
        private void createTable(SQLiteDatabase db) {
            db.execSQL(getCreateTableSql(mClazz));
        }

        /**
         * �õ��������
         *
         * @param clazz ָ����
         * @return sql���
         */
        private String getCreateTableSql(Class<?> clazz) {
            StringBuilder sb = new StringBuilder();
            String tabName = DBUtils.getTableName(clazz);
            sb.append("create table ").append(tabName).append(" (id  INTEGER PRIMARY KEY AUTOINCREMENT, ");
            Field[] fields = clazz.getDeclaredFields();
            for (Field fd : fields) {
                String fieldName = fd.getName();
                String fieldType = fd.getType().getName();
                if (fieldName.equalsIgnoreCase("_id") || fieldName.equalsIgnoreCase("id")) {
                    continue;
                } else {
                    sb.append(fieldName).append(DBUtils.getColumnType(fieldType)).append(", ");
                }
            }
            int len = sb.length();
            sb.replace(len - 2, len, ")");
            Log.d(TAG, "the result is " + sb.toString());
            return sb.toString();
        }
    }
}