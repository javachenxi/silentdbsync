package cn.com.dbsync.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017-10-25.
 */
public class SpringManager {

    private static transient final Log log =  LogFactory.getLog(SpringManager.class.getName());

    private static final String APPCONTEXT_PATH = "classpath:applicationContext-mybatis.xml";

    private static SpringManager springManager = null;

    private ApplicationContext applicationContext = null;

    private String contextPath = null;

    private static ReentrantLock reentrantLock = new ReentrantLock();

    private SpringManager(String contextPath){
        this.contextPath = contextPath;
        applicationContext = new ClassPathXmlApplicationContext(contextPath);
    }

    public static SpringManager getInstance(){
         if(springManager == null){
             try {
                 reentrantLock.lock();

                 if (springManager == null) {
                     springManager = new SpringManager(APPCONTEXT_PATH);
                 }
             }catch (Exception e){
                 log.error( "SpringManager≥ı ºªØ“Ï≥£", e);
             }finally {
                 reentrantLock.unlock();
             }
         }
        return springManager;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public <T> T getBeanByClassName(Class<T> t ){
        return this.getBeanByName(t.getName(), t);
    }

    public <T> T getBeanByName(String beanName, Class<T> t){
        return applicationContext.getBean(beanName, t);
    }

    public <T> T getBeanByType(Class<T> t ){
        return applicationContext.getBean(t);
    }

}
