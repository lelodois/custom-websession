package br.com.customsession.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import assai.horus.component.web.utils.HorusUtilsBase64;
import assai.horus.component.web.utils.SerializationPostProcessor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

@Repository
@Slf4j
public class SessionRepoImpl implements SessionRepo {

    @Autowired
    private JedisSentinelPool jedisPool;

    @Value("${base.redis.timeout}")
    private Integer userSessionTimeout;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SerializationPostProcessor postProcessor;

    private JedisSentinelPool getJedisPool() {
        return this.jedisPool;
    }

    @Override
    public HttpSessionImpl persist(HttpSessionImpl session) {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.hset(session.getId(), "__CreationTime", Long.toString(session.getCreationTime()));
            jedis.hset(session.getId(), "__LastAccessedTime", Long.toString(session.getCreationTime()));
            expire(jedis, session.getId());
        }
        return session;
    }

    @Override
    public HttpSessionImpl load(ServletContext servletContext, String id) {
        HttpSessionImpl session = null;
        long processTime = System.currentTimeMillis();
        try (Jedis jedis = getJedisPool().getResource()) {
            Map<String, String> attributes = jedis.hgetAll(id);

            String sct = attributes.get("__CreationTime");
            String slat = attributes.get("__LastAccessedTime");

            Long ct = sct != null ? Long.parseLong(sct) : 0l;
            Long lat = slat != null ? Long.parseLong(slat) : 0l;

            if (ct != 0) {
                session = new HttpSessionImpl(servletContext, id, ct, lat, this, attributes);

                jedis.hset(id, "__LastAccessedTime", Long.toString(System.currentTimeMillis()));
                expire(jedis, id);
            }
        }
        log.info(String.format("Load Redis Session Time %d ms", System.currentTimeMillis() - processTime));
        return session;
    }


    @Override
    public Object convertAttribute(String name, Map<String, String> attributes) {
        if (HORUS_FACES_VIEW_MAPS.equals(name)) {
            name = name.concat(context.getApplicationName());
        }

        Object value = null;
        String str = attributes.get(name);
        if (str != null) {
            try {
                value = UtilsBase64.fromString(str);
                if (name.contains(HORUS_FACES_VIEW_MAPS)) {
                    postProcessor.process(value);
                }
            } catch (Exception e) {
                log.error("Erro ao converter o item do redis para o objeto: " + name);
            }
        }
        return value;
    }

    @Override
    public void removeAttribute(String id, String name) {
        this.del(id, name);
    }

    @Override
    public void remove(String id) {
        Set<String> names = this.keys(id);
        this.del(id, names.toArray(new String[names.size()]));
    }

    @Override
    public Set<String> getAttributeNames(String id) {
        return this.keys(id);
    }

    @Override
    public void setAttributes(String id, Map<String, String> originalAttributes, Map<String, Object> attributes) {
        Map<String, String> delta = generateDelta(originalAttributes, attributes);
        if (!delta.isEmpty()) {
            try (Jedis jedis = getJedisPool().getResource()) {
                jedis.hmset(id, delta);
            }
        }
    }

    private Map<String, String> generateDelta(Map<String, String> originalAttributes, Map<String, Object> attributes) {
        Map<String, String> serializedAttributes = processAttributes(attributes);
        Map<String, String> delta = new HashMap<String, String>();
        for (Entry<String, String> entry : serializedAttributes.entrySet()) {
            if (!StringUtils.equals(entry.getValue(), originalAttributes.get(entry.getKey()))) {
                delta.put(entry.getKey(), entry.getValue());
            }
        }
        return delta;
    }

    private Map<String, String> processAttributes(Map<String, Object> attributes) {
        long time = System.currentTimeMillis();
        long facesMaps = 0;
        Map<String, String> processedAttributes = new HashMap<>();
        for (Entry<String, Object> entry : attributes.entrySet()) {
            try {
                String key = processAttribute(entry.getKey());
                String value;
                if (HORUS_FACES_VIEW_MAPS.equals(entry.getKey())) {
                    facesMaps = System.currentTimeMillis();
                    value = UtilsBase64.toStringRemovingServices(entry.getValue());
                    facesMaps = System.currentTimeMillis() - facesMaps;
                } else {
                    value = UtilsBase64.toString(entry.getValue());
                }
                processedAttributes.put(key, value);
            } catch (Exception e) {
                log.error("cannot serializable: " + entry.getKey() + " value: " + entry.getValue());
                e.printStackTrace();
            }
        }
        time = System.currentTimeMillis() - time;
        log.info(String.format("Serializing Redis Attributes: %d ms FacesMap: %d ms Total: %d ms %s", time - facesMaps, facesMaps, time, attributes.keySet()));
        return processedAttributes;
    }

    private String processAttribute(String key) {
        if (HORUS_FACES_VIEW_MAPS.equals(key)) {
            return key.concat(context.getApplicationName());
        }
        return key;
    }

    private void del(String id, String... field) {
        try (Jedis jedis = getJedisPool().getResource()) {
            String[] fields = processFieldName(field);
            jedis.hdel(id, fields);
        }
    }

    private String[] processFieldName(String[] fields) {
        for (String field : fields) {
            field = processAttribute(field);
        }
        return fields;
    }

    private Set<String> keys(String id) {
        try (Jedis jedis = getJedisPool().getResource()) {
            return jedis.hkeys(id);
        }
    }

    private void expire(Jedis jedis, String id) {
        jedis.expire(id, userSessionTimeout != null ? userSessionTimeout : 1800);
    }

}
