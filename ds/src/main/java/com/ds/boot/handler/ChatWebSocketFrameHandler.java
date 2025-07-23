package com.ds.boot.handler;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author ds
 * @date 2025/7/23
 * @description
 */
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

	// 存储所有连接的Channel
	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	// 存储用户与Channel的映射
	public static Map<String, Channel> userChannels = new HashMap<>();

	// 存储临时用户名
	public static Map<Channel, String> tempUsernames = new HashMap<>();

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();

		// 查找对应的用户名
		String username = null;
		for (Map.Entry<String, Channel> entry : userChannels.entrySet()) {
			if (entry.getValue() == incoming) {
				username = entry.getKey();
				break;
			}
		}

		if (username != null) {
			// 从用户映射中移除
			userChannels.remove(username);

			// 通知其他用户有用户离开
			for (Channel channel : channels) {
				channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + username + " 离开聊天"));
			}
		}

		channels.remove(ctx.channel());
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		Channel incoming = ctx.channel();

		// 获取消息内容
		String message = msg.text();

		try {
			// 尝试解析JSON消息
			JSONObject json = JSON.parseObject(message);

			if (json.containsKey("type")) {
				String type = json.getString("type");

				if ("username".equals(type)) {
					// 处理用户名设置
					String username = json.getString("username");

					// 检查用户名是否已被占用
					if (userChannels.containsKey(username)) {
						// 用户名已被占用
						incoming.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(
								JSONObject.of("type", "username", "username", username, "success", false))));
						return;
					}

					// 移除临时用户名映射（如果存在）
					tempUsernames.remove(incoming);

					// 将用户与Channel关联
					userChannels.put(username, incoming);

					// 通知其他用户有新用户加入
					for (Channel channel : channels) {
						channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + username + " 加入聊天"));
					}

					// 发送欢迎消息
					incoming.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(
							JSONObject.of("type", "username", "username", username, "success", true))));

					// 将用户添加到频道
					channels.add(ctx.channel());

					return;
				} else if ("message".equals(type)) {
					// 处理普通消息
					String username = json.getString("username");
					String content = json.getString("content");

					// 解析消息格式：@username message
					if (content.startsWith("@")) {
						int spaceIndex = content.indexOf(' ');
						if (spaceIndex > 1) {
							String targetUsername = content.substring(1, spaceIndex);
							String privateMessage = content.substring(spaceIndex + 1);

							// 查找目标用户
							Channel targetChannel = userChannels.get(targetUsername);
							if (targetChannel != null) {
								// 发送私聊消息
								targetChannel.writeAndFlush(new TextWebSocketFrame(
										"[私聊] " + username + ": " + privateMessage));

								// 回复发送者
								incoming.writeAndFlush(new TextWebSocketFrame(
										"[私聊] 给 " + targetUsername + ": " + privateMessage));
							} else {
								incoming.writeAndFlush(new TextWebSocketFrame(
										"[SERVER] - 用户 " + targetUsername + " 不存在"));
							}
						} else {
							incoming.writeAndFlush(new TextWebSocketFrame(
									"[SERVER] - 私聊消息格式错误: @username message"));
						}
					} else {
						// 打印收到的消息到服务器控制台
						System.out.println("收到 " + username + " 的消息: " + content);

						// 将消息广播给所有连接的客户端（包括发送者自己）
						for (Channel channel : channels) {
							channel.writeAndFlush(new TextWebSocketFrame("[" + username + "] " + content));
						}
					}

					return;
				}
			}
		} catch (Exception e) {
			// 不是JSON消息，可能是旧客户端发送的消息
			// 生成一个临时用户名
			String tempUsername = generateTempUsername();
			tempUsernames.put(incoming, tempUsername);

			// 通知其他用户有新用户加入
			for (Channel channel : channels) {
				channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + tempUsername + " 加入聊天"));
			}

			// 发送欢迎消息
			incoming.writeAndFlush(new TextWebSocketFrame("[SERVER] - 欢迎 " + tempUsername + " 加入聊天"));

			// 将用户添加到频道
			channels.add(ctx.channel());

			// 将消息广播给所有连接的客户端（包括发送者自己）
			for (Channel channel : channels) {
				channel.writeAndFlush(new TextWebSocketFrame("[" + tempUsername + "] " + message));
			}

			return;
		}
	}

	// 生成临时用户名
	private String generateTempUsername() {
		String username;
		do {
			username = "游客" + (int)(Math.random() * 1000);
		} while (userChannels.containsKey(username) || isTempUsernameExist(username));
		return username;
	}

	// 检查临时用户名是否存在
	private boolean isTempUsernameExist(String username) {
		for (String tempUsername : tempUsernames.values()) {
			if (tempUsername.equals(username)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}