package com.minister.component.redis.service;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

/**
 * RedisStringService
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 12:01
 */
public interface RedisStringService {

    /**
     * 删除给定的一个或多个 key
     *
     * @param key 键数组
     */
    public long del(String... key);

    /**
     * 删除给定的一个或多个 key
     *
     * @param keys 键集合
     */
    public long del(Collection<String> keys);

    /**
     * 检查给定 key 是否存在
     *
     * @param key 键
     * @return true:存在 / false:不存在
     */
    public boolean exists(String key);

    /**
     * 为给定 key 设置生存时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return true:设置成功 / false:设置失败
     */
    public boolean expire(String key, long time);

    /**
     * 为给定 key 设置生存时间
     *
     * @param key  键
     * @param date 过期时间
     * @return true:设置成功 / false:设置失败
     */
    public boolean expireAt(String key, Date date);

    /**
     * 返回给定 key 的剩余生存时间
     *
     * @param key 键
     * @return 剩余过期时间(秒) / 0:代表为永久有效 / -1:永久 / -2:key不存在
     */
    public long ttl(String key);

    /**
     * 查找所有符合给定模式 pattern 的 key
     * · KEYS * 匹配数据库中所有key
     * · KEYS h?llo 匹配hello，hallo和hxllo等
     * · KEYS h*llo 匹配hllo和heeeeello等
     * · KEYS h[ae]llo 匹配hello和hallo，但不匹配 hillo 。
     * · 特殊符号用 \ 隔开
     *
     * @param pattern 匹配模式
     * @return 符合条件的key集合
     */
    public Set<String> keys(String pattern);

    /**
     * 移除给定 key 的生存时间，使其变为永久
     *
     * @param key 键
     * @return true:移除成功 / false:移除失败
     */
    public boolean persist(String key);

    /**
     * 将 oldKey 改名为 newKey
     * 当 newKey 已经存在时，RENAME 命令将覆盖旧值
     *
     * @param oldKey 旧键
     * @param newKey 新键
     */
    public void rename(String oldKey, String newKey);

    /**
     * 当且仅当 newKey 不存在时，将 oldKey 改名为 newKey
     *
     * @param oldKey 旧键
     * @param newKey 新键
     * @return true:改名成功 / false:改名失败
     */
    public boolean renameNX(String oldKey, String newKey);

    /**
     * 返回 key 所储存的值的类型
     *
     * @param key 键
     * @return NONE:key不存在 / STRING:字符串 / LIST:列表 / SET:集合 / ZSET:有序集 / HASH:哈希表
     */
    public DataType type(String key);

//    ==============================String==============================

    /**
     * 返回与键 key 相关联的字符串值
     *
     * @param key 键
     * @return value
     */
    public String get(String key);

    /**
     * 将键 key 的值设为 value ，并返回键 key 在被设置之前的旧值
     *
     * @param key   键
     * @param value 值
     * @return 旧值
     */
    public String getSet(String key, String value);

    /**
     * 为键 key 储存的数字值加上增量 increment
     * 如果键 key 不存在，那么键 key 的值会先被初始化为 0 ，然后再执行
     *
     * @param key       键
     * @param increment 增量
     * @return 在加上增量 increment 之后，键 key 的值
     */
    public Integer incrBy(String key, int increment);

    /**
     * 为键 key 储存的数字值加上增量 increment, 并将键 key 的生存时间设置为 time 秒钟
     * 如果键 key 不存在，那么键 key 的值会先被初始化为 0 ，然后再执行
     *
     * @param key       键
     * @param increment 增量
     * @param time      过期时间，单位s
     * @return 在加上增量 increment 之后，键 key 的值
     */
    public Integer incrBy(String key, int increment, long time);

    /**
     * 为键 key 储存的数字值加上增量 increment
     * 如果键 key 不存在，那么键 key 的值会先被初始化为 0 ，然后再执行
     *
     * @param key       键
     * @param increment 增量
     * @return 在加上增量 increment 之后，键 key 的值
     */
    public Double incrBy(String key, double increment);

    /**
     * 为键 key 储存的数字值加上增量 increment, 并将键 key 的生存时间设置为 time 秒钟
     * 如果键 key 不存在，那么键 key 的值会先被初始化为 0 ，然后再执行
     *
     * @param key       键
     * @param increment 增量
     * @param time      过期时间，单位s
     * @return 在加上增量 increment 之后，键 key 的值
     */
    public Double incrBy(String key, double increment, long time);

