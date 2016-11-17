package com.hinacom.pix.dao;

import com.hinacom.pix.po.MessageStore;

import java.util.List;


public interface MessageStoreMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MessageStore record);

    int insertSelective(MessageStore record);

    MessageStore selectByPrimaryKey(Integer id);

    List<MessageStore> selectListByCondition(MessageStore messageStore);

    int updateByPrimaryKeySelective(MessageStore record);

    int updateByPrimaryKey(MessageStore record);
}