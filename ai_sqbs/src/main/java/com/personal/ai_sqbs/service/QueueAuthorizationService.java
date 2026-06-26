package com.personal.ai_sqbs.service;

import com.personal.ai_sqbs.entity.QueueTicket;
import com.personal.ai_sqbs.security.UserPrincipal;

public interface QueueAuthorizationService {

    void validateCanViewTicket(QueueTicket ticket, UserPrincipal currentUser);

    void validateCanOperateTicket(UserPrincipal currentUser);

    void validateCanViewBranchQueue(UserPrincipal currentUser);
}
