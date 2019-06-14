package microgram.impl.mongo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import utils.Hash;
import utils.TableR;

public class _TODO_MongoPosts implements Posts {
	private MongoCollection<Post> dbPosts;
	private MongoCollection<Profile> dbProfiles;
	private MongoCollection<TableR> followers ;
    private MongoCollection<TableR> dbLike;
    
    
	public _TODO_MongoPosts() {
		MongoClient mongo = new MongoClient("mongo1");
    	
    	CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    	MongoDatabase dbName = mongo.getDatabase("sd19-tp2-43653-44592").withCodecRegistry(pojoCodecRegistry);

    	dbPosts = dbName.getCollection("Posts", Post.class);

    	dbProfiles = dbName.getCollection("Profiles",Profile.class);
    	
    	followers = dbName.getCollection("Followers", TableR.class);
    	
    	dbLike = dbName.getCollection("Likes", TableR.class);
    	
    	dbPosts.createIndex(Indexes.hashed("postId"));
    	
    	dbPosts.createIndex(Indexes.hashed("ownerId"));
    	
    	dbProfiles.createIndex(Indexes.hashed("userId"));
    	
    	dbLike.createIndex(Indexes.hashed("field1"));

    	dbLike.createIndex(Indexes.ascending("field1","field2"),new IndexOptions().unique(true));
    	
    	dbLike.createIndex(Indexes.hashed("field2"));
    	
    	followers.createIndex(Indexes.hashed("field1"));
    	
    	followers.createIndex(Indexes.hashed("field2"));
	}

	@Override
	public Result<Post> getPost(String postId) {
	
		MongoIterable<Post> res = dbPosts.find(Filters.eq("postId", postId));
		if(!res.iterator().hasNext())
		return Result.error(Result.ErrorCode.NOT_FOUND);
		else return Result.ok(res.first());
}

	@Override
	public Result<String> createPost(Post post) {
		
		String postId = Hash.of(post.getOwnerId(), post.getMediaUrl());
		
		post.setPostId(postId);
		
		MongoIterable<Post> pst = dbPosts.find(Filters.eq("postId",postId)); 
		
		if(pst.iterator().hasNext()) 
		    return Result.error(Result.ErrorCode.CONFLICT);
		
		MongoIterable<Profile> res = dbProfiles.find(Filters.eq("userId",post.getOwnerId())); 
		if(!res.iterator().hasNext()) 
		    return Result.error(Result.ErrorCode.NOT_FOUND);
		
		dbPosts.insertOne(post);
		
		return Result.ok(postId);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		
			MongoIterable<Post> res = dbPosts.find(Filters.eq("postId",postId));
			if(!res.iterator().hasNext())
			return Result.error(Result.ErrorCode.NOT_FOUND);
			else {
			dbPosts.deleteOne(Filters.eq("postId",postId));
			dbLike.deleteMany(Filters.eq("field1",postId));
			return Result.ok();
			}
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		
			FindIterable<Post> post = dbPosts.find(Filters.eq("postId", postId));
			FindIterable<Profile> profile = dbProfiles.find(Filters.eq("userId",userId));
			
			if(!post.iterator().hasNext() || !profile.iterator().hasNext())
				return Result.error(Result.ErrorCode.NOT_FOUND);
			
			FindIterable<TableR> like = dbLike.find(Filters.and(Filters.eq("field1", postId),Filters.eq("field2", userId)));			

			if(isLiked) {
				if(like.iterator().hasNext())
					return Result.error(Result.ErrorCode.CONFLICT);
				
				TableR tr = new TableR(postId, userId);
				dbLike.insertOne(tr);
			}
			else {
				if(!like.iterator().hasNext())
					return Result.error(Result.ErrorCode.NOT_FOUND);
				dbLike.deleteOne(Filters.and(Filters.eq("field1", postId),Filters.eq("field2", postId)));
			}
			
			return Result.ok();
			
		
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		FindIterable<Post> post = dbPosts.find( Filters.eq("postId",postId) );
		
		if(!post.iterator().hasNext()) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
	
		FindIterable<TableR> res =  dbLike.find(Filters.and(Filters.eq("field1",postId), Filters.eq ("field2",userId)));
		if(!res.iterator().hasNext())
		return Result.ok(false);
		else return Result.ok(true);
	}

	
	@Override
	public Result<List<String>> getPosts(String userId) {
		FindIterable<Profile> res = dbProfiles.find(Filters.eq("userId", userId));
		if(!res.iterator().hasNext())
			return Result.error(Result.ErrorCode.NOT_FOUND);
		
		List<String> list = new ArrayList<String>();
		FindIterable<Post> posts = dbPosts.find(Filters.eq("ownerId", userId));
		MongoCursor<Post> it = posts.iterator();
		while(it.hasNext()) {
			String s = it.next().getPostId();
			list.add(s);
		}
		return Result.ok(list);
		
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		try {
			List<String> ls = new LinkedList<String>();
			MongoCursor<TableR> it1 =  followers.find(Filters.eq("field1", userId)).iterator();
		
			while(it1.hasNext()) {
				TableR user = it1.next();
				//System.out.println(user.getField2());
				MongoCursor<Post> it2 = dbPosts.find(Filters.eq("ownerId",user.getField2() )).iterator();
				while(it2.hasNext()) {
					ls.add(it2.next().getPostId());
				}
			}
			
			return Result.ok(ls);
		}
		catch(Exception x){
			x.printStackTrace();
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
	}

}
