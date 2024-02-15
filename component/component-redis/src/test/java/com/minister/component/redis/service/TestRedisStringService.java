package com.minister.component.redis.service;

import com.minister.component.redis.TestRedisApplication;
import com.minister.component.utils.JacksonUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * TestRedisStringService
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 12:06
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRedisApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRedisStringService {

    @Resource
    private RedisStringService redisStringService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Before
    public void setUp() {
        redisStringService.del("test", "test1", "test2", "test3", "test4", "test!@#$%^&*()_+11", "test!@#$%^&*()_+12");
    }

    @After
    public void tearDown() {
        redisStringService.del("test", "test1", "test2", "test3", "test4", "test!@#$%^&*()_+11", "test!@#$%^&*()_+12");
    }

    @Test
    public void tPipelined() {
        List<Object> l1 = stringRedisTemplate.executePipelined(new SessionCallback<String>() {
            @Override
            public String execute(RedisOperations operations) throws DataAccessException {
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) operations;

                for (int i = 0; i < 100; i++) {
                    stringRedisTemplate.opsForValue().set("test" + i, "111", 5, TimeUnit.SECONDS);
                    stringRedisTemplate.delete("test" + i);
                }
                return null;
            }
        });
        List<Object> l2 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            l2.add(true);
            l2.add(1);
        }
        Assert.assertEquals(JacksonUtil.bean2Json(l1), JacksonUtil.bean2Json(l2));
    }

    @Test
    public void t01SetAndGet() throws Exception {
        redisStringService.set("test", "test");
        Assert.assertEquals("test", redisStringService.get("test"));
        redisStringService.set("test", "test", 1);
        Assert.assertEquals("test", redisStringService.get("test"));
        Thread.sleep(2000);
        Assert.assertNull(redisStringService.get("test"));
    }

    @Test
    public void t02Del() {
        redisStringService.set("test", "test");
        redisStringService.del("test");
        Assert.assertNull(redisStringService.get("test"));
        redisStringService.set("test1", "test");
        redisStringService.set("test2", "test");
        redisStringService.del(new ArrayList<String>() {{
            add("test1");
            add("test2");
        }});
        Assert.assertNull(redisStringService.get("test1"));
        Assert.assertNull(redisStringService.get("test2"));
    }

    @Test
    public void t03MSetAndMGet() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("test1", "test");
            put("test2", "test");
        }};
        redisStringService.mSet(map);
        TreeMap<String, String> treeMap1 = new TreeMap<>(map);
        TreeMap<String, String> treeMap2 = new TreeMap<>(redisStringService.mGet(new ArrayList<String>() {{
            add("test1");
            add("test2");
        }}));
        Assert.assertEquals(treeMap1, treeMap2);
        redisStringService.del("test2");
        map.remove("test2");
        TreeMap<String, String> treeMap3 = new TreeMap<>(map);
        TreeMap<String, String> treeMap4 = new TreeMap<>(redisStringService.mGet(new ArrayList<String>() {{
            add("test1");
            add("test2");
        }}));
        Assert.assertEquals(treeMap3, treeMap4);
    }

    @Test
    public void t04Exists() {
        redisStringService.set("test", "test");
        Assert.assertTrue(redisStringService.exists("test"));
        redisStringService.del("test");
        Assert.assertFalse(redisStringService.exists("test"));
    }

    @Test
    public void t05ExpireAndTtl() throws Exception {
        // expire
        redisStringService.set("test", "test");
        Assert.assertEquals(-1, redisStringService.ttl("test"));
        Assert.assertTrue(redisStringService.expire("test", 1));
        Assert.assertTrue(redisStringService.ttl("test") >= 0);
        Thread.sleep(2000);
        Assert.assertEquals(-2, redisStringService.ttl("test"));
        // expireAt
        redisStringService.set("test", "test");
        Assert.assertTrue(redisStringService.expireAt("test", DateUtils.addSeconds(new Date(), 1)));
        Assert.assertTrue(redisStringService.ttl("test") >= 0);
        // 服务器时间和本地时间不一致，灵活调整等待时间
        Thread.sleep(5000);
        Assert.assertEquals(-2, redisStringService.ttl("test"));
    }

    @Test
    public void t06Keys() {
        Set<String> assertSet = new HashSet<String>() {{
            add("test!@#$%^&*()_+11");
            add("test!@#$%^&*()_+12");
        }};
        redisStringService.set("test!@#$%^&*()_+11", "test");
        redisStringService.set("test!@#$%^&*()_+12", "test");
        // test ?
        Set<String> redisSet = new HashSet<>(redisStringService.keys("test!@#$%^&*()_+1?"));
        Assert.assertArrayEquals(assertSet.toArray(new String[0]), redisSet.toArray(new String[0]));
        // test *
        redisSet.clear();
        redisSet.addAll(redisStringService.keys("test!@#$%^&*()_+*"));
        Assert.assertArrayEquals(assertSet.toArray(new String[0]), redisSet.toArray(new String[0]));
        // test []
        redisSet.clear();
        redisSet.addAll(redisStringService.keys("test!@#$%^&*()_+1[12]"));
        Assert.assertArrayEquals(assertSet.toArray(new String[0]), redisSet.toArray(new String[0]));
    }

    @Test
    public void t07Persist() {
        redisStringService.set("test", "test");
        Assert.assertTrue(redisStringService.expire("test", 3));
        Assert.assertTrue(redisStringService.ttl("test") >= 0);
        Assert.assertTrue(redisStringService.persist("test"));
        Assert.assertEquals(-1, redisStringService.ttl("test"));
    }

    @Test
    public void t08Rename() {
        redisStringService.set("test1", "test");
        redisStringService.rename("test1", "test2");
        Assert.assertFalse(redisStringService.exists("test1"));
        Assert.assertEquals("test", redisStringService.get("test2"));
    }

    @Test
    public void t09RenameNX() {
        redisStringService.set("test1", "test");
        redisStringService.set("test2", "test");
        Assert.assertFalse(redisStringService.renameNX("test1", "test2"));
        redisStringService.del("test2");
        Assert.assertTrue(redisStringService.renameNX("test1", "test2"));
        Assert.assertFalse(redisStringService.exists("test1"));
        Assert.assertEquals("test", redisStringService.get("test2"));
    }

    @Test
    public void t10Type() {
        Assert.assertEquals(DataType.NONE, redisStringService.type("test"));
        redisStringService.set("test", "test");
        Assert.assertEquals(DataType.STRING, redisStringService.type("test"));
    }