    /**
     * 如果键 key 已经存在并且它的值是一个字符串，APPEND 命令将把 value 追加到键 key 现有值的末尾
     * 如果 key 不存在，就像执行 SET key value 一样
     *
     * @param key   键
     * @param value 增加值
     * @return 追加 value 之后，键 key 的值的长度
     */
    public Integer append(String key, String value);

    /**
     * 返回键 key 储存的字符串值的指定部分，字符串的截取范围由 start 和 end 两个偏移量决定 (包括 start 和 end 在内)
     * 正数偏移量， 0 表示第一个字符， 1表示第二个字符，以此类推
     * 负数偏移量表示从字符串的末尾开始计数，-1 表示最后一个字符，-2 表示倒数第二个字符，以此类推
     * start < end
     *
     * @param key   键
     * @param start 起始偏移量
     * @param end   结束偏移量
     * @return 字符串值的指定部分
     */
    public String getRange(String key, int start, int end);

    /**
     * 返回给定的一个或多个字符串键的值
     *
     * @param keys 集合
     * @return 键值对应关系
     */
    public Map<String, String> mGet(Collection<String> keys);

    /**
     * 同时为多个键设置值
     *
     * @param map 键值对应关系
     */
    public void mSet(Map<String, String> map);

    /**
     * 当且仅当所有给定键都不存在时，为所有给定键设置值
     *
     * @param map 键值对应关系
     * @return true:设置成功 / false:设置失败
     */
    public boolean mSetNX(Map<String, String> map);

    /**
     * 将 value 关联到 key
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value);

    /**
     * 将键 key 的值设置为 value ，并将键 key 的生存时间设置为 time 秒钟
     *
     * @param key   键
     * @param value 值
     * @param time  生存时间(秒)
     */
    public void set(String key, String value, long time);

    /**
     * 只在键 key 不存在的情况下，将键 key 的值设置为 value
     *
     * @param key   键
     * @param value 值
     * @return true:设置成功 / false:设置失败
     */
    public boolean setNX(String key, String value);

    /**
     * 只在键 key 不存在的情况下，将键 key 的值设置为 value，如果成功设置有效时间
     *
     * @param key   键
     * @param value 值
     * @param time  生存时间(秒)
     * @return true:设置成功 / false:设置失败
     */
    public boolean setNX(String key, String value, long time);

    /**
     * 从偏移量 offset 开始，用 value 参数覆写(overwrite)键 key 储存的字符串值
     * 不存在的键 key 抛出异常
     *
     * @param key    键
     * @param offset 偏移量
     * @param value  覆写值值
     */
    public void setRange(String key, int offset, String value);

    /**
     * 返回键 key 储存的字符串值的长度
     *
     * @param key 键
     * @return 键 key 储存的字符串值的长度
     */
    public Integer strLen(String key);

//    ==============================Map==============================

    /**
     * 返回哈希表中给定域的值
     *
     * @param key   键
     * @param field 域
     * @return 哈希表中给定域的值
     */
    public String hGet(String key, String field);

    /**
     * 检查给定域 field 是否存在于哈希表 hash 当中
     *
     * @param key   键
     * @param field 域
     * @return true:存在 / false:不存在
     */
    public boolean hExists(String key, String field);

    /**
     * 为哈希表 key 中的域 field 的值加上增量 increment
     * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令
     * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0
     *
     * @param key       键
     * @param field     域
     * @param increment 增量
     * @return 在加上增量 increment 之后，键 key 的值
     */
    public Integer hIncrBy(String key, String field, int increment);

    /**
     * 为哈希表 key 中的域 field 的值加上增量 increment
     * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令
     * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0
     *
     * @param key       键
     * @param field     域
     * @param increment 增量
     * @return 在加上增量 increment 之后，键 key 的值
     */
    public Double hIncrBy(String key, String field, double increment);

    /**
     * 返回哈希表 key 中的所有域
     *
     * @param key 键
     * @return 哈希表 key 中的所有域
     */
    public Set<String> hKeys(String key);

    /**
     * 返回哈希表 key 中域的数量
     *
     * @param key 键
     * @return 当 key 不存在时，返回 0
     */
    public Integer hLen(String key);

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中
     *
     * @param key 键
     * @param map Map<域><String/值>
     */
    public void hMSet(String key, Map<String, String> map);

