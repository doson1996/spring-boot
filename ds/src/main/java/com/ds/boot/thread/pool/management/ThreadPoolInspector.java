package com.ds.boot.thread.pool.management;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author ds
 * @date 2026/3/26
 * @description
 */
public class ThreadPoolInspector {

	public static void main(String[] args) {
		printAllThreadPools();
	}

	/**
	 * 打印所有线程池信息到控制台
	 */
	public static void printAllThreadPools() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		long[] threadIds = threadMXBean.getAllThreadIds();
		ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, Integer.MAX_VALUE);

		System.out.println("=== JVM 线程池信息 ===");
		System.out.println("总线程数：" + threadIds.length);
		System.out.println();

		// 按线程名前缀分组
		Map<String, List<ThreadInfo>> groups = Arrays.stream(threadInfos)
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(info -> {
					String name = info.getThreadName();
					return name.replaceAll("\\d+$", "").trim();
				}));

		groups.entrySet().stream()
				.filter(e -> e.getValue().size() >= 1)
				.sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
				.forEach(entry -> {
					List<ThreadInfo> threads = entry.getValue();
					System.out.printf("线程组：%s - %d 个线程%n", entry.getKey(), threads.size());

					long runnable = threads.stream()
							.filter(t -> t.getThreadState() == Thread.State.RUNNABLE)
							.count();
					long waiting = threads.stream()
							.filter(t -> t.getThreadState() == Thread.State.WAITING ||
									t.getThreadState() == Thread.State.TIMED_WAITING)
							.count();
					long blocked = threads.stream()
							.filter(t -> t.getThreadState() == Thread.State.BLOCKED)
							.count();

					System.out.printf("  运行中：%d, 等待中：%d, 阻塞中：%d%n", runnable, waiting, blocked);

					// 显示前 5 个线程详情
					threads.stream().limit(5).forEach(t ->
							System.out.printf("    - %s (状态：%s)%n", t.getThreadName(), t.getThreadState())
					);
					if (threads.size() > 5) {
						System.out.printf("    ... 还有 %d 个线程%n", threads.size() - 5);
					}
					System.out.println();
				});
	}
}
