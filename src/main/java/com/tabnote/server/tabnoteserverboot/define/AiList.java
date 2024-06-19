package com.tabnote.server.tabnoteserverboot.define;

import java.net.InetSocketAddress;
import java.net.Proxy;

public interface AiList {
    String[] name = new String[]{
            "Gemini-Pro",
            "ChatGPT-3.5(敬请期待)",
            "ChatGPT-4.0(敬请期待)",
            "Llama_3(敬请期待)"};
    String GENERATE_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";
    String STREAM_API_URL =   "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:streamGenerateContent?alt=sse&key=";
    String GOOGLE_API_KEY = "key";
    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
    int GEMINI_FLASH_MAX_DAILY_REQUEST = 1450;
    int GEMINI_PRO_MAX_DAILY_REQUEST = 45;
}
