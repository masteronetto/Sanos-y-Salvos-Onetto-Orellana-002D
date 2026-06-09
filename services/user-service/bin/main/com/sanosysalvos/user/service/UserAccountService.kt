package com.sanosysalvos.user.service

// AUTH ROLE: redundant-auth
// CALLS XANO FOR AUTH: yes
// STATUS: redundant
// NOTE: This service performs auth via XanoUserClient, but active auth endpoints currently call XanoAuthClient from controller.
// SAFE TO REMOVE: only after UserHealthController auth endpoints are deleted
//                 and confirmed no traffic in staging/prod.

import org.springframework.stereotype.Service

@Service
class UserAccountService {
    // Retained for future user-data and admin service methods.
}