//    ==============================String==============================

    @Test
    public void t11GetSet() {
        redisStringService.set("test", "test1");
        Assert.assertEquals("test1", redisStringService.getSet("test", "test2"));
        Assert.assertEquals("test2", redisStringService.get("test"));
    }

    @Test
    public void t12IncrBy() {
        Assert.assertEquals("3", redisStringService.incrBy("test", 3).toString());
        Assert.assertEquals("5", redisStringService.incrBy("test", 2).toString());
        redisStringService.del("test");
        Assert.assertEquals("1.1", redisStringService.incrBy("test", 1.1D).toString());
        Assert.assertEquals("2.2", redisStringService.incrBy("test", 1.1D).toString());
    }

    @Test
    public void t13Append() {
        redisStringService.del("test");
        Assert.assertEquals("4", redisStringService.append("test", "test").toString());
        Assert.assertEquals("5", redisStringService.append("test", "1").toString());
    }

    @Test
    public void t14GetRange() {
        redisStringService.set("test", "test1test2");
        Assert.assertEquals("test1", redisStringService.getRange("test", 0, 4));
        Assert.assertEquals("test2", redisStringService.getRange("test", -5, -1));
    }

    @Test
    public void t15MSetNX() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("test1", "test");
            put("test2", "test");
        }};
        redisStringService.mSet(map);
        Assert.assertFalse(redisStringService.mSetNX(map));
        map.remove("test1");
        Assert.assertFalse(redisStringService.mSetNX(map));
        map.put("test1", "test");
        redisStringService.del(map.keySet());
        Assert.assertTrue(redisStringService.mSetNX(map));
    }

    @Test
    public void t16SetNX() {
        redisStringService.set("test", "test");
        Assert.assertFalse(redisStringService.setNX("test", "test"));
        redisStringService.del("test");
        Assert.assertTrue(redisStringService.setNX("test", "test"));
    }

    @Test
    public void t17SetRange() {
        redisStringService.setRange("test", 0, "test");
        Assert.assertEquals("test", redisStringService.get("test"));
        redisStringService.setRange("test", 3, "test");
        Assert.assertEquals("testest", redisStringService.get("test"));
    }

    @Test
    public void t18StrLen() {
        redisStringService.set("test", "test");
        Assert.assertEquals("4", redisStringService.strLen("test").toString());
    }