    /**
     * 返回哈希表 key 中，一个或多个给定域的值
     *
     * @param key    键
     * @param fields 域集合
     * @return Map<域><String/值>
     */
    public Map<String, String> hMGet(String key, Collection<String> fields);

    /**
     * 将哈希表 hash 中域 field 的值设置为 value
     *
     * @param key   键
     * @param field 域
     * @param value 值
     */
    public void hSet(String key, String field, String value);

    /**
     * 当且仅当域 field 尚未存在于哈希表的情况下， 将它的值设置为 value
     *
     * @param key   键
     * @param field 域
     * @param value 值
     * @return true:设置成功 / false:设置失败
     */
    public boolean hSetNX(String key, String field, String value);

    /**
     * 返回哈希表 key 中所有域的值
     *
     * @param key 键
     * @return 域的值集合
     */
    public List<String> hVals(String key);

    /**
     * 删除哈希表 key 中的一个或多个指定域
     *
     * @param key   键
     * @param field 域集合
     * @return 成功删除域数量
     */
    public Integer hDel(String key, String... field);

    /**
     * 删除哈希表 key 中的一个或多个指定域
     *
     * @param key    键
     * @param fields 域集合
     * @return 成功删除域数量
     */
    public Integer hDel(String key, Collection<String> fields);

    /**
     * 返回哈希表 key 中，所有的域和值
     *
     * @param key 键
     * @return Map<域><String/值>
     */
    public Map<String, String> hGetAll(String key);

//    ==============================list==============================

    /**
     * 返回列表 key 中，下标为 index 的元素
     * 以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推
     * 以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推
     *
     * @param key   键
     * @param index 下标
     * @return 下标为 index 的元素
     */
    public String lIndex(String key, int index);

    /**
     * 移除并返回列表 key 的头元素
     *
     * @param key 键
     * @return 列表 key 的头元素
     */
    public String lPop(String key);

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头
     *
     * @param key   键
     * @param value 一个或多个值
     * @return 列表的长度
     */
    public Integer lPush(String key, String... value);

    /**
     * 将一个或多个值 value 插入到列表 key 的表头
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头
     *
     * @param key    键
     * @param values 一个或多个值
     * @return 列表的长度
     */
    public Integer lPush(String key, Collection<String> values);

    /**
     * 将值 value 插入到列表 key 的表头
     * 当 key 不存在时， LPUSHX 命令什么也不做
     *
     * @param key   键
     * @param value 值
     * @return 表的长度
     */
    public Integer lPushX(String key, String value);

    /**
     * 将值 value 插入到列表 key 当中，位于值 pivot 之前或之后（前表示l方向，后表示r方向）
     * 当 pivot 不存在于列表 key 时，不执行任何操作
     * 当 key 不存在时， key 被视为空列表，不执行任何操作
     *
     * @param key      键
     * @param position Position.BEFORE 之前 / Position.AFTER 之后
     * @param pivot    查找值
     * @param value    值
     * @return 列表的长度:插入操作成功 / -1:没有找到 pivot / 0:key 不存在或为空列表
     */
    public Integer lInsert(String key, Position position, String pivot, String value);

    /**
     * 返回列表 key 的长度
     * 如果 key 不存在，则 key 被解释为一个空列表，返回 0
     *
     * @param key 键
     * @return 列表 key 的长度
     */
    public Integer lLen(String key);

    /**
     * 回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定
     * 以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推
     * 以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推
     *
     * @param key   键
     * @param start 起始量
     * @param end   结束量
     * @return 列表 key 中指定区间内的元素
     */
    public List<String> lRange(String key, int start, int end);

    /**
     * 据参数 count 的值，移除列表中与参数 value 相等的元素
     * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count
     * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值
     * count = 0 : 移除表中所有与 value 相等的值
     *
     * @param key   键
     * @param count 量
     * @param value 值
     * @return 被移除元素的数量
     */
    public Integer lRem(String key, int count, String value);

    /**
     * 移除并返回列表 key 的尾元素
     *
     * @param key 键
     * @return 列表 key 的尾元素
     */
    public String rPop(String key);

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表尾
     *
     * @param key   键
     * @param value 一个或多个值
     * @return 表的长度
     */
    public Integer rPush(String key, String... value);

    /**
     * 将一个或多个值 value 插入到列表 key 的表尾
     * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表尾
     *
     * @param key    键
     * @param values 一个或多个值
     * @return 表的长度
     */
    public Integer rPush(String key, Collection<String> values);

