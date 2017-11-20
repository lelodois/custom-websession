package br.com.customwebsession.dao.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import br.com.customwebsession.common.MyUtils;
import br.com.customwebsession.dao.SessionRepository;
import br.com.customwebsession.session.HttpSessionImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

@Repository
public class RedisSessionRepository implements SessionRepository {

	@Autowired
	private JedisSentinelPool jedisPool;

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
		return session;
	}

	@Override
	public Object convertAttribute(String name, Map<String, String> attributes) {
		Object value = null;
		String str = attributes.get(name);
		if (str != null) {
			value = MyUtils.fromBase64ToString(str);
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
		Map<String, String> processedAttributes = new HashMap<>();
		for (Entry<String, Object> entry : attributes.entrySet()) {
			String key = processAttribute(entry.getKey());
			String value = MyUtils.fromObjectToBase64(entry.getValue());
			processedAttributes.put(key, value);
		}
		return processedAttributes;
	}

	private String processAttribute(String key) {
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
		jedis.expire(id, 1800);
	}
}