//    ==============================Map==============================

    @Test
    public void t19HSetAndHGet() {
        redisStringService.hSet("test", "test1", "test");
        Assert.assertEquals("test", redisStringService.hGet("test", "test1"));
    }

    @Test
    public void t20HDel() {
        redisStringService.hSet("test", "test1", "test");
        redisStringService.hSet("test", "test2", "test");
        redisStringService.hSet("test", "test3", "test");
        Assert.assertEquals("2", redisStringService.hDel("test", "test1", "test2").toString());
        Assert.assertNull(redisStringService.hGet("test", "test1"));
        Assert.assertNull(redisStringService.hGet("test", "test2"));
        Assert.assertEquals("test", redisStringService.hGet("test", "test3"));
        redisStringService.hSet("test", "test4", "test");
        redisStringService.hSet("test", "test5", "test");
        Assert.assertEquals("2", redisStringService.hDel("test", new ArrayList<String>() {{
            add("test3");
            add("test4");
        }}).toString());
        Assert.assertNull(redisStringService.hGet("test", "test3"));
        Assert.assertNull(redisStringService.hGet("test", "test4"));
        Assert.assertEquals("test", redisStringService.hGet("test", "test5"));
    }

    @Test
    public void t21HMSetAndHMGet() {
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("test1", "test");
            put("test2", "test");
        }};
        redisStringService.hMSet("test", map);
        TreeMap<String, String> treeMap1 = new TreeMap<>(map);
        TreeMap<String, String> treeMap2 = new TreeMap<>(redisStringService.hMGet("test", new ArrayList<String>() {{
            add("test1");
            add("test2");
        }}));
        Assert.assertEquals(treeMap1, treeMap2);
    }

    @Test
    public void t22HExists() {
        Assert.assertFalse(redisStringService.hExists("test", "test1"));
        redisStringService.hSet("test", "test1", "test");
        Assert.assertTrue(redisStringService.hExists("test", "test1"));
    }

    @Test
    public void t23HIncrBy() {
        Assert.assertEquals("2", redisStringService.hIncrBy("test", "test1", 2).toString());
        Assert.assertEquals("2.5", redisStringService.hIncrBy("test", "test1", 0.5).toString());
    }

    @Test
    public void t24HKeysAndHLen() {
        redisStringService.hMSet("test", new HashMap<String, String>() {{
            put("test1", "1");
            put("test2", "2");
        }});
        Assert.assertEquals(new HashSet<String>() {{
            add("test1");
            add("test2");
        }}.toString(), redisStringService.hKeys("test").toString());
        Assert.assertEquals("2", redisStringService.hLen("test").toString());
    }

    @Test
    public void t25HSetNX() {
        redisStringService.hSet("test", "test1", "test");
        Assert.assertFalse(redisStringService.hSetNX("test", "test1", "test"));
        redisStringService.hDel("test", "test1");
        Assert.assertTrue(redisStringService.hSetNX("test", "test1", "test"));
    }

    @Test
    public void t26HVals() {
        List<String> list = new ArrayList<String>() {{
            add("1");
            add("2");
        }};
        Collections.sort(list);
        redisStringService.hMSet("test", new HashMap<String, String>() {{
            put("test1", "1");
            put("test2", "2");
        }});
        List<String> result = redisStringService.hVals("test");
        Collections.sort(result);
        Assert.assertEquals(list.toString(), result.toString());

    }

    @Test
    public void t27HGetAll() {
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("test1", "test1");
            put("test2", "test2");
        }};
        TreeMap<String, String> treeMap1 = new TreeMap<>(map);
        redisStringService.hMSet("test", map);
        Map<String, String> result = redisStringService.hGetAll("test");
        TreeMap<String, String> treeMap2 = new TreeMap<>(result);
        Assert.assertEquals(treeMap1, treeMap2);
    }

