-- 初始化面试官人设
INSERT INTO interviewer_personas (id, name, description, prompt_template) VALUES 
(1, '温和的HR', '注重沟通与团队协作，鼓励式面试', '你是一位非常温柔、友善的HR面试官。你的目标是让候选人放松，重点考察其沟通能力、抗压能力和团队协作精神。请用亲切的语气进行评价。'),
(2, '严厉的技术总监', '关注底层原理，喜欢深挖细节，压力面试', '你是一位要求极高、非常严厉的技术总监。你喜欢一针见血地指出候选人的漏洞，重点考察其技术深度（系统设计、底层原理）和逻辑思维。请用专业、甚至有些挑剔的语气进行评价。'),
(3, '经验丰富的架构师', '关注系统设计和全局观', '你是一位拥有十几年经验的架构师。你考察的重点不仅是代码实现，更是候选人的架构思维、可扩展性和复杂问题的解决能力。请给出建设性、高屋建瓴的反馈。')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 初始化题库
INSERT INTO questions (category, title, answer, difficulty, question_type, default_code) VALUES 
('Java基础', '请解释一下 Java 中的多态是什么，并给出一个实际应用场景？', '多态是指同一个方法调用，由于对象不同可能会有不同的行为。在Java中通过方法重写(Override) 和 接口实现来完成。应用场景：比如一个Pay接口，有微信支付和支付宝支付两个实现类，业务层只需要调用Pay接口的方法，无需关心具体实现。', '简单', 'conceptual', NULL),
('Java基础', 'HashMap 的底层原理是什么？为什么它是线程不安全的？', 'HashMap在JDK1.8前是数组+链表，1.8后是数组+链表/红黑树。它通过Key的hashCode计算数组下标。线程不安全是因为在多线程并发put时，可能会导致数据覆盖，在JDK1.7中多线程扩容还可能导致环形链表死循环。推荐多线程下使用ConcurrentHashMap。', '中等', 'conceptual', NULL),
('并发编程', '请说一下 volatile 关键字的作用。它能保证原子性吗？', 'volatile 保证了变量的可见性（每次读取都从主内存读取最新值）和禁止指令重排序。但它不能保证复合操作（如 i++）的原子性。如果要保证原子性，需要使用 synchronized 或 Atomic 类。', '中等', 'conceptual', NULL),
('数据库', 'MySQL 的事务隔离级别有哪些？默认是哪个？', '读未提交(Read Uncommitted)、读已提交(Read Committed)、可重复读(Repeatable Read)、串行化(Serializable)。MySQL InnoDB 默认的隔离级别是可重复读 (RR)。', '简单', 'conceptual', NULL),
('Spring框架', '什么是 Spring AOP？请举例说明其在项目中的常见应用。', 'AOP(面向切面编程)可以将那些与业务无关，却为业务模块所共同调用的逻辑（例如事务处理、日志管理、权限控制等）封装起来。应用例子：记录接口访问日志、统一的异常处理、声明式事务管理。', '中等', 'conceptual', NULL),
('简历问题', '请你先简短做个自我介绍吧。', '作为候选人，您应该做一段1-2分钟的自我介绍。介绍内容应包含：您的基本背景（如学校、专业）、核心技术栈（如Java, Spring Cloud）、最得意的项目经验以及您的技术亮点。AI面试官将根据您的自我介绍开展个性化追问。', '简单', 'conceptual', NULL),
('简历问题', '请详细描述下你简历中的这个点云感知项目', '3D点云感知项目通常涉及激光雷达（LiDAR）数据处理。回答时应阐述：1. 项目背景与目标（如自动驾驶中的障碍物检测）；2. 核心技术架构（如采用 PointNet++, CenterPoint 等深度学习网络，或传统的 PCL 滤波、分割算法）；3. 您的具体工作（如多传感器融合、模型轻量化）；4. 取得的量化成果（如FPS提升、精度mAP提升）。', '中等', 'conceptual', NULL),
('岗位问题', '你对我们公司了解多少？', '在面试前，您需要对目标公司的业务方向、行业地位、核心产品和近期动态有所了解。回答时建议遵循以下逻辑：1. 公司主营业务和所处赛道；2. 您对公司某款产品或技术的看法和使用体验；3. 表达自己对加入公司的强烈兴趣，并说明自己的技能如何与岗位需求及公司发展契合。', '简单', 'conceptual', NULL),
('算法', '如何在一个未排序的数组中找到第K大的元素？', '主要有以下几种方法：1. 快速选择算法（Quick Select）：基于快速排序的分治思想，平均时间复杂度为O(N)，最坏为O(N^2)；2. 堆排序法：构建一个大小为K的小顶堆，遍历数组，时间复杂度为O(N log K)，空间复杂度为O(K)；3. 排序法：直接排序后取第N-K个元素，时间复杂度为O(N log N)。推荐使用快速选择算法。', '中等', 'code', 'import java.util.*;

public class Solution {
    public int findKthLargest(int[] nums, int k) {
        // 在此编写您的算法代码
        return 0;
    }
}'),
('系统设计', '设计一个支持高并发的短网址生成系统。', '核心设计要点包括：
1. **唯一ID生成**：使用分布式ID生成器（如雪花算法 Snowflake）生成唯一长整型ID，然后通过 62进制（A-Z, a-z, 0-9）转换成短字符串。
2. **读写分离与缓存**：短链接读多写少，使用 Redis 缓存短网址到长网址的映射，设置合理的过期时间，命中后直接 302 重定向。
3. **分库分表**：根据短网址 of Hash 值进行分库分表，分流写压力。
4. **防并发重入与布隆过滤器**：使用布隆过滤器防止缓存穿透，保障数据库安全。
5. **系统架构图：
```mermaid
graph TD
    Client[客户端] --> |请求短网址| Gateway[API网关]
    Gateway --> |校验/限流| Cache{Redis缓存}
    Cache --> |命中: 302重定向| Client
    Cache --> |未命中| DB[(MySQL分库分表)]
    DB --> |回写缓存| Cache
    
    Client --> |生成短网址| Generator[短网址生成服务]
    Generator --> |雪花算法| IDGen[分布式ID生成器]
    Generator --> |Base62编码| ShortURL[生成短码]
    ShortURL --> |写入| DB
```', '困难', 'conceptual', NULL),
('NLP', 'Transformer 模型相比 RNN 的优势是什么？', 'Transformer 相比 RNN 具有以下核心优势：
1. **并行计算能力**：RNN 必须按时间步顺序计算，无法并行；Transformer 采用 Self-Attention（自注意力机制），可以一次性处理整个序列，极大提升了训练速度。
2. **长距离依赖解决**：RNN 容易出现梯度消失或梯度爆炸问题，难以保留长距离上下文信息；Transformer 每一个 token 都能直接与所有 token 交互，距离为 1，完美解决了长距离依赖问题。
3. **特征表达更强**：通过多头注意力机制（Multi-Head Attention），模型能同时关注不同位置的多种上下文特征，语义表征更丰富。', '中等', 'conceptual', NULL),
('新技术', 'DeepSeek 最近很火爆，你了解他的技术么？知道他厉害在哪里么？', 'DeepSeek (特别是 DeepSeek-V3 和 R1) 在全球引起巨大轰动，其核心技术优势体现在：
1. **MoE 混合专家架构（Multi-head Latent Attention, MLA）**：大幅降低了 KV Cache 的内存占用；并配合 DeepSeekMoE，仅激活部分专家，极大节省了计算资源和推理成本。
2. **R1 强化学习（Reinforcement Learning）**：DeepSeek-R1 采用大规模 RL 训练，使其展现出类似于 OpenAI o1 的“思考过程（Chain-of-Thought, CoT）”，具备强大的自我反思、推理 and 数学/编程解题能力。
3. **极低的训练与推理成本**：DeepSeek 实现了与 GPT-4/Claude 3.5 相当的性能，但训练成本仅为数百万美元，推理费用便宜了近百倍，堪称大模型性价比的里程碑。', '中等', 'conceptual', NULL),
('新技术', '2025 年至今发布的最重要的一个AI大模型是啥，请简要说明它的特点 and 应用场景', '2025 年至今发布的最重要的 AI 大模型之一是 **DeepSeek-R1** 及其开源模型家族（如基于 Llama 和 Qwen 的蒸馏版本）。
- **核心特点**：
  1. **原生推理（Chain-of-Thought）能力**：在回答前进行深度思考，并在文本中输出其思考逻辑 <think>...</think>，对错误步骤进行自我纠错。
  2. **完全开源**：DeepSeek 开源了其模型权重和研究细节，引爆了整个 AI 开源社区和本地私有化部署热潮。
  3. **超强逻辑解题**：在数学、竞赛编程（Math, Codeforces）以及科学问题上达到顶尖水平。
- **应用场景**：复杂算法编写、数学与物理公式推导、法律和医疗专业文档的深度逻辑 analysis、自动智能代理（Agent）的底层规划。', '简单', 'conceptual', NULL),
('金融', '解释贴现现金流（DCF）模型的计算步骤。', '贴现现金流（DCF, Discounted Cash Flow）是评估资产或公司价值的经典金融模型。其计算公式和步骤如下：

### 1. 核心计算公式
资产价值等于未来所有期望现金流的折现值之和：
$$Value = \sum_{t=1}^{n} \frac{CF_t}{(1+r)^t} + \frac{TV}{(1+r)^n}$$

其中：
- $CF_t$ 为第 $t$ 年的自由现金流（Free Cash Flow, FCF）
- $r$ 为折现率，通常采用加权平均资本成本（WACC）
- $TV$ 为终值（Terminal Value）
- $n$ 为预测期年限

### 2. 计算步骤
1. **预测未来现金流 ($CF_t$)**：通常预测未来 5-10 年的无杠杆自由现金流（UFCF）。公式为：
   $$UFCF = EBIT \times (1 - Tax) + D\&A - CapEx - \Delta NWC$$
   *(注：EBIT为息税前利润，Tax为税率，D&A为折旧摊销，CapEx为资本支出，\Delta NWC为营运资金变动)*
2. **确定折现率 ($r$)**：一般采用加权平均资本成本（WACC）：
   $$WACC = \frac{E}{V} \times R_e + \frac{D}{V} \times R_d \times (1 - T_c)$$
3. **计算终值 ($TV$)**：预测期后的永续价值，常用永续增长模型（Gordon Growth Model）：
   $$TV = \frac{CF_n \times (1 + g)}{r - g}$$
   *(g 为永续增长率，通常取 GDP 长期增长率)*
4. **折现并求和**：将预测期内的现金流和终值折现到当前时点，加总即得公司企业价值（EV）。', '困难', 'conceptual', NULL),
('算法', '两数之和 (Two Sum)', '使用 HashMap 可以在 O(N) 时间复杂度与 O(N) 空间复杂度下解决该问题。在遍历数组时，将当前数值与索引存入 Map，同时查找 Map 中是否存在目标值与当前值的差值。如果存在，则直接返回其索引与当前索引。', '简单', 'code', 'import java.util.*;

public class Solution {
    public int[] twoSum(int[] nums, int target) {
        // 在此编写您的算法代码
        return new int[0];
    }
}'),
('数据结构', '反转链表 (Reverse Linked List)', '可以使用双指针迭代法。定义一个 prev 指针指向 null，curr 指针指向头节点。在遍历链表时，先保存当前节点的下一个节点，然后将当前节点的 next 指向 prev，接着更新 prev 为当前节点，curr 为下一个节点。时间复杂度为 O(N)，空间复杂度为 O(1)。', '简单', 'code', 'public class Solution {
    public ListNode reverseList(ListNode head) {
        // 在此编写您的算法代码
        return null;
    }
}

// 链表节点定义
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}'),
('算法', '无重复字符的最长子串', '使用滑动窗口（双指针）法解决。维护一个左指针 left 和一个右指针 i，并使用 HashMap 记录字符最后出现的位置。当右指针遍历到重复字符时，将左指针移动到重复字符上一次出现位置的下一个位置。每次移动都计算当前窗口大小并更新最大长度。时间复杂度为 O(N)，空间复杂度为 O(min(M, N))。', '中等', 'code', 'import java.util.*;

public class Solution {
    public int lengthOfLongestSubstring(String s) {
        // 在此编写您的算法代码
        return 0;
    }
}')
ON DUPLICATE KEY UPDATE category=VALUES(category), answer=VALUES(answer), difficulty=VALUES(difficulty), question_type=VALUES(question_type), default_code=VALUES(default_code);
