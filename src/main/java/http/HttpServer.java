package http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;

public class HttpServer {
    public void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("codec", new HttpServerCodec())  // http 编解码
                                    .addLast("compressor", new HttpContentCompressor())  // HttpContent 压缩
                                    .addLast("aggregator", new HttpObjectAggregator(65536))  // http 消息聚合
//                                    .addLast("inboundA", new ChannelInboundHandlerAdapter(){
//                                        @Override
//                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                            System.out.println("inbound A");
//                                            super.channelRead(ctx, msg);
//                                        }
//                                    })
//                                    .addLast("inboundB", new ChannelInboundHandlerAdapter(){
//                                        @Override
//                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                            System.out.println("inbound B");
//                                            ctx.channel().writeAndFlush("i b");
//                                        }
//                                    })
//                                    .addLast("outboundA", new ChannelOutboundHandlerAdapter(){
//                                        @Override
//                                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                                            System.out.println("outbound A");
//                                            super.write(ctx, msg, promise);
//                                        }
//                                    })
//                                    .addLast("outboundB", new ChannelOutboundHandlerAdapter(){
//                                        @Override
//                                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                                            System.out.println("outbound B");
//                                            super.write(ctx, msg, promise);
//                                        }
//                                    });
                                    .addLast("handler", new HttpServerHandler()); // 自定义业务逻辑处理器
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind().sync();
            System.out.println("http server started, listening on " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new HttpServer().start(8081);
    }
}
