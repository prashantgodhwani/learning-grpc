package org.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://rootuser:rootpass@localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        System.out.println("Received create blog request");

        Blog blog = request.getBlog();
        Document document = new Document("author", blog.getAuthor())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        //create document in mongoDB
        System.out.println("Inserting blog..");
        collection.insertOne(document);

        //retrive mongoDB generated id
        String id = document.getObjectId("_id").toString();
        System.out.println("Inserted blog with id " + id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder()
                        .setId(id)
                        .build()
                ).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Received read blog request");

        String blogId = request.getBlogId();
        System.out.println("blog-id to be read = " + blogId);

        if(!ObjectId.isValid(blogId)){
            System.out.println("Invalid blogId sent - " + blogId);
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            return;
        }

        Document result = collection.find(eq("_id", new ObjectId(blogId)))
                .first();

        if(result == null){
            System.out.println("Blog with id " + blogId + " not found");
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog with id " + blogId + " not found")
                    .asRuntimeException());
        }else{
            System.out.println("Blog with id " + blogId + " found");

            Blog blog = Blog.newBuilder()
                    .setAuthor(result.getString("author"))
                    .setId(blogId)
                    .setTitle(result.getString("title"))
                    .setContent(result.getString("content")).build();

            responseObserver.onNext(ReadBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());

            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        System.out.println("Received update blog request");

        String blogId = request.getBlog().getId();
        System.out.println("blog-id to be read = " + blogId);

        if(!ObjectId.isValid(blogId)){
            System.out.println("Invalid blogId sent - " + blogId);
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            return;
        }

        Document result = collection.find(eq("_id", new ObjectId(blogId)))
                .first();

        if(result == null){
            System.out.println("Blog with id " + blogId + " not found");
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog with id " + blogId + " not found")
                    .asRuntimeException());
        }else{
            System.out.println("Blog with id " + blogId + " found, updating it.");

            Document updatedBlog = new Document("author", request.getBlog().getAuthor())
                    .append("title", request.getBlog().getTitle())
                    .append("content", request.getBlog().getContent());

            collection.replaceOne(eq("_id", result.getObjectId("_id")), updatedBlog);

            responseObserver.onNext(UpdateBlogResponse.newBuilder()
                    .setBlog(request.getBlog())
                    .build());

            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        System.out.println("Received delete blog request");

        String blogId = request.getBlogId();

        if(!ObjectId.isValid(blogId)){
            System.out.println("Invalid blogId sent - " + blogId);
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            return;
        }

        DeleteResult deleteResult = collection.deleteOne(eq("_id", new ObjectId(blogId)));

        if(deleteResult.getDeletedCount() == 0){
            System.out.println("blog with id " + blogId + " not found");
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }else{
            System.out.println("blog with id " + blogId + " was deleted");
            responseObserver.onNext(DeleteBlogResponse.newBuilder().setBlogId(blogId).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("Received list blog request");

        collection.find().forEach(document -> {
            Blog blog = Blog.newBuilder()
                    .setAuthor(document.getString("author"))
                    .setTitle(document.getString("title"))
                    .setContent(document.getString("content")).build();

            responseObserver.onNext(ListBlogResponse.newBuilder()
                    .setBlog(blog)
                    .build());
        });
        responseObserver.onCompleted();
    }
}
