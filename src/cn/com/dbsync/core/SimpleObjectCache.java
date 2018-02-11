package cn.com.dbsync.core;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Title: SimpleObjectCached
 * </p>
 * <p>
 * Description:简单的对象缓存
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009-2016
 * </p>
 * <p>
 * Company: servyou
 * </p>
 *
 * @param <K> the type parameter
 * @param <T> the type parameter
 * @author cxi
 * @version 1.0
 * @created 2016 /6/24
 */
public class SimpleObjectCache<K, T> {

    private Map<K, SoftReference<T>> cacheMap ;
    private IGenerateObject<K, T> iGenerateObject;

    /**
     * Instantiates a new Simple object cache.
     *
     * @param iGenerateObject the generate object
     */
    public SimpleObjectCache(IGenerateObject<K, T> iGenerateObject){
        cacheMap = new HashMap<K, SoftReference<T>>();
        this.iGenerateObject = iGenerateObject;
    }

    /**
     * Get object t.
     *
     * @param key the key
     * @return the t
     */
    public T getObject(K key){
        SoftReference<T> softReference = cacheMap.get(key);
        T retObj = null;

        if(softReference == null){
            retObj = this.iGenerateObject.create(key);
            cacheMap.put(key, new SoftReference<T>(retObj));
            return retObj;
        }

        retObj = softReference.get();

        if(retObj == null){
            retObj = this.iGenerateObject.create(key);
            softReference = new SoftReference<T>(retObj);
            cacheMap.put(key, softReference);
            return retObj;
        }

        return retObj;
    }

    /**
     * The interface Generate object.
     *
     * @param <K> the type parameter
     * @param <T> the type parameter
     */
    public interface IGenerateObject<K, T> {
        /**
         * Create t.
         *
         * @param key the key
         * @return the t
         */
        public T create(K key);
    }

}
