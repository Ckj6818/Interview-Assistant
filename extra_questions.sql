INSERT INTO questions (category, title, answer) VALUES 
('Java基础', 'String、StringBuffer和StringBuilder的区别？', 'String是不可变的，每次操作都会产生新对象；StringBuffer是可变且线程安全的（方法加了同步锁），适合多线程环境；StringBuilder是可变但非线程安全的，性能最高，适合单线程环境。'),
('Java基础', 'Java中重载和重写的区别？', '重载(Overload)发生在同一个类中，方法名相同但参数列表不同（类型、个数、顺序），与返回值无关；重写(Override)发生在父子类中，方法名、参数列表必须相同，返回值必须小于等于父类，抛出的异常必须小于等于父类，访问修饰符必须大于等于父类。'),
('Java基础', '接口和抽象类的区别？', '1. 接口只能包含抽象方法（Java 8+可有默认方法），抽象类可以有普通方法；2. 类可以实现多个接口，但只能继承一个抽象类；3. 接口成员变量默认是 public static final，抽象类可有各种变量；4. 接口主要用于定义规范，抽象类用于代码复用。'),
('Java并发', '什么是死锁？如何避免？', '死锁是指两个或多个线程互相持有对方需要的资源而无限期阻塞。避免死锁的方法：1. 避免一个线程同时获取多个锁；2. 避免一个线程在锁内同时占用多个资源，尽量保证每个锁只占用一个资源；3. 尝试使用定时锁（如 lock.tryLock(timeout)）；4. 保证资源请求的顺序性。'),
('Java并发', '请解释 ThreadLocal 的原理和内存泄漏问题。', 'ThreadLocal为每个线程提供独立的变量副本。底层通过Thread对象的ThreadLocalMap存储，key是ThreadLocal对象（弱引用），value是实际存储的值（强引用）。因为key是弱引用，GC时key会被回收变为null，但value是强引用无法回收，导致内存泄漏。解决：使用完后手动调用 remove() 方法。'),
('Spring框架', 'Spring MVC 的核心工作流程是什么？', '1. 客户端发送请求至前端控制器 DispatcherServlet；2. DispatcherServlet 收到请求调用 HandlerMapping 处理器映射器；3. 解析获取具体的处理器并返回给 DispatcherServlet；4. 传给 HandlerAdapter 处理器适配器去执行 Controller；5. Controller 执行完返回 ModelAndView；6. ViewResolver 视图解析器解析并渲染视图，响应给客户端。'),
('Spring框架', 'Spring 中的 Bean 是线程安全的吗？', 'Spring 容器中的 Bean 默认是单例（Singleton）的。如果 Bean 中存在可变的成员变量（状态），那么多线程并发访问时是线程不安全的。解决办法：1. 将 Bean 作用域改为 prototype；2. 使用 ThreadLocal 保存可变状态；3. 在 Bean 中尽量使用局部变量，避免定义可变的成员变量。'),
('数据库', 'Redis 有哪些常见的数据类型及应用场景？', '1. String：做缓存、计数器（如点赞数）；2. Hash：存储对象信息（如用户信息）；3. List：消息队列、最新文章列表；4. Set：共同好友、随机抽奖；5. ZSet（有序集合）：排行榜功能。'),
('数据库', '什么是缓存穿透、缓存击穿和缓存雪崩？', '缓存穿透：请求一个数据库和缓存中都不存在的数据，导致请求直接打到数据库。解决：布隆过滤器或缓存空值。缓存击穿：一个热点key在失效的瞬间，海量并发请求打到数据库。解决：互斥锁（如Redis分布式锁）。缓存雪崩：大量缓存在同一时间失效，导致数据库压力激增。解决：设置不同的过期时间、搭建Redis集群。'),
('计算机网络', 'GET 和 POST 的区别是什么？', '1. GET参数通过URL传递，POST参数放在请求体(Body)中；2. GET请求的URL长度有限制，POST没有；3. GET请求会被浏览器主动缓存，POST不会；4. GET请求在浏览器回退时是无害的，POST会再次提交请求；5. 从安全性看，POST比GET相对安全，因为参数不暴露在URL中。');
