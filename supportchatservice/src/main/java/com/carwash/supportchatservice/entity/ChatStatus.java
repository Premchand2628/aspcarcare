package com.carwash.supportchatservice.entity;

public enum ChatStatus {
    NEW,      // user requested, waiting for agent
    ACTIVE,   // agent and user chatting
    CLOSED    // chat ended by user or admin
}