//    ==============================list==============================

    @Test
    public void t28LPushAndLPop() {
        Assert.assertEquals("2", redisStringService.lPush("test", "test1", "test2").toString());
        Assert.assertEquals("3", redisStringService.lPush("test", "test3").toString());
        Assert.assertEquals("test3", redisStringService.lPop("test"));
        Assert.assertEquals("test2", redisStringService.lPop("test"));
        Assert.assertEquals("test1", redisStringService.lPop("test"));
        Assert.assertEquals("2", redisStringService.lPush("test", new ArrayList<String>() {{
            add("test1");
            add("test2");
        }}).toString());
        Assert.assertEquals("test2", redisStringService.lPop("test"));
        Assert.assertEquals("test1", redisStringService.lPop("test"));
    }

    @Test
    public void t29LIndex() {
        redisStringService.lPush("test", "test1", "test2");
        Assert.assertEquals("test2", redisStringService.lIndex("test", 0));
        Assert.assertEquals("test1", redisStringService.lIndex("test", 1));
        Assert.assertEquals("test1", redisStringService.lIndex("test", -1));
        Assert.assertEquals("test2", redisStringService.lIndex("test", -2));
    }

    @Test
    public void t30LPushX() {
        Assert.assertEquals("0", redisStringService.lPushX("test", "test").toString());
        redisStringService.lPush("test", "test");
        Assert.assertEquals("2", redisStringService.lPushX("test", "test").toString());
        Assert.assertEquals("test", redisStringService.lPop("test"));
        Assert.assertEquals("test", redisStringService.lPop("test"));
    }

    @Test
    public void t31LInsert() {
        Assert.assertEquals("0", redisStringService.lInsert("test", RedisListCommands.Position.AFTER, "test1", "test").toString());
        redisStringService.lPush("test", "test2", "test3");
        Assert.assertEquals("-1", redisStringService.lInsert("test", RedisListCommands.Position.AFTER, "test1", "test").toString());
        Assert.assertEquals("3", redisStringService.lInsert("test", RedisListCommands.Position.AFTER, "test2", "test1").toString());
        Assert.assertEquals("4", redisStringService.lInsert("test", RedisListCommands.Position.BEFORE, "test3", "test4").toString());
        Assert.assertEquals("test4", redisStringService.lPop("test"));
        Assert.assertEquals("test3", redisStringService.lPop("test"));
        Assert.assertEquals("test2", redisStringService.lPop("test"));
        Assert.assertEquals("test1", redisStringService.lPop("test"));
    }

    @Test
    public void t32LLenAndLRange() {
        Assert.assertEquals("0", redisStringService.lLen("test").toString());
        redisStringService.lPush("test", "test1", "test2");
        Assert.assertEquals("2", redisStringService.lLen("test").toString());
        Assert.assertEquals(new ArrayList<String>() {{
            add("test2");
            add("test1");
        }}, redisStringService.lRange("test", 0, 1));
        Assert.assertEquals(new ArrayList<String>() {{
            add("test1");
        }}, redisStringService.lRange("test", -1, -1));
    }

    @Test
    public void t33LRem() {
        redisStringService.lPush("test", new ArrayList<String>() {{
            add("test1");
            add("test");
            add("test");
            add("test2");
            add("test");
            add("test");
            add("test");
            add("test3");
            add("test");
            add("test");
            add("test");
            add("test");
            add("test4");
        }});
        Assert.assertEquals("4", redisStringService.lRem("test", 4, "test").toString());
        Assert.assertEquals(new ArrayList<String>() {{
            add("test4");
            add("test3");
            add("test");
            add("test");
            add("test");
            add("test2");
            add("test");
            add("test");
            add("test1");
        }}.toString(), redisStringService.lRange("test", 0, -1).toString());
        redisStringService.lRem("test", -2, "test");
        Assert.assertEquals(new ArrayList<String>() {{
            add("test4");
            add("test3");
            add("test");
            add("test");
            add("test");
            add("test2");
            add("test1");
        }}.toString(), redisStringService.lRange("test", 0, -1).toString());
        redisStringService.lRem("test", 0, "test");
        Assert.assertEquals(new ArrayList<String>() {{
            add("test4");
            add("test3");
            add("test2");
            add("test1");
        }}.toString(), redisStringService.lRange("test", 0, -1).toString());
    }

    @Test
    public void t34RPushAndRPop() {
        Assert.assertEquals("2", redisStringService.rPush("test", "test1", "test2").toString());
        Assert.assertEquals("3", redisStringService.rPush("test", "test3").toString());
        Assert.assertEquals("test3", redisStringService.rPop("test"));
        Assert.assertEquals("test2", redisStringService.rPop("test"));
        Assert.assertEquals("test1", redisStringService.rPop("test"));
        Assert.assertEquals("2", redisStringService.rPush("test", new ArrayList<String>() {{
            add("test1");
            add("test2");
        }}).toString());
        Assert.assertEquals("test2", redisStringService.rPop("test"));
        Assert.assertEquals("test1", redisStringService.rPop("test"));
    }

    @Test
    public void t35RPushX() {
        Assert.assertEquals("0", redisStringService.rPushX("test", "test").toString());
        redisStringService.rPush("test", "test");
        Assert.assertEquals("2", redisStringService.rPushX("test", "test").toString());
        Assert.assertEquals(new ArrayList<String>() {{
            add("test");
            add("test");
        }}, redisStringService.lRange("test", 0, -1));
    }

    @Test
    public void t36RPopLPush() {
        redisStringService.lPush("test1", "test1");
        redisStringService.lPush("test2", "test2");
        Assert.assertEquals("test1", redisStringService.rPopLPush("test1", "test2"));
        Assert.assertEquals(new ArrayList<String>() {{
            add("test1");
            add("test2");
        }}.toString(), redisStringService.lRange("test2", 0, -1).toString());
        redisStringService.rPopLPush("test2", "test2");
        Assert.assertEquals(new ArrayList<String>() {{
            add("test2");
            add("test1");
        }}.toString(), redisStringService.lRange("test2", 0, -1).toString());
    }

    @Test
    public void t37LSet() {
        redisStringService.lPush("test", "test1", "test2", "test3");
        redisStringService.lSet("test", 1, "test");
        Assert.assertEquals(new ArrayList<String>() {{
            add("test3");
            add("test");
            add("test1");
        }}, redisStringService.lRange("test", 0, -1));
    }

    @Test
    public void t38LTrim() {
        redisStringService.lPush("test", "test1", "test2", "test3", "test4");
        redisStringService.lTrim("test", 0, 2);
        Assert.assertEquals(new ArrayList<String>() {{
            add("test4");
            add("test3");
            add("test2");
        }}, redisStringService.lRange("test", 0, -1));
        redisStringService.lTrim("test", -1, -1);
        Assert.assertEquals(new ArrayList<String>() {{
            add("test2");
        }}.toString(), redisStringService.lRange("test", 0, -1).toString());
    }