    /**
     * 将值 value 插入到列表 key 的表尾
     * 当 key 不存在时，RPUSHX 命令什么也不做
     *
     * @param key   键
     * @param value 值
     * @return 表的长度
     */
    public Integer rPushX(String key, String value);

    /**
     * 将列表 source 中的最后一个元素弹出，并返回，将 source 弹出的元素插入到列表 destination ，作为 destination 列表的的头元素
     * 如果 source 和 destination 相同，则列表中的表尾元素被移动到表头
     * 集群时无法从空列表中弹出元素
     *
     * @param sourceKey      源列表键
     * @param destinationKey 目标列表键
     * @return 列表 source 中的最后一个元素
     */
    public String rPopLPush(String sourceKey, String destinationKey);

    /**
     * 将列表 key 下标为 index 的元素的值设置为 value
     *
     * @param key   键
     * @param index 下标
     * @param value 值
     */
    public void lSet(String key, int index, String value);

    /**
     * 对一个列表进行修剪（让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除）
     * 以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推
     * 以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推
     *
     * @param key   键
     * @param start 起始下标
     * @param end   结束下标
     */
    public void lTrim(String key, int start, int end);

//    ==============================set==============================

    /**
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略
     *
     * @param key   键
     * @param value 一个或多个值
     * @return 被添加到集合中的新元素的数量
     */
    public Integer sAdd(String key, String... value);

    /**
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略
     *
     * @param key    键
     * @param values 一个或多个值
     * @return 被添加到集合中的新元素的数量
     */
    public Integer sAdd(String key, Collection<String> values);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合之间的差集
     * 不存在的 key 被视为空集
     *
     * @param key        给定集合键
     * @param compareKey 一个或多个对比集合键
     * @return 一个包含差集成员的列表
     */
    public Set<String> sDiff(String key, String... compareKey);

    /**
     * 将源集合和一个或多个集合进行对比求出差集，并返回差集所有元素
     * 不存在的 key 被视为空集
     *
     * @param key         源集合键
     * @param compareKeys 一个或多个对比集合键
     * @return 差集所有元素
     */
    public Set<String> sDiff(String key, Collection<String> compareKeys);

    /**
     * 将源集合和一个或多个集合进行对比求出差集，返回差集的元素数量，并将差集所有元素保存到 destination 集合
     *
     * @param destinationKey 存储集合键
     * @param key            源集合键
     * @param compareKey     一个或多个对比集合键
     * @return 差集的元素数量
     */
    public Integer sDiffStore(String destinationKey, String key, String... compareKey);

    /**
     * 将源集合和一个或多个集合进行对比求出差集，返回差集的元素数量，并将差集所有元素保存到 destination 集合
     *
     * @param destinationKey 存储集合键
     * @param key            源集合键
     * @param compareKeys    一个或多个对比集合键
     * @return 差集的元素数量
     */
    public Integer sDiffStore(String destinationKey, String key, Collection<String> compareKeys);

    /**
     * 将源集合和一个或多个集合进行对比求出交集，并返回交集所有元素
     *
     * @param key        源集合键
     * @param compareKey 一个或多个对比集合键
     * @return 交集所有元素
     */
    public Set<String> sInter(String key, String... compareKey);

    /**
     * 将源集合和一个或多个集合进行对比求出交集，并返回交集所有元素
     *
     * @param key         源集合键
     * @param compareKeys 一个或多个对比集合键
     * @return 交集所有元素
     */
    public Set<String> sInter(String key, Collection<String> compareKeys);

    /**
     * 将源集合和一个或多个集合进行对比求出交集，返回交集的元素数量，并将交集所有元素保存到 destination 集合
     *
     * @param destinationKey 存储集合键
     * @param key            源集合键
     * @param compareKey     一个或多个对比集合键
     * @return 交集的元素数量
     */
    public Integer sInterStore(String destinationKey, String key, String... compareKey);

    /**
     * 将源集合和一个或多个集合进行对比求出交集，返回交集的元素数量，并将交集所有元素保存到 destination 集合
     *
     * @param destinationKey 存储集合键
     * @param key            源集合键
     * @param compareKeys    一个或多个对比集合键
     * @return 交集的元素数量
     */
    public Integer sInterStore(String destinationKey, String key, Collection<String> compareKeys);

