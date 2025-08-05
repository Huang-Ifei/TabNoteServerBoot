package com.tabnote.server.tabnoteserverboot.define;

public interface MQName {
    String EXCHANGE_DIRECT = "exchange.tabnote.quota";
    String ROUTING_KEY = "key.tabnote.quota";
    String QUEUE_NAME = "queue.tabnote.quota";

    String EXCHANGE_BACKUP = "exchange.tabnote.backup.quota";
    String ROUTING_BACKUP = "key.tabnote.backup.quota";
    String QUEUE_BACKUP = "queue.tabnote.backup.quota";

    String EXCHANGE_DEAD = "exchange.tabnote.dead.quota";
    String ROUTING_DEAD = "key.tabnote.dead.quota";
    String QUEUE_DEAD = "queue.tabnote.dead.quota";
}