//    ==============================set==============================

    @Test
    public void t39SAddAndSMembers() {
        Assert.assertEquals("2", redisStringService.sAdd("test", "test1", "test2").toString());
        TreeSet<String> treeSet1 = new TreeSet<String>() {{
            add("test1");
            add("test2");
        }};
        TreeSet<String> treeSet2 = new TreeSet<String>() {{
            addAll(redisStringService.sMembers("test"));
        }};
        Assert.assertEquals(treeSet1.toString(), treeSet2.toString());
        Assert.assertEquals("1", redisStringService.sAdd("test", new ArrayList<String>() {{
            add("test2");
            add("test3");
        }}).toString());
        TreeSet<String> treeSet3 = new TreeSet<String>() {{
            add("test1");
            add("test2");
            add("test3");
        }};
        TreeSet<String> treeSet4 = new TreeSet<String>() {{
            addAll(redisStringService.sMembers("test"));
        }};
        Assert.assertEquals(treeSet3.toString(), treeSet4.toString());
    }

    @Test
    public void t40SDiff() {
        redisStringService.sAdd("test1", "1", "2", "3", "4");
        redisStringService.sAdd("test2", "2", "5", "6", "7");
        redisStringService.sAdd("test3", "3", "5", "6", "7");
        TreeSet<String> treeSet1 = new TreeSet<String>() {{
            add("1");
            add("4");
        }};
        TreeSet<String> treeSet2 = new TreeSet<>(redisStringService.sDiff("test1", "test2", "test3"));
        Assert.assertEquals(treeSet1.toString(), treeSet2.toString());
        Assert.assertEquals("2", redisStringService.sDiffStore("test4", "test1", "test2", "test3").toString());
        TreeSet<String> treeSet4 = new TreeSet<>(redisStringService.sMembers("test4"));
        Assert.assertEquals(treeSet1.toString(), treeSet4.toString());
    }

    @Test
    public void t41SInter() {
        redisStringService.sAdd("test1", "1", "2", "3", "4");
        redisStringService.sAdd("test2", "2", "3", "4", "5");
        redisStringService.sAdd("test3", "3", "4", "5", "6");
        TreeSet<String> treeSet1 = new TreeSet<String>() {{
            add("3");
            add("4");
        }};
        TreeSet<String> treeSet2 = new TreeSet<>(redisStringService.sInter("test1", "test2", "test3"));
        Assert.assertEquals(treeSet1.toString(), treeSet2.toString());
        Assert.assertEquals("2", redisStringService.sInterStore("test4", "test1", "test2", "test3").toString());
        TreeSet<String> treeSet4 = new TreeSet<>(redisStringService.sMembers("test4"));
        Assert.assertEquals(treeSet1.toString(), treeSet4.toString());
    }

    @Test
    public void t42SIsMemberAndSMove() {
        redisStringService.sAdd("test1", "0", "1", "2");
        Assert.assertTrue(redisStringService.sIsMember("test1", "2"));
        Assert.assertFalse(redisStringService.sIsMember("test1", "3"));
        redisStringService.sAdd("test2", "1", "3");
        Assert.assertFalse(redisStringService.sMove("test1", "test2", "3"));
        // 此处在集群中操作成功后返回 false，单机为true
//        Assert.assertTrue(redisStringService.sMove("test1", "test2", "1"));
//        Assert.assertFalse(redisStringService.sIsMember("test1", "1"));
        Assert.assertTrue(redisStringService.sMove("test1", "test2", "2"));
        Assert.assertFalse(redisStringService.sIsMember("test1", "2"));
        Assert.assertTrue(redisStringService.sIsMember("test1", "0"));
        redisStringService.sAdd("test1", "1");
        redisStringService.del("test2");
        redisStringService.sAdd("test2", "3");
        Assert.assertTrue(redisStringService.sMove("test1", "test2", "1"));
        TreeSet<String> treeSet1 = new TreeSet<String>() {{
            add("1");
            add("3");
        }};
        TreeSet<String> treeSet2 = new TreeSet<>(redisStringService.sMembers("test2"));
        Assert.assertEquals(treeSet1.toString(), treeSet2.toString());
    }

    @Test
    public void t43SRandMember() {
        List<String> list = new ArrayList<String>() {{
            add("1");
            add("2");
            add("3");
        }};
        redisStringService.sAdd("test", list);
        Assert.assertTrue(list.contains(redisStringService.sRandMember("test")));
        Set<String> resultSet1 = redisStringService.sRandMember("test", 2);
        Set<String> resultSet2 = redisStringService.sRandMember("test", 4);
        Assert.assertEquals("2", String.valueOf(resultSet1.size()));
        Assert.assertEquals("3", String.valueOf(resultSet2.size()));
        for (String value : resultSet1) {
            Assert.assertTrue(list.contains(value));
        }
        List<String> resultList = redisStringService.sRandMembers("test", 10);
        Assert.assertEquals("10", String.valueOf(resultList.size()));
        for (String value : resultList) {
            Assert.assertTrue(list.contains(value));
        }
    }

    @Test
    public void t44SRemAndSPopAndSCard() {
        TreeSet<String> treeSet = new TreeSet<String>() {{
            add("1");
            add("2");
            add("3");
            add("4");
            add("5");
            add("6");
        }};
        redisStringService.sAdd("test", treeSet);
        Assert.assertEquals("2", String.valueOf(redisStringService.sRem("test", "6", "5")));
        treeSet.remove("5");
        treeSet.remove("6");
        Assert.assertEquals(treeSet.toString(), redisStringService.sMembers("test").toString());
        String delMember = redisStringService.sPop("test");
        Assert.assertTrue(treeSet.contains(delMember));
        treeSet.remove(delMember);
        Assert.assertEquals(treeSet.toString(), redisStringService.sMembers("test").toString());
        Assert.assertEquals("0", String.valueOf(redisStringService.sCard("test1")));
        Assert.assertEquals("3", String.valueOf(redisStringService.sCard("test")));
    }

    @Test
    public void t45SUnion() {
        redisStringService.sAdd("test1", "1", "2");
        redisStringService.sAdd("test2", "2", "3");
        redisStringService.sAdd("test3", "3", "4");
        TreeSet<String> treeSet1 = new TreeSet<String>() {{
            add("1");
            add("2");
            add("3");
            add("4");
        }};
        TreeSet<String> treeSet2 = new TreeSet<>(redisStringService.sUnion("test1", "test2", "test3"));
        Assert.assertEquals(treeSet1.toString(), treeSet2.toString());
        Assert.assertEquals("4", redisStringService.sUnionStore("test4", "test1", "test2", "test3").toString());
        TreeSet<String> treeSet4 = new TreeSet<>(redisStringService.sMembers("test4"));
        Assert.assertEquals(treeSet1.toString(), treeSet4.toString());
    }