    /**
     * 判断 member 元素是否集合 key 的成员
     *
     * @param key    键
     * @param member 判断值
     * @return true:是 / false:否
     */
    public Boolean sIsMember(String key, String member);

    /**
     * 返回集合 key 中的所有成员
     *
     * @param key 键
     * @return 集合中的所有成员
     */
    public Set<String> sMembers(String key);

    /**
     * (此方法慎用，集群和单机返回不一致)将 member 元素从 source 集合移动到 destination 集合
     *
     * @param sourceKey      源集合key
     * @param destinationKey 目标集合key
     * @param member         值
     * @return true:操作成功 / false:操作失败
     */
    public boolean sMove(String sourceKey, String destinationKey, String member);

    /**
     * 返回集合中的一个随机元素
     *
     * @param key 键
     * @return 集合中的一个随机元素
     */
    public String sRandMember(String key);

    /**
     * 返回一个小于等于 count 个随机元素的去重数组
     *
     * @param key   键
     * @param count 数组数量
     * @return 小于等于 count 个元素的去重数组
     */
    public Set<String> sRandMember(String key, int count);

    /**
     * 返回一个包含 count 个随机元素的数组
     *
     * @param key   键
     * @param count 数组数量
     * @return 包含 count 个元素的数组
     */
    public List<String> sRandMembers(String key, int count);

    /**
     * 除集合 key 中的一个或多个 member 元素
     *
     * @param key   键
     * @param value 一个或多个值
     * @return 被成功移除的元素的数量
     */
    public Integer sRem(String key, String... value);

    /**
     * 移除并返回集合中的一个随机元素
     *
     * @param key 键
     * @return 被移除的随机元素
     */
    public String sPop(String key);

    /**
     * 返回集合 key 的基数
     *
     * @param key 键
     * @return 集合的基数。当 key 不存在时，返回 0
     */
    public Integer sCard(String key);

    /**
     * 将源集合和一个或多个集合进行对比求出并集，并返回并集所有元素
     *
     * @param key        源集合键
     * @param compareKey 一个或多个对比集合键
     * @return 并集所有元素
     */
    public Set<String> sUnion(String key, String... compareKey);

    /**
     * 将源集合和一个或多个集合进行对比求出并集，并返回并集所有元素
     *
     * @param key         源集合键
     * @param compareKeys 一个或多个对比集合键
     * @return 并集所有元素
     */
    public Set<String> sUnion(String key, Collection<String> compareKeys);

    /**
     * 将源集合和一个或多个集合进行对比求出并集，返回并集的元素数量，并将并集所有元素保存到 destination 集合
     *
     * @param destinationKey 存储集合键
     * @param key            源集合键
     * @param compareKey     一个或多个对比集合键
     * @return 并集的元素数量
     */
    public Integer sUnionStore(String destinationKey, String key, String... compareKey);

    /**
     * 将源集合和一个或多个集合进行对比求出并集，返回并集的元素数量，并将并集所有元素保存到 destination 集合
     *
     * @param destinationKey 存储集合键
     * @param key            源集合键
     * @param compareKeys    一个或多个对比集合键
     * @return 并集的元素数量
     */
    public Integer sUnionStore(String destinationKey, String key, Collection<String> compareKeys);

//    ==============================有序集合==============================

    /**
     * 将一个 元素 及其 score 值加入到有序集 key 当中
     * 如果 元素 已经是有序集的成员，那么更新这个 元素 的 score 值，并重新排序
     * 如果 key 不存在，则创建一个空的有序集并执行 zAdd 操作
     * 当 key 存在但不是有序集类型时，返回一个错误
     *
     * @param key    键
     * @param score  序号
     * @param member 元素
     * @return 是否执行成功，当插入已存在元素时失败
     */
    public boolean zAdd(String key, double score, String member);

    /**
     * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中
     * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并重新排序
     * 如果 key 不存在，则创建一个空的有序集并执行 zAdd 操作
     * 当 key 存在但不是有序集类型时，返回一个错误
     *
     * @param key    键
     * @param tuples TypedTuple<member, score>
     * @return 添加元素数量
     */
    public Integer zAdd(String key, Set<ZSetOperations.TypedTuple<String>> tuples);

    /**
     * 返回有序集 key 中，成员 member 的 score 值
     *
     * @param key    键
     * @param member 元素
     * @return 序号
     */
    public Double zScore(String key, String member);

