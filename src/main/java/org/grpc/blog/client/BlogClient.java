package org.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) {
        System.out.println("Blog gRPC Client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        //create blog
        //createBlog(channel);

        //read blog
        //readBlog(channel);

        //update blog
        //updateBlog(channel);

        //delete blog
        //deleteBlog(channel);

        //stream, list blogs
        streamBlogs(channel);
    }

    private static void streamBlogs(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        blogClient.listBlog(ListBlogRequest.newBuilder().build())
                .forEachRemaining(listBlogResponse -> System.out.println(listBlogResponse.getBlog()));
    }

    private static void deleteBlog(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        DeleteBlogResponse response = blogClient.deleteBlog(DeleteBlogRequest.newBuilder()
                .setBlogId("65b07d46eb870b7a6645c21e")
                .build());

        System.out.println("Received response...");
        System.out.println(response.getBlogId());
    }

    private static void updateBlog(ManagedChannel channel){
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        UpdateBlogResponse response = blogClient.updateBlog(UpdateBlogRequest.newBuilder()
                .setBlog(Blog.newBuilder()
                        .setId("65b07d46eb870b7a6645c21e")
                        .setTitle("Hello")
                        .setContent("this is content")
                        .setAuthor("Prashant Godhwani").build())
                .build());

        System.out.println("Received response...");
        System.out.println(response.getBlog());
    }

    private static void readBlog(ManagedChannel channel){
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        ReadBlogResponse response = blogClient.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId("TRY")
                .build());

        System.out.println("Received response...");
        System.out.println(response.getBlog());
    }

    private static void createBlog(ManagedChannel channel){
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        CreateBlogResponse response = blogClient.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(Blog.newBuilder()
                        .setTitle("Hello 3 ")
                        .setContent("this is content 3")
                        .setAuthor("Prashant 2").build())
                .build());

        System.out.println("Received response...");
        System.out.println(response.toString());
    }
}
