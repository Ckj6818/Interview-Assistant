package com.mindspark.interviewai;

import java.nio.file.*;

public class FixJsStrings {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/main/resources/templates/mock_interview.html");
        String content = new String(Files.readAllBytes(path), "UTF-8");
        
        content = content.replace(
            "previewText.innerText = \"未听清您的回答，请点击麦克风重新说话?;", 
            "previewText.innerText = \"未听清您的回答，请点击麦克风重新说话。\";"
        );
        content = content.replace(
            "previewText.innerText = \"网络异常，无法连接到面试服务器?;", 
            "previewText.innerText = \"网络异常，无法连接到面试服务器。\";"
        );
        content = content.replace(
            "previewText.innerText = \"【模拟面试已达最大轮次，正在生成评估报告...?;", 
            "previewText.innerText = \"【模拟面试已达最大轮次，正在生成评估报告...】\";"
        );
        content = content.replace(
            "previewText.innerText = \"【您可以点击麦克风进行回答，或点击右下角结束面试?;", 
            "previewText.innerText = \"【您可以点击麦克风进行回答，或点击右下角结束面试】\";"
        );
        content = content.replace(
            "label.innerText = role === 'ai' ? 'AI 面试? : '?;", 
            "label.innerText = role === 'ai' ? 'AI 面试官' : '我';"
        );
        content = content.replace(
            "AI 正在深度审查您的多轮对答记录，打分并编排雷达图各项能力评测。请稍等几秒钟</p>", 
            "AI 正在深度审查您的多轮对答记录，打分并编排雷达图各项能力评测。请稍等几秒钟。</p>"
        );
        content = content.replace(
            "fullLog += `${msg.role === 'user' ? '候选人' : '面试?}: ${msg.content}\\n\\n`;", 
            "fullLog += `${msg.role === 'user' ? '候选人' : '面试官'}: ${msg.content}\\n\\n`;"
        );
        content = content.replace(
            "alert(\"评分生成失败，请重试?);", 
            "alert(\"评分生成失败，请重试。\");"
        );
        
        Files.write(path, content.getBytes("UTF-8"));
        System.out.println("Replaced corrupted JS strings");
    }
}