    /**
     * 为有序集 key 的成员 member 的 score 值加上增量 increment
     * 当 key 不存在，或 member 不是 key 的成员时， zIncrBy key increment member 等同于 zAdd key increment member
     * 当 key 不是有序集类型时，返回一个错误
     *
     * @param key       键
     * @param increment 增量
     * @param member    元素
     * @return 序号
     */
    public Double zIncrBy(String key, double increment, String member);

    /**
     * 返回有序集 key 的基数
     *
     * @param key 键
     * @return 有序集合的大小
     */
    public Integer zCard(String key);

    /**
     * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量
     *
     * @param key 键
     * @param min 起始score
     * @param max 结束score
     * @return 在区间内的元素数量
     */
    public Integer zCount(String key, double min, double max);

    /**
     * 返回有序集 key 中，指定区间内的成员
     * 成员的位置按 score 值递增(从小到大)来排序
     * <p>
     * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推
     * 也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推
     * <p>
     * 超出范围的下标并不会引起错误。 比如说:
     * 1. 当 start 的值比有序集的最大下标还要大，或是 start > stop 时， zRange 命令只是简单地返回一个空列表
     * 2. 假如 stop 参数的值比有序集的最大下标还要大，那么 Redis 将 stop 当作最大下标来处理
     *
     * @param key   键
     * @param start 起始下标
     * @param stop  结束下标
     * @return 在区间内的元素
     */
    public Set<String> zRange(String key, int start, int stop);

    /**
     * 返回有序集 key 中，指定区间内的成员
     * 成员的位置按 score 值递增(从大到小)来排序
     *
     * @param key   键
     * @param start 起始下标
     * @param stop  结束下标
     * @return 在区间内的元素
     */
    public Set<String> zRevRange(String key, int start, int stop);

    /**
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列
     *
     * @param key 键
     * @param min 起始score
     * @param max 结束score
     * @return 在区间内的元素
     */
    public Set<String> zRangeByScore(String key, double min, double max);

    /**
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列
     * 通过 offset 和 count 指定返回结果下标区间
     *
     * @param key    键
     * @param min    起始score
     * @param max    结束score
     * @param offset 起始下标
     * @param count  数量大小
     * @return 在区间内的元素
     */
    public Set<String> zRangeByScore(String key, double min, double max, int offset, int count);

    /**
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从大到小)次序排列
     *
     * @param key 键
     * @param min 起始score
     * @param max 结束score
     * @return 在区间内的元素
     */
    public Set<String> zRevRangeByScore(String key, double min, double max);

    /**
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从大到小)次序排列
     *
     * @param key    键
     * @param min    起始score
     * @param max    结束score
     * @param offset 起始下标
     * @param count  数量大小
     * @return 在区间内的元素
     */
    public Set<String> zRevRangeByScore(String key, double min, double max, int offset, int count);

    /**
     * 返回有序集 key 中成员 member 的坐标。其中有序集成员按 score 值递增(从小到大)顺序排列
     *
     * @param key    键
     * @param member 元素
     * @return 元素下标
     */
    public Integer zRank(String key, String member);

    /**
     * 返回有序集 key 中成员 member 的坐标。其中有序集成员按 score 值递增(从大到小)顺序排列
     *
     * @param key    键
     * @param member 元素
     * @return 元素下标
     */
    public Integer zRevRank(String key, String member);

    /**
     * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略
     * key 存在但不是有序集类型时，返回一个错误
     *
     * @param key    键
     * @param member 一个或多个元素
     * @return 删除元素数量
     */
    public Integer zRem(String key, String... member);

    /**
     * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略
     * key 存在但不是有序集类型时，返回一个错误
     *
     * @param key     键
     * @param members 一个或多个元素
     * @return 删除元素数量
     */
    public Integer zRem(String key, Collection<String> members);

    /**
     * 移除有序集 key 中，下标介于 start 和 stop 之间（包括等于 start 或 stop ）的成员
     * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。
     * 允许使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推
     *
     * @param key   键
     * @param start 起始下标
     * @param stop  结束下标
     * @return 删除元素数量
     */
    public Integer zRemRangeByRank(String key, int start, int stop);

    /**
     * 除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员
     *
     * @param key 键
     * @param min 起始score
     * @param max 结束score
     * @return 删除元素数量
     */
    public Integer zRemRangeByScore(String key, double min, double max);

}
