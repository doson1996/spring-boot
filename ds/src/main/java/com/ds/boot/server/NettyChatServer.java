package com.ds.boot.server;

import com.ds.boot.handler.ChatWebSocketFrameHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author ds
 * @date 2025/7/23
 * @description
 */
public class NettyChatServer {

	private final int port;

	public NettyChatServer(int port) {
		this.port = port;
	}

	public void start() throws InterruptedException {
		// 接收客户端连接的主EventLoopGroup
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		// 处理连接的EventLoopGroup
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new HttpServerCodec());
							ch.pipeline().addLast(new ChunkedWriteHandler());
							ch.pipeline().addLast(new HttpObjectAggregator(65536));
							ch.pipeline().addLast(new WebSocketServerProtocolHandler("/chat"));
							ch.pipeline().addLast(new ChatWebSocketFrameHandler());
						}
					});

			ChannelFuture future = bootstrap.bind(port).sync();
			System.out.println("Netty聊天服务器已启动，监听端口: " + port);

			// 等待服务器关闭
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		new NettyChatServer(8080).start();
	}

}
