package com.example.emos.workflow.service.impl;

import com.example.emos.workflow.db.dao.TbAmectTypeDao;
import com.example.emos.workflow.db.pojo.TbAmect;
import com.example.emos.workflow.service.AmectService;
import com.example.emos.workflow.service.AmectTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AmectTypeServiceImpl implements AmectTypeService {
    @Autowired
    private TbAmectTypeDao amectTypeDao;

    @Override
    public HashMap searchByType(String type) {
        HashMap map= amectTypeDao.searchByType(type);
        return map;
    }
}
