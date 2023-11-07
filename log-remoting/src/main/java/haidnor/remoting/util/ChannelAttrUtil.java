package haidnor.remoting.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class ChannelAttrUtil {

    public static <T> AttributeKey<T> getKey(Class<T> type) {
        return AttributeKey.valueOf(type.getName());
    }

    /**
     * 为 Channel 添加附件
     *
     * @param channel   channel
     * @param attribute 附件
     * @param type      附件的 Class 类型, 这里需要手动设置类型的原型为 attribute 可能为动态代理对象, 使用 attribute.getClass() 不能获取到真实的类型
     */
    public static <T> void set(Channel channel, T attribute, Class<T> type) {
        AttributeKey<T> attributeKey = getKey(type);
        channel.attr(attributeKey).set(attribute);
    }

    public static <T> T get(Channel channel, Class<T> type) {
        AttributeKey<T> attributeKey = getKey(type);
        return channel.attr(attributeKey).get();
    }

}