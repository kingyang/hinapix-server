package com.hinacom.pix.ihe.log;


import com.hinacom.pix.dao.MessageStoreMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fyu on 2016/8/16.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SqlMessageStoreLogger implements IMessageStoreLogger {
    @Autowired
    protected MessageStoreMapper messageStoreMapper;

    @Override
    public void saveLog(MessageStore messageStore) {
        com.hinacom.pix.po.MessageStore poMessageStore = new com.hinacom.pix.po.MessageStore();
        poMessageStore.setIp(messageStore.getIp());
        poMessageStore.setOutmessage(messageStore.getOutMessage());
        poMessageStore.setInmessage(messageStore.getInMessage());
        poMessageStore.setMessagedate(messageStore.getMessageDate());
        poMessageStore.setMessageid(messageStore.getMessageId());
        poMessageStore.setErrormessage(messageStore.getErrorMessage());
        poMessageStore.setSendingfacility(messageStore.getSendingFacility());
        poMessageStore.setSendingapplication(messageStore.getSendingApplication());
        poMessageStore.setReceivingapplication(messageStore.getReceivingApplication());
        poMessageStore.setMessagecode(messageStore.getMessageCode());
        poMessageStore.setTriggerevent(messageStore.getTriggerEvent());
        poMessageStore.setReceivingfacility(messageStore.getReceivingFacility());

        messageStoreMapper.insert(poMessageStore);
    }

    public List<MessageStore> searchLog(MessageStore messageLog){
        // convert message bo to po in order to search
        com.hinacom.pix.po.MessageStore poMessageStore = new com.hinacom.pix.po.MessageStore();
        poMessageStore.setIp(messageLog.getIp());
        poMessageStore.setOutmessage(messageLog.getOutMessage());
        poMessageStore.setInmessage(messageLog.getInMessage());
        poMessageStore.setMessagedate(messageLog.getMessageDate());
        poMessageStore.setMessageid(messageLog.getMessageId());
        poMessageStore.setErrormessage(messageLog.getErrorMessage());
        poMessageStore.setSendingfacility(messageLog.getSendingFacility());
        poMessageStore.setSendingapplication(messageLog.getSendingApplication());
        poMessageStore.setReceivingapplication(messageLog.getReceivingApplication());
        poMessageStore.setMessagecode(messageLog.getMessageCode());
        poMessageStore.setTriggerevent(messageLog.getTriggerEvent());
        poMessageStore.setReceivingfacility(messageLog.getReceivingFacility());

        // query po list to bo list
        List<com.hinacom.pix.po.MessageStore> rPoMessageStoreList
                = messageStoreMapper.selectListByCondition(poMessageStore);
        List<MessageStore> result = new ArrayList<MessageStore>();
        for(com.hinacom.pix.po.MessageStore rPoMessageStor:rPoMessageStoreList)
        {
            MessageStore messageStore = new MessageStore();
            messageStore.setId(messageLog.getId());
            messageStore.setIp(messageLog.getIp());
            messageStore.setOutMessage(rPoMessageStor.getOutmessage());
            messageStore.setInMessage(rPoMessageStor.getInmessage());
            messageStore.setMessageDate(rPoMessageStor.getMessagedate());
            messageStore.setMessageId(rPoMessageStor.getMessageid());
            messageStore.setErrorMessage(rPoMessageStor.getErrormessage());
            messageStore.setSendingFacility(rPoMessageStor.getSendingfacility());
            messageStore.setSendingApplication(rPoMessageStor.getSendingapplication());
            messageStore.setReceivingApplication(rPoMessageStor.getReceivingapplication());
            messageStore.setMessageCode(rPoMessageStor.getMessagecode());
            messageStore.setTriggerEvent(rPoMessageStor.getTriggerevent());
            messageStore.setReceivingFacility(rPoMessageStor.getReceivingfacility());
            result.add(messageStore);
        }
        return result;
    }
}
