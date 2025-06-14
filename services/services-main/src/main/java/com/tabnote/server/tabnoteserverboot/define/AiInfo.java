package com.tabnote.server.tabnoteserverboot.define;

import java.net.InetSocketAddress;
import java.net.Proxy;

public interface AiInfo {
    String CHATGPT_API_KEY = "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";

    String siliconFlowDeepSeek_API_KEY = "sk-zpeawyykaamxijenlghmdmloqdzigemeesmcpxjxcxkuyhuw";

    String DEEPSEEK_API_KEY = "sk-b1573aa6ffc04264a0a263ece1b7a4ab";

    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));

    String secretId = "AKIDTiieYDcmhMmwqkYjsqZ2FCtKUO2QYR5U";

    String secretKey = "C9uQggRxEEySgdZJpFgz3rPhjW2IK6cs";

    String[] modelList = {
            "gpt-4o-2024-08-06",
            "gpt-4o-mini",
            "o1-mini",
            "deepseek-reasoner",
            "Pro/deepseek-ai/DeepSeek-R1",
            "o3-mini",
            "o3",
            "Pro/deepseek-ai/DeepSeek-V3",
            "gpt-4.1",
            "gpt-4.1-mini",
            "o4-mini",
    };
}
