package com.mindspark.interviewai;

import java.nio.file.*;
import java.util.regex.*;

public class FixJsSyntax {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/main/resources/templates/mock_interview.html");
        String content = new String(Files.readAllBytes(path), "UTF-8");
        
        String replacement = "    const resumeTemplates = {\n" +
"        autonomous: {\n" +
"            company: '阿尔法自动驾驶科技有限公司',\n" +
"            job: '自动驾驶三维点云感知算法专家',\n" +
"            resume: '【个人简历】\\n姓名：张三\\n求职意向：自动驾驶点云感知算法专家\\n工作经历：\\n2024年 - 至今 | 某头部自动驾驶公司 | 资深点云算法工程师\\n- 负责自动驾驶激光雷达三维点云感知项目。\\n- 深入优化三维点云深度学习模型，主导基于 PointNet++ 和 PointPillars 的点云分割与三维目标检测（3D Object Detection）网络研发。\\n- 针对车载毫米波雷达与 LiDAR 点云数据进行多传感器融合（Multi-sensor Fusion），实现极端天气下障碍物检测精度提升 15%。\\n- 解决大规模点云场景下的实时性瓶颈，通过 TensorRT 对感知算法进行端到端加速，将推理延迟从 80ms 降低至 22ms。'\n" +
"        },\n" +
"        finance: {\n" +
"            company: '华尔街环球资产管理公司',\n" +
"            job: '高级金融分析师(M&A)',\n" +
"            resume: '【个人简历】\\n姓名：李四\\n求职意向：高级金融分析师\\n工作经历：\\n2023年 - 2025年 | 头部投资银行 | 投资银行部高级分析师\\n- 深度参与多个大型跨国并购（M&A）及 IPO 业务，主导财务建模与估值工作。\\n- 精通贴现现金流（Discounted Cash Flow, DCF）模型，负责对目标公司进行财务预测（Financial Forecasting）、自由现金流（FCF）计算以及加权平均资本成本（WACC）测算。\\n- 独立编写行业研究报告与投资备忘录（Investment Memorandum），评估宏观政策与微观财务指标对项目估值的影响。'\n" +
"        }\n" +
"    };";
        
        Pattern pattern = Pattern.compile("const resumeTemplates = \\{.*?\\};", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            content = matcher.replaceAll(Matcher.quoteReplacement(replacement));
            Files.write(path, content.getBytes("UTF-8"));
            System.out.println("Replaced resumeTemplates");
        } else {
            System.out.println("Could not find resumeTemplates");
        }
    }
}