//    ==============================有序集合==============================

    @Test
    public void t46ZAddAndZRange() {
        Assert.assertTrue(redisStringService.zAdd("test", 1.1, "test1"));
        Assert.assertFalse(redisStringService.zAdd("test", 1.1, "test1"));
        Assert.assertArrayEquals(new String[]{"test1"}, redisStringService.zRange("test", 0, 1).toArray());
        Assert.assertTrue(redisStringService.zAdd("test", 1.0, "test2"));
        Assert.assertTrue(redisStringService.zAdd("test", 1.2, "test3"));
        Assert.assertTrue(redisStringService.zAdd("test", 1.1, "test4"));
        Assert.assertArrayEquals(new String[]{"test2", "test1", "test4", "test3"}, redisStringService.zRange("test", 0, -1).toArray());
        Assert.assertArrayEquals(new String[]{}, redisStringService.zRange("test", 10, 10).toArray());
        Assert.assertArrayEquals(new String[]{"test2", "test1", "test4", "test3"}, redisStringService.zRange("test", 0, 10).toArray());

        Assert.assertEquals("2", redisStringService.zAdd("test1", new HashSet<ZSetOperations.TypedTuple<String>>() {{
            add(new DefaultTypedTuple<>("test1", 1.1));
            add(new DefaultTypedTuple<>("test3", 1.2));
        }}).toString());
        Assert.assertArrayEquals(new String[]{"test1", "test3"}, redisStringService.zRange("test1", 0, -1).toArray());
        Assert.assertEquals("1", redisStringService.zAdd("test1", new HashSet<ZSetOperations.TypedTuple<String>>() {{
            add(new DefaultTypedTuple<>("test2", 1.0));
            add(new DefaultTypedTuple<>("test3", 1.2));
        }}).toString());
        Assert.assertArrayEquals(new String[]{"test2", "test1", "test3"}, redisStringService.zRange("test1", 0, -1).toArray());
    }

    @Test
    public void t47ZScore() {
        redisStringService.zAdd("test", 1.1, "test1");
        Assert.assertEquals("1.1", redisStringService.zScore("test", "test1").toString());
    }

    @Test
    public void t48ZIncrBy() {
        redisStringService.zIncrBy("test", 1.1, "test1");
        Assert.assertEquals("1.1", redisStringService.zScore("test", "test1").toString());
        Assert.assertEquals("2.2", redisStringService.zIncrBy("test", 1.1, "test1").toString());
    }

    @Test
    public void t49ZCard() {
        redisStringService.zAdd("test", 1.1, "test1");
        Assert.assertEquals("1", redisStringService.zCard("test").toString());
        redisStringService.zAdd("test", 1.1, "test2");
        Assert.assertEquals("2", redisStringService.zCard("test").toString());
    }

    @Test
    public void t50ZCount() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        redisStringService.zAdd("test", 1.3, "test4");
        Assert.assertEquals("2", redisStringService.zCount("test", 1.1, 1.2).toString());
    }

    @Test
    public void t51ZRevRange() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        Assert.assertArrayEquals(new String[]{"test3", "test2", "test1"}, redisStringService.zRevRange("test", 0, -1).toArray());
    }

    @Test
    public void t52ZRangeByScore() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        redisStringService.zAdd("test", 1.3, "test4");
        redisStringService.zAdd("test", 1.4, "test5");
        Assert.assertArrayEquals(new String[]{"test2", "test3"}, redisStringService.zRangeByScore("test", 1.1, 1.2).toArray());

        Assert.assertArrayEquals(new String[]{"test3", "test4"}, redisStringService.zRangeByScore("test", 1.1, 1.3, 1, 2).toArray());
    }

    @Test
    public void t53ZRevRangeByScore() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        redisStringService.zAdd("test", 1.3, "test4");
        redisStringService.zAdd("test", 1.4, "test5");
        Assert.assertArrayEquals(new String[]{"test3", "test2"}, redisStringService.zRevRangeByScore("test", 1.1, 1.2).toArray());

        Assert.assertArrayEquals(new String[]{"test3", "test2"}, redisStringService.zRevRangeByScore("test", 1.1, 1.3, 1, 2).toArray());
    }

    @Test
    public void t54ZRank() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        Assert.assertEquals("1", redisStringService.zRank("test", "test2").toString());
    }

    @Test
    public void t55ZRevRank() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        Assert.assertEquals("1", redisStringService.zRevRank("test", "test2").toString());
    }

    @Test
    public void t56ZRem() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        redisStringService.zAdd("test", 1.3, "test4");
        redisStringService.zAdd("test", 1.4, "test5");
        Assert.assertEquals("2", redisStringService.zRem("test", "test2", "test3").toString());
        Assert.assertEquals("2", redisStringService.zRem("test", new ArrayList<String>() {{
            add("test4");
            add("test5");
        }}).toString());
        Assert.assertEquals("1", redisStringService.zCard("test").toString());
    }

    @Test
    public void t57ZRemRangeByRank() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        redisStringService.zAdd("test", 1.3, "test4");
        Assert.assertEquals("2", redisStringService.zRemRangeByRank("test", 1, 2).toString());
        Assert.assertArrayEquals(new String[]{"test1", "test4"}, redisStringService.zRange("test", 0, -1).toArray());
    }

    @Test
    public void t58ZRemRangeByScore() {
        redisStringService.zAdd("test", 1.0, "test1");
        redisStringService.zAdd("test", 1.1, "test2");
        redisStringService.zAdd("test", 1.2, "test3");
        redisStringService.zAdd("test", 1.3, "test4");
        Assert.assertEquals("2", redisStringService.zRemRangeByScore("test", 1.1, 1.2).toString());
        Assert.assertArrayEquals(new String[]{"test1", "test4"}, redisStringService.zRange("test", 0, -1).toArray());
    }

//    ==============================补充方法==============================

    @Test
    public void t01IncrByTimeOut() {
        long i = redisStringService.incrBy("test", 1, 3);
        Assert.assertEquals(i, 1L);
        i = redisStringService.incrBy("test", 1, 3);
        Assert.assertEquals(i, 2L);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        i = redisStringService.incrBy("test", 1, 3);
        Assert.assertEquals(i, 1);
    }

    @Test
    public void t02SetNx() throws InterruptedException {
        redisStringService.set("test", "test");
        boolean result1 = redisStringService.setNX("test", "test", 3);
        Assert.assertFalse(result1);
        redisStringService.del("test");
        boolean result2 = redisStringService.setNX("test", "test", 3);
        Assert.assertTrue(result2);
        long expire = redisStringService.ttl("test");
        Assert.assertTrue(expire > 0);

    }

}
