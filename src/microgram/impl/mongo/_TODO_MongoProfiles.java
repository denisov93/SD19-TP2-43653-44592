package microgram.impl.mongo;

import static microgram.api.java.Result.ErrorCode.INTERNAL_ERROR;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.text.Document;

import org.bson.BSON;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

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
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import utils.TableR;

public class _TODO_MongoProfiles implements Profiles {

    private MongoCollection<Profile> dbProfiles;
    private MongoCollection<TableR> followers;
    private MongoCollection<Post> dbPosts;
    private MongoCollection<TableR> dbLike;
    
    public _TODO_MongoProfiles() {
        MongoClient mongo = new MongoClient("mongo1");
           
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            MongoDatabase dbName = mongo.getDatabase("sd19-tp2-43653-44592").withCodecRegistry(pojoCodecRegistry);

            dbProfiles = dbName.getCollection("Profiles", Profile.class);
            dbPosts = dbName.getCollection("Posts", Post.class);
           
            dbLike = dbName.getCollection("Likes", TableR.class);
            
            followers = dbName.getCollection("Followers", TableR.class);
                
            dbProfiles.createIndex(Indexes.hashed("userId"));
            
            dbPosts.createIndex(Indexes.hashed("ownerId"));
            
            followers.createIndex(Indexes.hashed("field1"));
            
            followers.createIndex(Indexes.ascending("field1","field2"),new IndexOptions().unique(true));
            
            followers.createIndex(Indexes.hashed("field2"));
            
            dbLike.createIndex(Indexes.hashed("field1"));

        	dbLike.createIndex(Indexes.ascending("field1","field2"),new IndexOptions().unique(true));
        	
        	dbLike.createIndex(Indexes.hashed("field2"));
         }
    
	
	@Override
	public Result<Profile> getProfile(String userId) {
		try {
	    MongoIterable<Profile> res = dbProfiles.find(Filters.eq("userId",userId)); 
	    if(!res.iterator().hasNext()) return Result.error(Result.ErrorCode.NOT_FOUND);
	    	Profile p  = res.first();
	    p.setFollowers((int) followers.countDocuments(Filters.eq("field2", userId)));
	    p.setFollowing((int) followers.countDocuments(Filters.eq("field1", userId)));
	    p.setPosts((int) dbPosts.countDocuments(Filters.eq("ownerId", userId)));
	    // System.err.println("Get profile: " + userId + " - " + res.getPosts());
	    return Result.ok(p);
		}catch(Exception e) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
	    try {
	    	
	    	MongoIterable<Profile> res = dbProfiles.find(Filters.eq("userId",profile.getUserId())); 
		    if(res.iterator().hasNext()) return Result.error(Result.ErrorCode.CONFLICT);
	    	dbProfiles.insertOne(profile);
		   
		    return Result.ok();
	    }catch(MongoWriteException x) {
	    	return Result.error(Result.ErrorCode.CONFLICT);
	    }
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
	    try {
	    	Profile p = dbProfiles.findOneAndDelete(Filters.eq("userId",userId));
	        if(p==null)
	        	return Result.error(Result.ErrorCode.NOT_FOUND);
	        followers.deleteMany(Filters.eq("field1",userId));
	        followers.deleteMany(Filters.eq("field2",userId));
	        dbPosts.deleteMany(Filters.eq("ownerId", userId));

	        }catch(MongoWriteException e) {
	        	return Result.error(INTERNAL_ERROR);
	        }
	        return Result.ok();
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		Pattern pattern = java.util.regex.Pattern.compile("^"+prefix);
	    FindIterable<Profile> cur = dbProfiles.find(Filters.or(Filters.eq("fullName",pattern),
	    		Filters.eq("userId", pattern)));
	    List<Profile> prof = new LinkedList<Profile>();
	    for(Profile p : cur)
	    	prof.add(p);

	    return Result.ok(prof);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		   try {
			Profile p1 = dbProfiles.find(Filters.eq("userId",userId1)).first();
		    Profile p2 = dbProfiles.find(Filters.eq("userId",userId2)).first();
		    if(p1==null || p2==null)
		    	return Result.error(Result.ErrorCode.NOT_FOUND);

		    if(isFollowing) {

		    	TableR ts = new TableR(userId1,userId2);
		    	followers.insertOne(ts);
		    }else {
		    	followers.deleteOne(Filters.and(Filters.eq("field1", userId1), Filters.eq("field2",userId2)));
		    }
		    return Result.ok();
		    }catch(Exception e) {
		    	e.printStackTrace();
		    	return Result.error(Result.ErrorCode.CONFLICT);
		    }
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		try {
		MongoIterable<TableR>table = followers.find(Filters.or(
					Filters.and(Filters.eq("field1", userId1),Filters.eq("field2", userId2)),
					Filters.and(Filters.eq("field2", userId1),Filters.eq("field1", userId2))
					));
		if(!table.iterator().hasNext())
		    return Result.ok(false);
		else return Result.ok(true);
		
		}catch(Exception x) {
			x.printStackTrace();
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
	}


	//Para Questao B
	@Override
	public Result<Integer> likesOfPosts(String userId) {
		MongoIterable<Profile> prof = dbProfiles.find(Filters.eq("userId", userId));
		if(!prof.iterator().hasNext()) return Result.error(Result.ErrorCode.NOT_FOUND);
		
		MongoIterable<Post>table = dbPosts.find(Filters.eq("ownerId", userId));
		int count = 0;	
		for(Post p : table){
			String postId = p.getPostId();
			
			long targets = dbLike.countDocuments(Filters.eq("field1" , postId));
			count += (int) targets;
		}

		return Result.ok(count);
	}
}
