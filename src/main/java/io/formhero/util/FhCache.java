package io.formhero.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ryankimber on 2016-03-15.
 */
public class FhCache {

    private static final Logger log = LogManager.getLogger(FhCache.class.getName());
    private static FhConfig fhConfig;
    private static boolean shutdown = false;
    private static FhJedis fhJedis;

    private static class FhJedis {
        private JedisPool jedisPool;
        private JedisCluster jedisCluster;

        FhJedis(FhConfig theConfig) throws FhConfigException
        {
            super();
            init(theConfig);
        }

        private void init(FhConfig theConfig) throws FhConfigException
        {
            List<FhConfig> clusterHosts = theConfig.getSubConfigList("redis.connectOptions.cluster");
            if(clusterHosts.size() == 0)
            {
                //This is not a cluster
                jedisPool = getJedisPool(theConfig);
            }
            else //This is a cluster.
            {
                jedisCluster = getJedisCluster(theConfig, clusterHosts);
            }
        }

        void shutdown()
        {
            if(jedisPool != null) jedisPool.destroy();
            //As of 2018-03-27: There is not proper shutdown command for jedisCluster - this is deprecated and throws an exception.
            //if(jedisCluster != null) jedisCluster.shutdown();
        }

        String get(String key) throws FhCacheException, FhConfigException
        {
            if(jedisCluster != null)
            {
                return jedisCluster.get(key);
            }
            else {
                Jedis jedis = null;
                try
                {
                    jedis = jedisPool.getResource();
                    return jedis.get(key);
                }
                catch (JedisConnectionException e)
                {
                    throw new FhCacheException("Unable to get data from cache...", e);
                }
                finally
                {
                    if (jedis != null)
                    {
                        jedis.close();
                    }
                }
            }
        }

        void set(String key, String json, int ttl) throws FhConfigException, FhCacheException
        {
            if(jedisCluster != null)
            {
                jedisCluster.setex(key, ttl, json);
                return;
            }
            else {
                Jedis jedis = null;
                try
                {
                    jedis = jedisPool.getResource();
                    jedis.setex(key, ttl, json);
                }
                catch (JedisConnectionException e)
                {
                    log.warn("JedisConnectionException: " + e.getMessage(), e);
                    e.printStackTrace();
                    throw new FhCacheException("Error in FhCache:", e);
                }
                finally
                {
                    if (jedis != null)
                    {
                        jedis.close();
                    }
                }
            }
        }

        private JedisPool getJedisPool(FhConfig theConfig) throws FhConfigException {
            if(jedisPool == null) {
                FhConfig config = getConfig();
                String server = config.getString("redis.connectOptions.host");
                String pazzword = config.getString("redis.connectOptions.password");
                int port = config.getInt("redis.connectOptions.port", 6379);
                jedisPool = new JedisPool(new JedisPoolConfig(), server, port, Protocol.DEFAULT_TIMEOUT, pazzword, 0);
            }
            return jedisPool;
        }

        private JedisCluster getJedisCluster(FhConfig theConfig, List<FhConfig> clusterHosts) throws FhConfigException {
            if(jedisCluster == null) {
                Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
                for(FhConfig hostCfg : clusterHosts)
                {
                    jedisClusterNodes.add(new HostAndPort(hostCfg.getString("host"), hostCfg.getInt("port", 6379)));
                }
                jedisCluster  = new JedisCluster(jedisClusterNodes);
            }
            return jedisCluster;
        }
    }

    private static FhConfig getConfig() throws FhConfigException
    {
        if(fhConfig == null) fhConfig = FhConfig.loadConfig();
        return fhConfig;
    }

    private static FhJedis getFhJedis() throws FhConfigException
    {
        if(fhJedis == null)
        {
            fhJedis = new FhJedis(getConfig());
        }
        return fhJedis;
    }



    public static String get(String type, String sessionId, String requestId, String key) throws FhConfigException, FhCacheException
    {
        return get(type + "::" + sessionId + "::" + requestId + "::" + key);
    }

    public static String get(String type, String sessionId, String requestId) throws FhConfigException, FhCacheException
    {
        return get(type + "::" + sessionId + "::" + requestId);
    }

    public static String get(String key) throws FhConfigException, FhCacheException {
        return getFhJedis().get(key);
    }

    public static void set(String type, String sessionId, String requestId, String key, String json) throws FhConfigException, FhCacheException
    {
        set(type + "::" + sessionId + "::" + requestId + "::" + key, json);
    }

    public static void set(String type, String sessionId, String requestId, String key, String json, int ttl) throws FhConfigException, FhCacheException
    {
        set(type + "::" + sessionId + "::" + requestId + "::" + key, json, ttl);
    }

    public static void set(String type, String sessionId, String requestId, String json) throws FhConfigException, FhCacheException
    {
        set(type + "::" + sessionId + "::" + requestId, json);
    }

    public static void set(String type, String sessionId, String requestId, String json, int ttl) throws FhConfigException, FhCacheException
    {
        set(type + "::" + sessionId + "::" + requestId, json, ttl);
    }

    public static void set(String key, String json) throws FhConfigException, FhCacheException
    {
        set(key, json, 300);
    }

    public static void set(String key, String json, int ttl) throws FhConfigException, FhCacheException
    {
        getFhJedis().set(key, json, ttl);
    }

    public static void shutdown() throws FhConfigException {
        getFhJedis().shutdown();
    }
}
