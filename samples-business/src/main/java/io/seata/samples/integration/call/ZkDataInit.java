package io.seata.samples.integration.call;

import io.seata.config.zk.DefaultZkSerializer;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
@Slf4j
public class ZkDataInit {

    private static volatile ZkClient zkClient;

    public static void main(String[] args) {

        if (zkClient == null) {
            ZkSerializer zkSerializer = new DefaultZkSerializer();
            zkClient = new ZkClient("127.0.0.1:2181", 6000, 2000, zkSerializer);
        }
        if (!zkClient.exists("/seata")) {
            zkClient.createPersistent("/seata", true);
        }
        //获取key对应的value值
        Properties properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        // 使用properties对象加载输入流
        try {
            File file = ResourceUtils.getFile("classpath:zk-config.properties");
            InputStream in = new FileInputStream(file);
            properties.load(in);
            Set<Object> keys = properties.keySet();//返回属性key的集合
            for (Object key : keys) {
                boolean b = putConfig(key.toString(), properties.get(key).toString());
                log.info(key.toString() + "=" + properties.get(key)+"result="+b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param dataId
     * @param content
     * @return
     */
    public static boolean putConfig(final String dataId, final String content) {
        Boolean flag = false;
        String path = "/seata/" + dataId;
        if (!zkClient.exists(path)) {
            zkClient.create(path, content, CreateMode.PERSISTENT);
            flag = true;
        } else {
            zkClient.writeData(path, content);
            flag = true;
        }
        return flag;
    }
}
