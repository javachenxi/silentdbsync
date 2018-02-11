package cn.com.dbsync.core;

/**
 * Created by cxi on 2016/5/11.
 */
public interface IDispatchPolicy {


    /**
     * �ַ��¼��Ĳ���ʵ��
     */
    public void dispatch();

    /**
     * �Ƿ�Ϊ�첽����
     *
     * @return boolean boolean
     */
    public boolean isASync();


    /**
     * ����ȴ�
     *
     * @param timeout the timeout
     * @throws InterruptedException the interrupted exception
     */
    public void waiting(long timeout) throws InterruptedException;

    /**
     * ������ڵȴ������ѵ�ǰ�߳�
     */
    public void wakeup();

}
