package com.aisino.gmall.user.service;

import com.aisino.gmall.user.bean.UmsMember;
import com.aisino.gmall.user.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
